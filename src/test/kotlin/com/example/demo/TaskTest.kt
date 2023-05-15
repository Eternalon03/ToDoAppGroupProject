package com.example.demo

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.*

import kotlinx.serialization.json.Json
import kotlinx.serialization.encodeToString
import kotlinx.serialization.decodeFromString
import java.time.*
import java.util.TreeMap

internal class TaskTest {
    @Test
    fun testMainConstructor(){
        var t = Task("this is a title", "it has a description")
        assertEquals("this is a title", t.title)
        assertEquals("it has a description", t.description)
        assertEquals(null, t.priority)
        assertEquals(null, t.due)
        t = Task(t.title, t.description, Task.Priority.A)
        assertEquals("this is a title", t.title)
        assertEquals("it has a description", t.description)
        assertEquals(Task.Priority.A, t.priority)
        assertEquals(null, t.due)
        t = Task(t.title, t.description, Task.Priority.A, Instant.parse("1970-01-01T00:00:00Z"))
        assertEquals("this is a title", t.title)
        assertEquals("it has a description", t.description)
        assertEquals(Task.Priority.A, t.priority)
        assertEquals(Instant.parse("1970-01-01T00:00:00Z"), t.due)
    }

    @Test
    fun testAlternativeConstructors(){
        var t = Task("this is a title", "it has a description", "A")
        assertEquals("this is a title", t.title)
        assertEquals("it has a description", t.description)
        assertEquals(Task.Priority.A, t.priority)
        assertEquals(null, t.due)
        t = Task(t.title, t.description, "not a priority lol")
        assertEquals("this is a title", t.title)
        assertEquals("it has a description", t.description)
        assertEquals(null, t.priority)
        assertEquals(null, t.due)
        t = Task(t.title, t.description, 'B')
        assertEquals("this is a title", t.title)
        assertEquals("it has a description", t.description)
        assertEquals(Task.Priority.B, t.priority)
        assertEquals(null, t.due)
        t = Task(t.title, t.description, '0')
        assertEquals("this is a title", t.title)
        assertEquals("it has a description", t.description)
        assertEquals(null, t.priority)
        assertEquals(null, t.due)
        t = Task(t.title, t.description, 'C', Instant.parse("1970-01-01T00:00:00Z"))
        assertEquals("this is a title", t.title)
        assertEquals("it has a description", t.description)
        assertEquals(Task.Priority.C, t.priority)
        assertEquals(Instant.parse("1970-01-01T00:00:00Z"), t.due)
    }

    @Test
    fun testTitle() {
        val t = Task("this is a task", "it has a description")
        assertEquals("this is a task", t.title)
        t.title = "updated title"
        assertEquals("updated title", t.title)
    }

    @Test
    fun testDescription() {
        val t = Task("this is a task", "it has a description")
        assertEquals("it has a description", t.description)
        t.description = "updated description"
        assertEquals("updated description", t.description)

    }

    @Test
    fun testPriority() {
        val t = Task("", "", "F")
        assertEquals(Task.Priority.F, t.priority)
        // This demonstrates that the priority aliases work
        t.priority = Task.Priority.LOW
        assertEquals(Task.Priority.D, t.priority)
        t.priority = Task.Priority.MEDIUM
        assertEquals(Task.Priority.C, t.priority)
        t.priority = Task.Priority.HIGH
        assertEquals(Task.Priority.B, t.priority)
        t.priority = Task.Priority.URGENT
        assertEquals(Task.Priority.A, t.priority)
    }

    @Test
    fun testDue() {
        val t = Task("", "", "", Instant.parse("1970-01-01T00:00:00Z"))
        assertEquals(Instant.parse("1970-01-01T00:00:00Z"), t.due)
        t.due = Instant.parse("1970-01-02T00:00:00Z")
        assertEquals(Instant.parse("1970-01-02T00:00:00Z"), t.due)
    }

    @Test
    fun testGetCompleted() {
        val t = Task("","")
        assertFalse(t.completed)
        t.completed = true
        assertTrue(t.completed)
    }

    @Test
    fun testSetCompleted() {
        val t = Task("","")
        assertFalse(t.completed)
        t.inProgress = true
        t.completed = true
        assertTrue(t.completed)
        assertFalse(t.inProgress)
    }

    @Test
    fun testReminders() {
        val t = Task("", "")
        assertEquals(t.reminders, mutableSetOf<Instant>())
        val firstReminder = Instant.parse("1970-01-01T00:00:00Z")
        val secondReminder = Instant.parse("1970-01-02T00:00:00Z")
        t.reminders.add(firstReminder)
        assertEquals(t.reminders, mutableSetOf<Instant>(firstReminder))
        t.reminders.add(secondReminder)
        assertEquals(t.reminders, mutableSetOf<Instant>(firstReminder, secondReminder))
        t.reminders.remove(firstReminder)
        assertEquals(t.reminders, mutableSetOf<Instant>(secondReminder))
    }

    @Test
    fun testIntentions() {
        val t = Task("", "")
        assertEquals(t.intentions, TreeMap<Instant, Instant>())
        val firstIntentionStart = Instant.parse("1970-01-01T00:00:00Z")
        val firstIntentionEnd = Instant.parse("1970-01-01T01:00:00Z")
        val secondIntentionStart = Instant.parse("1970-01-02T00:00:00Z")
        val secondIntentionEnd = Instant.parse("1970-01-02T01:00:00Z")
        t.intentions[firstIntentionStart] = firstIntentionEnd
        assertTrue(firstIntentionStart in t.intentions)
        assertTrue(t.intentions[firstIntentionStart] == firstIntentionEnd)
        t.intentions[secondIntentionStart] = secondIntentionEnd
        assertTrue(secondIntentionStart in t.intentions)
        t.intentions.remove(secondIntentionStart)
        assertFalse(secondIntentionStart in t.intentions)
    }

    @Test
    fun testGetProgress() {
        val t = Task("","")
        assertEquals(listOf<Instant>(), t.progress)
        t.inProgress = true
        assertEquals(1, t.progress.size)
        t.inProgress = false
        assertEquals(2, t.progress.size)
    }

    @Test
    fun testInProgress() {
        val t = Task("","")
        assertFalse(t.inProgress)
        t.completed = true
        t.inProgress = true
        assertTrue(t.inProgress)
        assertFalse(t.completed)
        t.inProgress = false
        assertFalse(t.inProgress)
    }

    @Test
    fun testClearProgress() {
        val t = Task("","")
        assertFalse(t.inProgress)
        for(i in 1..10){
            t.inProgress = !t.inProgress
            t.inProgress = !t.inProgress
        }
        assertFalse(t.inProgress)
        assertEquals(20, t.progress.size)
        val oldProgress = t.progress
        t.clearProgress(5)
        assertFalse(t.inProgress)
        assertEquals(10, t.progress.size)
        assertEquals(oldProgress.slice(10 until oldProgress.size), t.progress)
        t.inProgress = !t.inProgress
        // Clearing progress should preserve inProgress property
        t.clearProgress()
        assertTrue(t.inProgress)
        assertEquals(1, t.progress.size)
    }

    @Test
    fun testLabels() {
        val t = Task("this task has one #label","")
        assertEquals(setOf("label"), t.labels)
        t.title = "this task now has #two #labels"
        assertEquals(setOf("two", "labels"), t.labels)
        t.title = "this task now has no#label"
        assertEquals(setOf<String>(), t.labels)
    }

    @Test
    fun testToString() {
        val t = Task("this is a task", "it has a description")
        assertEquals(
            """{"title":"this is a task","description":"it has a description"}""",
            t.toString())
    }

    @Test
    fun testToTodoTxtString() {
        val t = Task("title #label \n#another-label not#a#label", "this is a description",
                        Task.Priority.A, Instant.now())
        assertEquals("(A) title @label  @another-label not#a#label " +
                "desc:this+is+a+description " +
                "due:${t.due!!.atZone(ZoneId.systemDefault()).toLocalDate()} \n", t.toTodoTxtString())
        t.completed = true
        assertEquals("x ${LocalDate.now()} title @label  @another-label not#a#label " +
                "desc:this+is+a+description " +
                "pri:A " +
                "due:${t.due!!.atZone(ZoneId.systemDefault()).toLocalDate()} \n", t.toTodoTxtString())
        t.priority = null
        assertEquals("x ${LocalDate.now()} title @label  @another-label not#a#label " +
                "desc:this+is+a+description " +
                "due:${t.due!!.atZone(ZoneId.systemDefault()).toLocalDate()} \n", t.toTodoTxtString())
        t.completed = false
        assertEquals("title @label  @another-label not#a#label " +
                "desc:this+is+a+description " +
                "due:${t.due!!.atZone(ZoneId.systemDefault()).toLocalDate()} \n", t.toTodoTxtString())
        t.due = null
        assertEquals("title @label  @another-label not#a#label " +
                "desc:this+is+a+description \n", t.toTodoTxtString())
        t.due = Instant.now()
        t.description = ""
        assertEquals("title @label  @another-label not#a#label " +
                "due:${t.due!!.atZone(ZoneId.systemDefault()).toLocalDate()} \n", t.toTodoTxtString())
    }

    @Test
    fun testParse() {
        val s = """{"title": "this is a task","description": "it has a description"}"""
        val t = Task.parse(s)
        assertEquals("this is a task", t.title)
        assertEquals("it has a description", t.description)
        assertEquals(null, t.priority)
        assertEquals(null, t.due)
        assertFalse(t.completed)
        assertEquals(mutableSetOf<Instant>(), t.reminders)
        assertEquals(TreeMap<Instant, Instant>(), t.intentions)
        assertEquals(listOf<Instant>(), t.progress)
        assertFalse(t.inProgress)
    }

    @Test
    fun testEquals() {
        val t1 = Task("", "")
        var t2 = Task("", "")
        // t1 and t2 are equal different task objects in memory
        assertFalse(t1 === t2)
        assertEquals(t1, t2)
        t1.inProgress = true
        Thread.sleep(100)
        t2.inProgress = true
        // t1 and t2 aren't equal because they were marked as in progress at different times
        assertNotEquals(t1, t2)
        // this forces t1 to have identical data to t2, so they should be equal now
        t2 = Task.parse(t1.toString())
        assertFalse(t1 === t2)
        assertEquals(t1, t2)
    }

    @Test
    fun testHashCode() {
        val t1 = Task("", "")
        val t2 = Task("", "")
        assertFalse(t1 === t2)
        assertEquals(t1, t2)
        assertEquals(t1.hashCode(), t2.hashCode())
    }

    @Test
    fun testComparators() {
        val today = Instant.now()
        val yesterday = today.minusSeconds(86400)
        val tomorrow = today.plusSeconds(86400)
        val t1 = Task("Do a thing", "foxtrot", "C", tomorrow)
        val t2 = Task("Do not do a thing", "echo", "D", today)
        val t3 = Task("Done a thing", "delta", "B", today) ; t3.completed = true
        val t4 = Task("Have done a thing", "charlie", "Q", yesterday) ; t4.completed = true
        val t5 = Task("Will do a thing", "bravo", "A", tomorrow) ; t5.inProgress = true
        val t6 = Task("Won't do a thing", "alpha", "", yesterday) ; t6.inProgress = true
        val taskList = mutableListOf(t2, t1, t4, t3, t6, t5)
        assertNotEquals(taskList, mutableListOf(t1, t2, t3, t4, t5, t6))
        taskList.sortWith(Task.titleComparator)
        assertEquals(taskList, mutableListOf(t1, t2, t3, t4, t5, t6))
        taskList.sortWith(Task.descriptionComparator)
        assertEquals(taskList, mutableListOf(t6, t5, t4, t3, t2, t1))
        taskList.sortWith(Task.priorityComparator)
        assertEquals(taskList, mutableListOf(t5, t3, t1, t2, t4, t6))
        taskList.sortWith(Task.dueComparator)
        assertEquals(taskList, mutableListOf(t4, t6, t3, t2, t5, t1))
        taskList.sortWith(Task.completedComparator)
        assertEquals(taskList, mutableListOf(t6, t2, t5, t1, t4, t3))
        taskList.sortWith(Task.inProgressComparator)
        assertEquals(taskList, mutableListOf(t6, t5, t2, t1, t4, t3))
    }

    @Test
    fun testSerialization() {
        val taskList = mutableListOf(Task("t1","desc"),
                                     Task("t2","desc"),
                                     Task("t3","desc"),
                                     Task("t4","desc"),
                                     Task("t5","desc"))
        val taskListString = Json.encodeToString(taskList)
        assertEquals("""[{"title":"t1","description":"desc"},"""
            +"""{"title":"t2","description":"desc"},"""
            +"""{"title":"t3","description":"desc"},"""
            +"""{"title":"t4","description":"desc"},"""
            +"""{"title":"t5","description":"desc"}]""", taskListString)
        val otherTaskList = Json.decodeFromString<MutableList<Task>>(taskListString)
        assertEquals(otherTaskList, taskList)
    }
}
