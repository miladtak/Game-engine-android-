package com.persiangulf.gameengine

import android.graphics.Color
import android.os.Bundle
import android.view.Gravity
import android.view.MotionEvent
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.persiangulf.gameengine.engine.EngineViewport
import com.persiangulf.gameengine.model.GameObject3D
import com.persiangulf.gameengine.storage.ProjectStorage

class MainActivity : AppCompatActivity() {
    private lateinit var storage: ProjectStorage
    private val gameObjects = mutableListOf<GameObject3D>()
    private lateinit var viewport: EngineViewport

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        storage = ProjectStorage(this)
        val loaded = storage.loadProject()
        if (loaded.isEmpty()) {
            gameObjects.add(GameObject3D("1", "SkeletalPlayer", "player", 200f, 500f, 0f, 1f, 1f, 1f, "#38bdf8", "solid", false))
            gameObjects.add(GameObject3D("2", "Monster", "enemy", 700f, 500f, 0f, 1.2f, 1.2f, 1f, "#ef4444", "solid", true))
            storage.saveProject(gameObjects)
        } else {
            gameObjects.addAll(loaded)
        }

        val mainLayout = FrameLayout(this)
        viewport = EngineViewport(this, gameObjects)
        mainLayout.addView(viewport)

        // نوار ابزار اصلی موتور «پارس»
        val topBar = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            setBackgroundColor(Color.parseColor("#cc0f172a"))
            setPadding(15, 15, 15, 15)
        }

        val btnAdd = Button(this).apply { text = "+ دشمن جدید"; setOnClickListener { addNewObject() } }
        val btnMode = Button(this).apply {
            text = "▶ تست بازی"
            setOnClickListener {
                viewport.isPlayMode = !viewport.isPlayMode
                text = if (viewport.isPlayMode) "⏸ حالت ویرایش" else "▶ تست بازی"
            }
        }

        topBar.addView(btnAdd)
        topBar.addView(btnMode)

        val topParams = FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.WRAP_CONTENT)
        topParams.gravity = Gravity.TOP
        mainLayout.addView(topBar, topParams)

        // دکمه‌های حرکت انیمیشنی به چپ و راست
        val controllerLayout = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
        }

        val btnLeft = Button(this).apply { text = "◀ چپ" }
        btnLeft.setOnTouchListener { _, event ->
            if (event.action == MotionEvent.ACTION_MOVE || event.action == MotionEvent.ACTION_DOWN) {
                viewport.movePlayer(-1f)
            } else if (event.action == MotionEvent.ACTION_UP) {
                viewport.stopPlayer()
            }
            true
        }

        val btnRight = Button(this).apply { text = "راست ▶" }
        btnRight.setOnTouchListener { _, event ->
            if (event.action == MotionEvent.ACTION_MOVE || event.action == MotionEvent.ACTION_DOWN) {
                viewport.movePlayer(1f)
            } else if (event.action == MotionEvent.ACTION_UP) {
                viewport.stopPlayer()
            }
            true
        }

        controllerLayout.addView(btnLeft)
        controllerLayout.addView(btnRight)

        val ctrlParams = FrameLayout.LayoutParams(FrameLayout.LayoutParams.WRAP_CONTENT, FrameLayout.LayoutParams.WRAP_CONTENT).apply {
            gravity = Gravity.BOTTOM or Gravity.LEFT
            setMargins(30, 0, 0, 30)
        }
        mainLayout.addView(controllerLayout, ctrlParams)

        setContentView(mainLayout)
    }

    private fun addNewObject() {
        val newObj = GameObject3D(
            id = System.currentTimeMillis().toString(),
            name = "Enemy_${gameObjects.size}",
            type = "enemy",
            x = (500..1200).random().toFloat(),
            y = 500f,
            colorHex = "#ef4444"
        )
        gameObjects.add(newObj)
        storage.saveProject(gameObjects)
        Toast.makeText(this, "دشمن جدید اضافه شد!", Toast.LENGTH_SHORT).show()
    }

    override fun onPause() {
        super.onPause()
        storage.saveProject(gameObjects)
    }
}
