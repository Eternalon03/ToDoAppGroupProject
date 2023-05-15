package com.example.demo

import javafx.stage.Screen
import kotlinx.serialization.Serializable
import java.nio.file.Paths

@Serializable
class ApplicationPreferences {

    var cloudSaveLocation: String = "https://intentionstododata.blob.core.windows.net/tododata/myblockblob?sv=2021-06-08&ss=bf&srt=co&sp=rwdlaciytfx&se=2023-12-17T23:07:30Z&st=2022-11-17T15:07:30Z&spr=https,http&sig=nq%2FMJZIt4G1tO4q4Bf0%2FXhTkEJ731CvFNX7KB%2BTtywM%3D"

    var localSaveLocation: String = Paths.get("taskList.json").toAbsolutePath().toString()

    var fontSize: Int = 12

    var windowWidth: Double = 900.0

    var windowHeight: Double = 700.0

    var windowX: Double = (Screen.getPrimary().visualBounds.width - windowWidth) / 2.0

    var windowY: Double = (Screen.getPrimary().visualBounds.height - windowHeight) / 2.0

    var editWidth: Double = 640.0

    var editHeight: Double = 280.0

    var editX: Double = (Screen.getPrimary().visualBounds.width - editWidth) / 2.0

    var editY: Double = (Screen.getPrimary().visualBounds.height - editHeight) / 2.0

}
