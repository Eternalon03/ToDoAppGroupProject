package com.example.demo

import java.nio.file.Path
import java.nio.file.Paths

// A simple container object to hold the application data.
// For now there is no encapsulation, though this might change in the future.
object ApplicationData{

    // Preferences are saved in a hard coded location
    val prefSaveLocation: Path = Paths.get("prefs.json").toAbsolutePath()

    var prefs: ApplicationPreferences = Persistence.load(prefSaveLocation)
                                     ?: ApplicationPreferences()
    init{
        Persistence.save(prefs, prefSaveLocation)
    }

    var taskList: MutableList<Task> = Persistence.load(prefs.localSaveLocation)
                                   ?: mutableListOf()
    init{
        taskList = Persistence.Cloud.get(prefs.cloudSaveLocation) ?: taskList
        Persistence.save(taskList, prefs.localSaveLocation)
    }

//    val taskLabels: Set<String>
//        File("taskList.json").readText() = taskList.map{ t -> t.labels }.fold(setOf()){ a, b -> a union b }
//        // get() = taskList.map{ t -> t.labels }.fold(setOf()){ a, b -> a union b }
}
