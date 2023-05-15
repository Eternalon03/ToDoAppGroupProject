package com.example.demo

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.*

import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream

internal class PersistenceTest {

    @Test
    fun testLoad() {
        val taskListString = ("""[{"title":"t1","description":"desc"},"""
        +"""{"title":"t2","description":"desc"},"""
        +"""{"title":"t3","description":"desc"},"""
        +"""{"title":"t4","description":"desc"},"""
        +"""{"title":"t5","description":"desc"}]""")
        val input = ByteArrayInputStream(taskListString.toByteArray(Charsets.UTF_8))
        val taskList: MutableList<Task> = Persistence.load(input) ?: mutableListOf()
        assertEquals(
            mutableListOf(Task("t1","desc"),
            Task("t2","desc"),
            Task("t3","desc"),
            Task("t4","desc"),
            Task("t5","desc")), taskList)
        input.close()
    }

    @Test
    fun testSave() {
        val taskList = mutableListOf(Task("t1","desc"),
            Task("t2","desc"),
            Task("t3","desc"),
            Task("t4","desc"),
            Task("t5","desc"))
        val output = ByteArrayOutputStream()
        Persistence.save(taskList, output)
        val taskListString = output.toString(Charsets.UTF_8)
        assertEquals("""[{"title":"t1","description":"desc"},"""
                +"""{"title":"t2","description":"desc"},"""
                +"""{"title":"t3","description":"desc"},"""
                +"""{"title":"t4","description":"desc"},"""
                +"""{"title":"t5","description":"desc"}]""", taskListString)
        output.close()
    }
}
