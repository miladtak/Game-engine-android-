package com.persiangulf.gameengine

import android.os.Bundle
import android.widget.FrameLayout
import androidx.appcompat.app.AppCompatActivity
import com.persiangulf.gameengine.engine.GameEngineView
import com.persiangulf.gameengine.model.GameObject
import com.persiangulf.gameengine.storage.ProjectStorage

class MainActivity : AppCompatActivity() {
    private lateinit var storage: ProjectStorage
    private val gameObjects = mutableListOf<GameObject>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        storage = ProjectStorage(this)
        
        // بارگذاری پروژه یا ساخت نمونه پیش‌فرض اگر خالی باشد
        val loaded = storage.loadProject()
        if (loaded.isEmpty()) {
            gameObjects.add(GameObject("1", "Player", "player", false, 100f, 400f, 0f, 60f, 60f, 40f, "#38bdf8", 200f, 600f, ""))
            gameObjects.add(GameObject("2", "Ground", "platform", true, 0f, 700f, 0f, 1080f, 100f, 50f, "#1e293b", 0f, 0f, ""))
            storage.saveProject(gameObjects)
        } else {
            gameObjects.addAll(loaded)
        }

        // اجرای موتور بازی روی صفحه
        val engineView = GameEngineView(this, gameObjects)
        setContentView(engineView)
    }

    override fun onPause() {
        super.onPause()
        storage.saveProject(gameObjects)
    }
}
