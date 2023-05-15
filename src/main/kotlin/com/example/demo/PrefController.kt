package com.example.demo

import javafx.fxml.FXML
import javafx.scene.control.Button
import javafx.scene.control.ComboBox
import javafx.stage.FileChooser
import javafx.stage.Stage
import java.nio.file.Path
import java.nio.file.Paths

class PrefController {

    lateinit var currentStage: Stage

    lateinit var mainController: MainAppController

    @FXML
    private val chooseFileButton: Button? = null

    private var selectedFile: Path? = null

    @FXML
    private var textPicker: ComboBox<String>? = ComboBox<String>()

    @FXML
    private val closeButton: Button? = null

    fun initialize () {
        textPicker?.items?.addAll(
            "12px",
            "14px",
            "16px",
            "18px",
            "20px"
        )
    }

    @FXML
    private fun onChooseFileButtonCLick() {

        val chooser = FileChooser()
        chooser.initialFileName = "taskList.json"
        chooser.initialDirectory = Paths.get(ApplicationData.prefs.localSaveLocation).parent.toFile()
        selectedFile = chooser.showSaveDialog(null)?.toPath()?.toAbsolutePath()

    }

    @FXML
    private fun onCloseButtonClick() {

        val fontSize = textPicker?.value

        if (fontSize != null) {
            ApplicationData.prefs.fontSize = fontSize.take(fontSize.length - 2).toInt()

            mainController.setFontSize(ApplicationData.prefs.fontSize)
        }

        if (selectedFile != null) {
            ApplicationData.prefs.localSaveLocation = selectedFile!!.toString()

            Persistence.save(ApplicationData.taskList, ApplicationData.prefs.localSaveLocation)
        }

        Persistence.save(ApplicationData.prefs, ApplicationData.prefSaveLocation)

        currentStage.close()
    }

}
