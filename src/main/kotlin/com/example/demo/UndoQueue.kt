package com.example.demo

import kotlinx.serialization.json.Json
import kotlinx.serialization.encodeToString
import kotlinx.serialization.decodeFromString
import java.util.ArrayDeque
import java.util.Deque

/*
A class designed to encapsulate the behaviour of an "undo/redo" queue.
This class stores current, previous, and future versions of the state
of some serializable object, represented as a JSON String, within it.

UndoQueue has no public constructor. Instead, UndoQueue objects are
created using the create method provided with a mandatory initial state,
as well as an optional maxUndoLimit parameter that specifies the
maximum number of changes to track for undo. A negative value for maxUndoLimit
specifies an unlimited number of changes (this is the default option).

There are three principal operations of an UndoQueue:

- registerNewState: registers an object's state change with the UndoQueue.
  This "alters history" by destroying the redo queue

- performUndo: rolls back an object to a previous state and logs the current state for redo

- performRedo: rolls forward an object to a future state and logs the current state for undo

NOTE: There are versions of all these operations that take an
UndoQueue.NoSerialization object as an initial parameter. This tells
the UndoQueue to operate on the Strings directly and not to perform any
JSON serialization. It is not recommended to call these methods directly.

The typical usage looks something like this:

var obj: T = someValue // A JSON serializable object
val undoQueue = UndoQueue.create(obj) // the undo queue for this object

obj = someOtherValue // changes obj
undoQueue.registerNewState(obj) // registers the change to obj in undoQueue

obj = undoQueue.performUndo() // now obj == someValue again

obj = undoQueue.performRedo() // now obj == someOtherValue again
*/
class UndoQueue private constructor (var maxUndoLimit: Int = -1){

    private val undoQueue: Deque<String> = ArrayDeque()
    private lateinit var currentState: String
    private val redoQueue: Deque<String> = ArrayDeque()

    companion object {
        /*
        The singleton object to distinguish between the
        methods that serialize and those that do not
        */
        object NoSerialization

        /*
        Creates a new UndoQueue with initial state initialState
        and a maximum undo limit of maxUndoLimit.
        This function does not serialize the initial state.
        */
        @Suppress("UNUSED_PARAMETER")
        fun create(noSerialize: NoSerialization,
                   initialState: String, maxUndoLimit: Int = -1): UndoQueue {
            val newQueue = UndoQueue(maxUndoLimit)
            newQueue.currentState = initialState
            return newQueue
        }

        /*
        Creates a new UndoQueue with initial state initialState
        and a maximum undo limit of maxUndoLimit.
        This function serializes the initial state.
        */
        inline fun <reified T> create(initialState: T, maxUndoLimit: Int = -1): UndoQueue {
            return create(NoSerialization, Json.encodeToString(initialState), maxUndoLimit)
        }
    }

    /*
    Registers the new state newState with this UndoQueue.
    This function does not serialize the new state.
    */
    @Suppress("UNUSED_PARAMETER")
    fun registerNewState(noSerialize: NoSerialization, newState: String){
        undoQueue.addFirst(currentState)
        if(maxUndoLimit >= 0 && undoQueue.size > maxUndoLimit) {
            undoQueue.pollLast()
        }
        currentState = newState
        redoQueue.clear()
    }

    /*
    Registers the new state newState with this UndoQueue.
    This function serializes the new state.
    */
    inline fun <reified T> registerNewState(newState: T){
        registerNewState(NoSerialization, Json.encodeToString(newState))
    }

    /*
    Rolls back the state of this UndoQueue and returns the rolled back state.
    This function does not deserialize the rolled back state.
    */
    @Suppress("UNUSED_PARAMETER")
    fun performUndo(noSerialize: NoSerialization): String {
        val undoState: String? = undoQueue.pollFirst()
        if(undoState != null) {
            redoQueue.addFirst(currentState)
            currentState = undoState
            return undoState
        }
        return currentState
    }

    /*
    Rolls back the state of this UndoQueue and returns the rolled back state.
    This function deserializes the rolled back state.
    */
    inline fun <reified T> performUndo(): T {
        return Json.decodeFromString(performUndo(NoSerialization))
    }

    /*
    Rolls forward the state of this UndoQueue and returns the rolled forward state.
    This function does not deserialize the rolled forward state.
    */
    @Suppress("UNUSED_PARAMETER")
    fun performRedo(noSerialize: NoSerialization): String {
        val redoState: String? = redoQueue.pollFirst()
        if(redoState != null) {
            undoQueue.addFirst(currentState)
            currentState = redoState
            return redoState
        }
        return currentState
    }

    /*
    Rolls forward the state of this UndoQueue and returns the rolled forward state.
    This function deserializes the rolled forward state.
    */
    inline fun <reified T> performRedo(): T {
        return Json.decodeFromString(performRedo(NoSerialization))
    }
}
