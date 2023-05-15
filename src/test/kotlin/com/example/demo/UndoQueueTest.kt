package com.example.demo

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.*

internal class UndoQueueTest {

    @Test
    fun testUndoRedo() {
        var value = 5
        val undoQueue = UndoQueue.create(value)
        value = 11
        undoQueue.registerNewState(value)
        // perform undo redo
        value = undoQueue.performUndo()
        assertEquals(5, value)
        value = undoQueue.performRedo()
        assertEquals(11, value)
    }

    @Test
    fun testMaxUndoLimit() {
        var value: List<Int> = listOf()
        val undoQueue = UndoQueue.create(value, 5) // this UndoQueue only tracks last 5 states
        for(i in 1..10){
            value = value.plus(i)
            undoQueue.registerNewState(value)
        }
        // performs 10 undos, but only 5 will actually have any effect
        for(i in 1..10){
            value = undoQueue.performUndo()
        }
        assertEquals(listOf(1,2,3,4,5), value)
    }
}
