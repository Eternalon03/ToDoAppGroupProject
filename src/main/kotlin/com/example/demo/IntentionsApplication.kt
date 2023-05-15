package com.example.demo

import javafx.application.Application
import javafx.fxml.FXMLLoader
import javafx.scene.Scene
import javafx.stage.Stage

class IntentionsApplication : Application() {
    override fun start(stage: Stage) {
        val fxmlLoader = FXMLLoader(IntentionsApplication::class.java.getResource("mainApp.fxml"))
        val mainScene = Scene(fxmlLoader.load(), ApplicationData.prefs.windowWidth, ApplicationData.prefs.windowHeight)
        stage.scene = mainScene
        stage.scene.stylesheets.add(IntentionsApplication::class.java.getResource("/Stylesheets/main.css").toExternalForm())

        stage.scene.heightProperty().addListener { _ ->
            ApplicationData.prefs.windowHeight = stage.scene.heightProperty().value
            Persistence.save(ApplicationData.prefs, ApplicationData.prefSaveLocation)
        }

        stage.scene.widthProperty().addListener { _ ->
            ApplicationData.prefs.windowWidth = stage.scene.widthProperty().value
            Persistence.save(ApplicationData.prefs, ApplicationData.prefSaveLocation)
        }

        stage.xProperty().addListener { _ ->
            ApplicationData.prefs.windowX = stage.xProperty().value
            Persistence.save(ApplicationData.prefs, ApplicationData.prefSaveLocation)
        }

        stage.yProperty().addListener { _ ->
            ApplicationData.prefs.windowY = stage.yProperty().value
            Persistence.save(ApplicationData.prefs, ApplicationData.prefSaveLocation)
        }

        stage.x = ApplicationData.prefs.windowX
        stage.y = ApplicationData.prefs.windowY

        stage.show()
    }
}

fun main() {
    Application.launch(IntentionsApplication::class.java)
}
