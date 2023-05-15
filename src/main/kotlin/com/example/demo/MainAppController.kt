package com.example.demo

import javafx.beans.property.SimpleStringProperty
import javafx.fxml.FXML
import javafx.fxml.FXMLLoader
import javafx.scene.Scene
import javafx.scene.control.*
import javafx.scene.input.KeyCode
import javafx.scene.input.KeyEvent
import javafx.scene.layout.AnchorPane
import javafx.scene.layout.VBox
import javafx.scene.paint.Color
import javafx.scene.text.Font
import javafx.stage.FileChooser
import javafx.stage.Modality
import javafx.stage.Stage
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlin.system.exitProcess
import java.time.ZoneId

class MainAppController {
    @FXML
    private lateinit var welcomeText: Label

    @FXML
    private val createTaskButton: Button? = null

    @FXML
    private val x3: Font? = null

    @FXML
    private val x4: Color? = null

    @FXML
    private val trueBack: VBox? = null


    // An object encapsulating a Task object, except converting to string
    // returns a string suitable for a ListView object.
    class TaskViewer (val task: Task){
      override fun toString(): String {
          return when {
              task.completed -> "\u2611 " // ☑
              task.inProgress -> "\u25b6 " // ▶
              else -> "\u2610 " // ☐
          } + when(task.priority) {
              null -> ""
              Task.Priority.URGENT -> "\u2757\u2757\u2757\u2757 " // ❗❗❗❗
              Task.Priority.HIGH -> "\u2757\u2757\u2757 " // ❗❗❗
              Task.Priority.MEDIUM -> "\u2757\u2757 " // ❗❗
              Task.Priority.LOW -> "\u2757 " // ❗
              else -> "(${task.priority}) " // the literal letter value
          } + task.title
        }
    }

    @FXML
    private var taskView: ListView<TaskViewer>? = ListView<TaskViewer>()

    @FXML
    private var searchBox: TextField? = null
    private var selectedLabels: MutableSet<String> = mutableSetOf()
    private val searchFunction: (Task) -> Boolean = { t ->
        t.labels.containsAll(selectedLabels) && (
                searchBox?.text
                         ?.split(Regex("\\s+"))
                         ?.all{ it in t.title || it in t.description } ?: true
        )
    }

    var undoQueue = UndoQueue.create(ApplicationData.taskList)

    // clipboard is a serialized Task. This forces copy
    // and paste to actually duplicate the Tasks.
    private var clipboard: String? = null

    @FXML
    fun initialize () {
        searchBox?.textProperty()?.addListener{ _ ->
            repopulateTaskView()
        }
        taskView?.itemsProperty()?.addListener { _ ->
            displaySelectedTaskInSidebar()
        }
        taskView?.selectionModel?.selectedItemProperty()?.addListener { _ ->
            displaySelectedTaskInSidebar()
        }
        sideTitle.heightProperty().addListener { _ ->
            displaySelectedTaskInSidebar()
        }
        setFontSize(ApplicationData.prefs.fontSize)
        repopulateTaskView()
    }

    @FXML
    private fun launchTaskEditor(workingTask: Task, isNewTask: Boolean) {
        val stage = Stage()
        val fxmlLoader = FXMLLoader(IntentionsApplication::class.java.getResource("editTask.fxml"))
        val scene = Scene(fxmlLoader.load(), ApplicationData.prefs.editWidth, ApplicationData.prefs.editHeight)
        stage.scene = scene
        stage.scene.stylesheets.add(IntentionsApplication::class.java.getResource("/Stylesheets/edit.css").toExternalForm())
        val editTaskController: EditTaskController = fxmlLoader.getController()
        editTaskController.mainController = this
        editTaskController.currentStage = stage
        editTaskController.workingTask = workingTask
        editTaskController.workingTaskIsNew = isNewTask
        editTaskController.populateFields()
        stage.initModality(Modality.APPLICATION_MODAL)

        stage.scene.heightProperty().addListener { _ ->
            ApplicationData.prefs.editHeight = stage.scene.heightProperty().value
            Persistence.save(ApplicationData.prefs, ApplicationData.prefSaveLocation)
        }

        stage.scene.widthProperty().addListener { _ ->
            ApplicationData.prefs.editWidth = stage.scene.widthProperty().value
            Persistence.save(ApplicationData.prefs, ApplicationData.prefSaveLocation)
        }

        stage.xProperty().addListener { _ ->
            ApplicationData.prefs.editX = stage.xProperty().value
            Persistence.save(ApplicationData.prefs, ApplicationData.prefSaveLocation)
        }

        stage.yProperty().addListener { _ ->
            ApplicationData.prefs.editY = stage.yProperty().value
            Persistence.save(ApplicationData.prefs, ApplicationData.prefSaveLocation)
        }

        stage.x = ApplicationData.prefs.editX
        stage.y = ApplicationData.prefs.editY

        stage.showAndWait()
    }

    @FXML
    private fun onPreferences() {
        val stage = Stage()
        val fxmlLoader = FXMLLoader(IntentionsApplication::class.java.getResource("pref.fxml"))
        val scene = Scene(fxmlLoader.load(), 400.0, 215.0)
        stage.scene = scene
        stage.scene.stylesheets.add(IntentionsApplication::class.java.getResource("/Stylesheets/prefs.css").toExternalForm())
        val prefController: PrefController = fxmlLoader.getController()
        prefController.currentStage = stage
        prefController.mainController = this
        stage.initModality(Modality.APPLICATION_MODAL)

        stage.showAndWait()
    }

    @FXML
    private fun onExportToTodoTxt(){
        val chooser = FileChooser()
        chooser.initialFileName = "todo.txt"
        try{
            chooser.showSaveDialog(null)
                   .writeText(ApplicationData.taskList
                                             .map{ t -> t.toTodoTxtString() }
                                             .joinToString(""), Charsets.UTF_8)
        }catch(_: Exception){}
    }

    @FXML
    private fun onExportToJson(){
        val chooser = FileChooser()
        chooser.initialFileName = "taskList.json"
        try{
            chooser.showSaveDialog(null)
                .writeText(Json.encodeToString(ApplicationData.taskList), Charsets.UTF_8)
        }catch(_: Exception){}
    }

    @FXML
    fun setFontSize(fontSize: Int){
        taskView?.style = ".list-cell {\n" +
                "    -fx-font-family: \"Helvetica\";\n" +
                "    -fx-font-size: ${fontSize}px;\n" + "}"

        sideDescription.style = "#sideDescription {\n" +
                "    -fx-font-family: \"Helvetica\";\n" +
                "    -fx-font-size: ${fontSize}px;\n" + "}"

        sideTitle.style = "#sideTitle {\n" +
                "    -fx-font-family: \"Helvetica\";\n" +
                "    -fx-font-size: ${fontSize+6}px;\n" + "}"

        sideDate.style = "#sideDate {\n" +
                "    -fx-font-family: \"Helvetica\";\n" +
                "    -fx-font-size: ${fontSize+2}px;\n" + "}"
    }

    @FXML
    fun repopulateTaskView(searchFilter: (Task) -> Boolean = searchFunction) {
        taskView?.items?.clear()
        taskView?.items?.addAll(
            ApplicationData.taskList.filter(searchFilter).map{ t -> TaskViewer(t) }
        )
    }

    @FXML
    fun addToTaskView(value: Task){
        taskView?.items?.add(TaskViewer(value))
    }

    @FXML
    fun refreshTaskView(){
        taskView?.refresh()
        displaySelectedTaskInSidebar()
    }

    @FXML
    private fun onCreateTaskButtonClick() {
        launchTaskEditor(Task(), true)
    }

    @FXML
    private fun sortTasks(comp: Comparator<Task>){
        ApplicationData.taskList.sortWith(comp)
        repopulateTaskView()
        Persistence.save(ApplicationData.taskList, ApplicationData.prefs.localSaveLocation)
        Persistence.Cloud.put(ApplicationData.taskList, ApplicationData.prefs.cloudSaveLocation)
        undoQueue.registerNewState(ApplicationData.taskList)
    }
    @FXML
    private fun onTaskSortByTitle() = sortTasks(Task.titleComparator)
    @FXML
    private fun onTaskSortByTitleDesc() = sortTasks(Task.titleComparator.reversed())
    @FXML
    private fun onTaskSortByStatus() = sortTasks(Task.completedComparator
                                                     .then(Task.inProgressComparator))
    @FXML
    private fun onTaskSortByPriority() = sortTasks(Task.priorityComparator)
    @FXML
    private fun onTaskSortByPriorityDesc() = sortTasks(Task.priorityComparator.reversed())
    @FXML
    private fun onTaskSortByDue() = sortTasks(Task.dueComparator)
    @FXML
    private fun onTaskSortByDueDesc() = sortTasks(Task.dueComparator.reversed())

    @FXML
    private fun onTaskEdit() {
        launchTaskEditor(taskView!!.selectionModel.selectedItem.task, false)
    }

    @FXML
    private fun onTaskUndo() {
        ApplicationData.taskList = undoQueue.performUndo()
        repopulateTaskView()
        Persistence.save(ApplicationData.taskList, ApplicationData.prefs.localSaveLocation)
        Persistence.Cloud.put(ApplicationData.taskList, ApplicationData.prefs.cloudSaveLocation)
    }

    @FXML
    private fun onTaskRedo() {
        ApplicationData.taskList = undoQueue.performRedo()
        repopulateTaskView()
        Persistence.save(ApplicationData.taskList, ApplicationData.prefs.localSaveLocation)
        Persistence.Cloud.put(ApplicationData.taskList, ApplicationData.prefs.cloudSaveLocation)
    }

    @FXML
    private fun onTaskCut(){
        onTaskCopy()
        onTaskDelete()
    }

    @FXML
    private fun onTaskCopy(){
        clipboard = taskView?.selectionModel?.selectedItem?.task?.toString()
    }

    @FXML
    private fun onTaskPaste(){
        if(clipboard != null) {
            val taskToPaste = Task.parse(clipboard as String)
            ApplicationData.taskList.add(taskToPaste)
            addToTaskView(taskToPaste)
            taskView!!.selectionModel.select(taskView!!.items.size - 1)
            Persistence.save(ApplicationData.taskList, ApplicationData.prefs.localSaveLocation)
            Persistence.Cloud.put(ApplicationData.taskList, ApplicationData.prefs.cloudSaveLocation)
            undoQueue.registerNewState(ApplicationData.taskList)
        }
    }

    @FXML
    private fun onTaskToggleCompleted(){
        val selectedTask = taskView!!.selectionModel.selectedItem.task
        selectedTask.completed = !selectedTask.completed
        refreshTaskView()
        Persistence.save(ApplicationData.taskList, ApplicationData.prefs.localSaveLocation)
        Persistence.Cloud.put(ApplicationData.taskList, ApplicationData.prefs.cloudSaveLocation)
        undoQueue.registerNewState(ApplicationData.taskList)
    }

    @FXML
    private fun onTaskToggleProgress(){
        val selectedTask = taskView!!.selectionModel.selectedItem.task
        selectedTask.inProgress = !selectedTask.inProgress
        refreshTaskView()
        Persistence.save(ApplicationData.taskList, ApplicationData.prefs.localSaveLocation)
        Persistence.Cloud.put(ApplicationData.taskList, ApplicationData.prefs.cloudSaveLocation)
        undoQueue.registerNewState(ApplicationData.taskList)
    }

    @FXML
    private fun swapSelectedTask(numPlaces: Int) {
        val selectedTaskIndex = taskView!!.selectionModel.selectedIndex
        val destinationTaskIndex = selectedTaskIndex + numPlaces
        if(destinationTaskIndex in 0 until taskView!!.items.size){
            val selectedTask = taskView!!.items[selectedTaskIndex].task
            val destinationTask = taskView!!.items[destinationTaskIndex].task

            val listIndexOfSelectedTask = ApplicationData.taskList.indexOfFirst{ it === selectedTask }
            val listIndexOfDestinationTask = ApplicationData.taskList.indexOfFirst{ it === destinationTask }

            // Swaps the tasks in the application data task list
            ApplicationData.taskList[listIndexOfSelectedTask] =
                ApplicationData.taskList[listIndexOfDestinationTask].also{
                    ApplicationData.taskList[listIndexOfDestinationTask] =
                        ApplicationData.taskList[listIndexOfSelectedTask]
                }

            // Swaps the tasks in the task list view
            taskView!!.items[selectedTaskIndex] = taskView!!.items[destinationTaskIndex].also{
                taskView!!.items[destinationTaskIndex] = taskView!!.items[selectedTaskIndex]
            }
            // Selects the swapped task
            taskView!!.selectionModel.select(destinationTaskIndex)

            Persistence.save(ApplicationData.taskList, ApplicationData.prefs.localSaveLocation)
            Persistence.Cloud.put(ApplicationData.taskList, ApplicationData.prefs.cloudSaveLocation)
            undoQueue.registerNewState(ApplicationData.taskList)
        }
    }

    @FXML
    private fun onTaskMoveUp() = swapSelectedTask(-1)

    @FXML
    private fun onTaskMoveDown() = swapSelectedTask(1)

    @FXML
    private fun onTaskDelete() {
        val selectedTask = taskView?.selectionModel?.selectedItem?.task
        ApplicationData.taskList.removeAll{ it === selectedTask }
        taskView?.items?.removeAll{ it.task === selectedTask }
        Persistence.save(ApplicationData.taskList, ApplicationData.prefs.localSaveLocation)
        Persistence.Cloud.put(ApplicationData.taskList, ApplicationData.prefs.cloudSaveLocation)
        undoQueue.registerNewState(ApplicationData.taskList)
    }

    @FXML
    private fun onKeyPressed(event: KeyEvent) {
        val new: KeyCode = KeyCode.N
        val edit: KeyCode = KeyCode.E
        val undo: KeyCode = KeyCode.Z
        val redo: KeyCode = KeyCode.Y
        val cut: KeyCode = KeyCode.X
        val copy: KeyCode = KeyCode.C
        val paste: KeyCode = KeyCode.V
        val moveUp: KeyCode = KeyCode.Q // KeyCode.UP
        val moveDown: KeyCode = KeyCode.A // KeyCode.DOWN
        val delete: KeyCode = KeyCode.BACK_SPACE
        val prefs: KeyCode = KeyCode.COMMA
        if(event.isControlDown){
            when(event.code){
                new -> onCreateTaskButtonClick()
                edit -> onTaskEdit()
                undo -> if(event.isShiftDown){ onTaskRedo() }else{ onTaskUndo() }
                redo -> onTaskRedo()
                cut -> onTaskCut()
                copy -> onTaskCopy()
                paste -> onTaskPaste()
                moveUp -> if(event.isShiftDown){ onTaskMoveUp() }
                moveDown -> if(event.isShiftDown){ onTaskMoveDown() }
                delete -> onTaskDelete()
                prefs -> onPreferences()
                else -> {}
            }
        }
    }

    @FXML
    private lateinit var sideTitle: Label

    @FXML
    private lateinit var sideDescription: Label

    @FXML
    private lateinit var sideDate: Label

    private var offset = 0

    @FXML
    private fun displayTaskInSidebar(t: Task?){

        sideTitle.textProperty().bind(SimpleStringProperty(""))
        sideDate.text = ""
        sideDescription.text = ""

        if(t != null) {

            offset = 0 - (sideTitle.height.toInt() - 30)

            sideTitle.textProperty().bind(SimpleStringProperty(TaskViewer(t).toString()))

            val dueDate = t.due?.atZone(ZoneId.systemDefault())?.toLocalDate()

            if (dueDate != null) {
                sideDate.layoutY = 55.0 - offset
                sideDate.text = "Due: $dueDate"
            } else {
                sideDate.text = ""
                offset += 30
            }

            sideDescription.text += "Description: \n" + t.description

            if (t.labels.isNotEmpty()) {
                sideDescription.text +=
                     "\n\nLabels: \n" +
                            t.labels.joinToString("\n")
            }

            AnchorPane.setTopAnchor(sideDescription, 88.0 - offset)

            offset = 0

        }
    }

    @FXML
    private fun displaySelectedTaskInSidebar() {
        displayTaskInSidebar(taskView?.selectionModel?.selectedItem?.task)
    }

    @FXML
    private fun onAbout() {
        val stage = Stage()
        val fxmlLoader = FXMLLoader(IntentionsApplication::class.java.getResource("instructions.fxml"))
        val scene = Scene(fxmlLoader.load(), 470.0, 500.0)
        stage.scene = scene
        stage.scene.stylesheets.add(IntentionsApplication::class.java.getResource("/Stylesheets/instructions.css").toExternalForm())

        stage.show()
    }

    @FXML
    private fun onQuit() {
        exitProcess(0)
    }
}
