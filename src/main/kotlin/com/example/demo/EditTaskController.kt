package com.example.demo

import javafx.fxml.FXML
import javafx.scene.control.*
import javafx.scene.input.KeyCode
import javafx.scene.input.KeyEvent
import javafx.scene.layout.AnchorPane
import javafx.stage.Stage
import java.time.LocalDate
import java.time.ZoneId

class EditTaskController {
    @FXML
    private lateinit var welcomeText: Label

    @FXML
    private val saveButton: Button? = null

    @FXML
    private var titleText: TextField? = null

    @FXML
    private var labelAdder: TextField? = null

    @FXML
    private var descText: TextArea? = null

    @FXML
    private var priorityPicker: ComboBox<String>? = ComboBox<String>()

    @FXML
    private var dueDatePicker: DatePicker? = null

    @FXML
    private val backAnchor: AnchorPane? = null

    @FXML
    private val subtask: ComboBox<*>? = null

    lateinit var mainController: MainAppController
    lateinit var currentStage: Stage

    lateinit var workingTask: Task
    // true if we are adding a new Task in this window, and
    // false if we are editing an existing Task in this window.
    var workingTaskIsNew: Boolean = false

    companion object {
        private val priorityMap = mapOf(
            null to null,
            "None" to null,
            "Urgent" to Task.Priority.URGENT,
            "High" to Task.Priority.HIGH,
            "Medium" to Task.Priority.MEDIUM,
            "Low" to Task.Priority.LOW)

        private fun priorityStringOf(pri: Task.Priority?): String {
            return when(pri) {
                null -> "None"
                Task.Priority.URGENT -> "Urgent"
                Task.Priority.HIGH -> "High"
                Task.Priority.MEDIUM -> "Medium"
                Task.Priority.LOW -> "Low"
                else -> pri.toString()
            }
        }
        private fun optionalDateOf(value: String?): LocalDate?{
            return try{ LocalDate.parse(value) }catch(_: Exception){ null }
        }
    }

    @FXML
    fun initialize () {
            priorityPicker?.items?.addAll(
                    "None",
                    "Urgent",
                    "High",
                    "Medium",
                    "Low"
                )
    }

    @FXML
    fun populateFields() {
        titleText!!.text = workingTask.title
        descText!!.text = workingTask.description
        priorityPicker?.value = priorityStringOf(workingTask.priority)
        dueDatePicker?.value = workingTask.due?.atZone(ZoneId.systemDefault())?.toLocalDate()
    }

    @FXML
    private fun onSaveButtonClick() {
        val title = titleText!!.text
        val labelsToAdd = labelAdder!!.text.split(Regex("\\s+"))
                                           .map{ s -> (if(s.isEmpty()) "" else " #") + s }
                                           .joinToString("")
        val description = descText!!.text
        val priority = priorityMap[priorityPicker?.value]
        val due = optionalDateOf(dueDatePicker?.editor?.text)
                  ?.atStartOfDay(ZoneId.systemDefault())?.toInstant()

        workingTask.title = title + labelsToAdd
        workingTask.description = description
        workingTask.priority = priority
        workingTask.due = due

        if(workingTaskIsNew) {
            ApplicationData.taskList.add(workingTask)
            mainController.addToTaskView(workingTask)
        }
        else{
            mainController.refreshTaskView()
        }

        Persistence.save(ApplicationData.taskList, ApplicationData.prefs.localSaveLocation)
        Persistence.Cloud.put(ApplicationData.taskList, ApplicationData.prefs.cloudSaveLocation)
        currentStage.close()
    }

    @FXML
    private fun onKeyPressed(event: KeyEvent) {
        val save: KeyCode = KeyCode.S
        if(event.isControlDown){
            when(event.code){
                save -> onSaveButtonClick()
                else -> {}
            }
        }
    }
}
