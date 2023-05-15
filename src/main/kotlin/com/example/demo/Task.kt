package com.example.demo

import java.time.*
import java.util.TreeMap
import java.net.URLEncoder
import kotlin.math.min
import kotlin.comparisons.*
import kotlinx.serialization.*
import kotlinx.serialization.builtins.*
import kotlinx.serialization.encoding.*
import kotlinx.serialization.descriptors.*
import kotlinx.serialization.json.Json

/*
A Task represents a discrete item in a to-do list. All Task
objects consist of the following properties:

title: String - the title of the Task.

description: String - the description of the Task.

priority: Priority? - the priority of the Task.
May be null if the Task has no priority.

due: Instant? - the due date of the Task. May be null if
the Task has no due date. Note that the type of Instant
(java.time.Instant) represents a timestamp, using the Java time API.

completed: Boolean - whether or not the Task is marked as completed.

reminders: MutableSet<Instant> - the set of reminders attached to the Task.

intentions: TreeMap<Instant, Instant> - the set of intentions attached to
the Task. An intention represents a block of time in which the user intends to
work on or complete the item on their to-do list. This property reflects that
by storing each block of time in a key-value pair, with the key representing
the starting timestamp and the value representing the ending timestamp.
  The class TreeMap (java.util.TreeMap) is used as a MutableMap implementation,
as it allows for getting "floor entries" and "ceiling entries", which is
useful for checking non-overlapping. It also allows iteration over the entries
in sorted order by the keys. Note that this class makes no attempt to ensure
that the time blocks stored in the intentions field are non-overlapping,
as the intentions field is exposed directly for modification.

progress: List<Instant> - a list representing the actual progress that a user
has made working on their to-do item (i.e. working on the Task). The entries in
the list are the timestamps when the user has either started working or
stopped working on the Task, which then form blocks of time via grouping into
pairs. The even-indexed entries are the timestamps when the user started
working, and the odd-indexed entries are the timestamps when the user paused
working. Thus, a task is "in progress" if and only if the progress field has an
odd number of entries. Note that the progress field is stored as an immutable
list and cannot be mutated outside of this class. Instead, progress is modified
via the derived field inProgress (see below) and the clearProgress method.

inProgress: Boolean - whether or not the Task is in progress. This property
is a derived field, gotten from checking whether or not the progress field has
an odd number of entries. When this property is toggled (i.e. set from
false to true or from true to false), the current time is
automatically appended to the progress field.

labels: Set<String> - The labels for the Task. This property is a derived
field, gotten from returning the set of all whitespace delimited tokens
in the title that begin with "#" and are more than 1 character long.
The elements in labels are without their initial "#" character.
This property cannot be set; it depends on the value of the title.

*/

/*
Task objects are supplied with a title, description, priority, and due value
upon being constructed. The other non-derived properties are given appropriate
default values upon construction:

completed defaults to false
reminders defaults to an empty set
intentions defaults to an empty list
progress defaults to an empty list (i.e. Tasks start with no progress)
*/
@Serializable
class Task (var title: String = "", var description: String = "", var priority: Priority? = null,
            @Serializable(with = InstantSerializer::class) var due: Instant? = null){

    /*
    The class representing a priority in a Task. A priority is represented by
    one of the 26 letters of the alphabet, with earlier letters representing
    higher priorities. The aliases URGENT, HIGH, MEDIUM, and LOW are also
    provided for the first four priority letters.
    */
    enum class Priority {
        A,B,C,D,E,F,G,H,I,J,K,L,M,N,O,P,Q,R,S,T,U,V,W,X,Y,Z;

        companion object {
            val URGENT = A
            val HIGH = B
            val MEDIUM = C
            val LOW = D
        }
    }

    var completed: Boolean = false
        set(value) {
            // A completed Task cannot be in progress,
            // so set inProgress to false in this case
            if(value && inProgress){ inProgress = false }
            field = value
        }

    var reminders = mutableSetOf<@Serializable(with = InstantSerializer::class) Instant>()

    @Serializable(with = InstantTreeMapSerializer::class)
    var intentions = TreeMap<@Serializable(with = InstantSerializer::class) Instant,
                             @Serializable(with = InstantSerializer::class) Instant>()

    var progress = listOf<@Serializable(with = InstantSerializer::class) Instant>()
        private set

    var inProgress: Boolean
        get() = progress.size % 2 != 0
        set(value) {
            // An in progress Task cannot be completed,
            // so set completed to false in this case
            if(value && completed){ completed = false }
            // if the new value and the current value are different,
            // update the progress list to toggle the current value
            if(value != inProgress){ progress = progress.plus(Instant.now()) }
        }

    /*
    Clears the oldest count blocks of progress from the progress list. If
    count is negative, then all progress is cleared from the progress list.
    Note that entries in the list are naturally cleared in pairs, and the
    last entry of a Task currently in progress is never cleared, so
    running this method does not affect the value of the inProgress property.
    */
    fun clearProgress(count: Int = -1) {
        val numEntries = min((if (count < 0) progress.size/2 else count), progress.size/2)
        progress = progress.slice(2*numEntries until progress.size)
    }

    val labels: Set<String>
        get() = title.split(Regex("\\s+"))
                     .filter{ s -> Regex("#.+").matches(s) }
                     .map{ s -> s.slice(1 until s.length) }
                     .toSet()

    /*
    An alternative constructor in case one wants
    to supply the priority as a String.
    */
    constructor (title: String, description: String,
        priority: String, due: Instant? = null): this(title, description, null, due) {
        if(priority.matches(Regex("[A-Z]"))){
            this.priority = Priority.valueOf(priority)
        }
    }

    /*
    An alternative constructor in case one wants
    to supply the priority as a Char.
    */
    constructor (title: String, description: String,
        priority: Char, due: Instant? = null): this(title, description, "$priority", due)

    /*
    Returns a JSON-encoded String representation of this Task.
    */
    override fun toString() = Json.encodeToString(this)

    /*
    Returns a TodoTxt-encoded String representation of this Task.
    Not all information is preserved: Only the title,
    description, priority, due date (but not time), and completed
    properties get preserved.

    Note: All labels in the title are treated as TodoTxt "contexts"
    and are converted as such. The description is URL-encoded.
    */
    fun toTodoTxtString(): String {
        val titleString = title.split(Regex("\\s"))
                               .map { s ->
                                   if (Regex("#.+").matches(s)){
                                       "@${s.slice(1 until s.length)}"
                                   }else{ s }
                              }.joinToString(" ")+" "
        val descriptionString = if(description.isNotEmpty()){
            "desc:${URLEncoder.encode(description, "utf-8")} "
        }else{ "" }
        val priorityString = if(priority != null){
            if(completed){ "pri:$priority " }else{ "($priority) " }
        }else{ "" }
        val dueString = if(due != null){
            "due:${due!!.atZone(ZoneId.systemDefault()).toLocalDate()} "
        }else{ "" }
        val completedString = if(completed){
            "x ${progress.lastOrNull()?.atZone(ZoneId.systemDefault())?.toLocalDate() ?: LocalDate.now()} "
        }else{ "" }
        return completedString +
               if(completed){ "" }else{ priorityString } +
               titleString +
               descriptionString +
               if(completed){ priorityString }else{ "" } +
               dueString + "\n"
    }

    /*
    Returns true if this Task is equal to other, and false otherwise.
    Every property is checked for equality, including progress
    (which cannot directly be modified).
    */
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Task

        if (title != other.title) return false
        if (description != other.description) return false
        if (priority != other.priority) return false
        if (due != other.due) return false
        if (completed != other.completed) return false
        if (reminders != other.reminders) return false
        if (intentions != other.intentions) return false
        if (progress != other.progress) return false

        return true
    }

    /*
    Returns a hash code for this Task.
    */
    override fun hashCode(): Int {
        var result = title.hashCode()
        result = 31 * result + description.hashCode()
        result = 31 * result + (priority?.hashCode() ?: 0)
        result = 31 * result + (due?.hashCode() ?: 0)
        result = 31 * result + completed.hashCode()
        result = 31 * result + reminders.hashCode()
        result = 31 * result + intentions.hashCode()
        result = 31 * result + progress.hashCode()
        return result
    }

    companion object {
        /*
        Returns a new Task parsed from a JSON-encoded
        String representation of a Task.
        */
        fun parse(jsonStr: String) = Json.decodeFromString<Task>(jsonStr)

        /*
        A Comparator to compare Task objects by title.
        */
        val titleComparator = Comparator<Task> { a, b -> a.title.compareTo(b.title) }

        /*
        A Comparator to compare Task objects by description.
        */
        val descriptionComparator = Comparator<Task> {
                a, b -> a.description.compareTo(b.description)
        }

        /*
        A Comparator to compare Task objects by priority.
        Note that higher priority tasks compare as less than lower
        priority tasks, as these tasks come first. Tasks with no
        priority come last and are compared accordingly.
        */
        val priorityComparator = Comparator<Task> {
                a, b -> nullsLast(naturalOrder<Priority>()).compare(a.priority, b.priority)
        }

        /*
        A Comparator to compare Task objects by due.
        Tasks with no due date come last and are compared accordingly.
        */
        val dueComparator = Comparator<Task> {
                a, b -> nullsLast(naturalOrder<Instant>()).compare(a.due, b.due)
        }

        /*
        A Comparator to compare Task objects by completed.
        Incomplete Tasks come before complete tasks.
        */
        val completedComparator = Comparator<Task> {
                a, b -> a.completed.compareTo(b.completed)
        }

        /*
        A Comparator to compare Task objects by inProgress.
        In progress Tasks come before not in progress tasks.
        */
        val inProgressComparator = Comparator<Task> {
                // Notice the reversal here. Without this,
                // not in progress Tasks would come before in progress Tasks.
                a, b -> b.inProgress.compareTo(a.inProgress)
        }
    }
}

/*
The objects below are KSerializer objects that are needed in order for the
Task class to be serializable by the Kotlin Serialization Module. They are
kept private as they are only needed to make serialization work.

The object InstantSerializer is for serializing java.time.Instant instances,
while the object InstantTreeMapSerializer is for serializing java.util.TreeMap
instances whose keys and values are both java.time.Instant instances.

The Task class itself uses the Kotlin Serialization Module in order to
convert Task objects to and from JSON-encoded Strings.
*/

private object InstantSerializer: KSerializer<Instant> {
    override val descriptor = PrimitiveSerialDescriptor("Instant", PrimitiveKind.STRING)

    override fun deserialize(decoder: Decoder): Instant {
        return Instant.parse(decoder.decodeString())
    }

    override fun serialize(encoder: Encoder, value: Instant) {
        encoder.encodeString(value.toString())
    }
}

private object InstantTreeMapSerializer: KSerializer<TreeMap<Instant, Instant>> {
    private val mapSerializer = MapSerializer(InstantSerializer, InstantSerializer)

    override val descriptor: SerialDescriptor = mapSerializer.descriptor

    override fun serialize(encoder: Encoder, value: TreeMap<Instant, Instant>) {
        mapSerializer.serialize(encoder, value)
    }

    override fun deserialize(decoder: Decoder): TreeMap<Instant, Instant> {
        return TreeMap<Instant, Instant>(mapSerializer.deserialize(decoder))
    }
}
