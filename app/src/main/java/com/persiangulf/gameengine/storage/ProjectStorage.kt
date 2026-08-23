package com.persiangulf.gameengine.storage

import android.content.Context
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.persiangulf.gameengine.model.GameObject3D
import java.io.File

class ProjectStorage(private val context: Context) {
    private val gson = Gson()
    private val fileName = "pars_engine_3d_project.json"

    fun saveProject(objects: List<GameObject3D>) {
        val json = gson.toJson(objects)
        File(context.filesDir, fileName).writeText(json)
    }

    fun loadProject(): MutableList<GameObject3D> {
        val file = File(context.filesDir, fileName)
        if (!file.exists()) return mutableListOf()
        val type = object : TypeToken<MutableList<GameObject3D>>() {}.type
        return gson.fromJson(file.readText(), type) ?: mutableListOf()
    }
}
