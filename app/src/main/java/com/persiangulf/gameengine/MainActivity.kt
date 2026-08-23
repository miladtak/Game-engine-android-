package com.persiangulf.gameengine

import android.graphics.Color
import android.os.Bundle
import android.view.Gravity
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
            gameObjects.add(GameObject3D("1", "Cube_Player", 200f, 300f, 0f, 1f, 1f, 1f, "#38bdf8", true))
            storage.saveProject(gameObjects)
        } else {
            gameObjects.addAll(loaded)
        }

        val mainLayout = FrameLayout(this)
        viewport = EngineViewport(this, gameObjects)
        mainLayout.addView(viewport)

        // پنل کنترل بالای صفحه
        val topBar = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            setBackgroundColor(Color.parseColor("#cc0f172a"))
            setPadding(20, 20, 20, 20)
        }

        val btnAdd = Button(this).apply { text = "+ افزودن شیء"; setOnClickListener { addNewObject() } }
        val btnInspect = Button(this).apply { text = "⚙ تنظیمات شیء"; setOnClickListener { openInspector() } }
        val btnMode = Button(this).apply {
            text = "▶ اجرای فیزیک"
            setOnClickListener {
                viewport.isPlayMode = !viewport.isPlayMode
                text = if (viewport.isPlayMode) "⏸ ویرایش" else "▶ اجرای فیزیک"
            }
        }

        topBar.addView(btnAdd)
        topBar.addView(btnInspect)
        topBar.addView(btnMode)

        val params = FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.WRAP_CONTENT)
        params.gravity = Gravity.TOP
        mainLayout.addView(topBar, params)

        setContentView(mainLayout)
    }

    private fun addNewObject() {
        val newObj = GameObject3D(
            id = System.currentTimeMillis().toString(),
            name = "Object_${gameObjects.size + 1}",
            x = 400f,
            y = 200f,
            colorHex = listOf("#ef4444", "#10b981", "#facc15", "#a855f7").random()
        )
        gameObjects.add(newObj)
        storage.saveProject(gameObjects)
        Toast.makeText(this, "شیء جدید ساخته شد. لمس کنید و بکشید!", Toast.LENGTH_SHORT).show()
    }

    private fun openInspector() {
        val obj = viewport.selectedObject
        if (obj == null) {
            Toast.makeText(this, "لطفاً ابتدا یک شیء را روی صفحه لمس کنید!", Toast.LENGTH_SHORT).show()
            return
        }

        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(40, 40, 40, 40)
        }

        val txtInfo = TextView(this).apply { text = "ویرایش شیء: ${obj.name}"; textSize = 18f }
        val edtColor = EditText(this).apply { hint = "کد رنگ (مثلاً #ff0000)"; setText(obj.colorHex) }
        val chkGravity = CheckBox(this).apply { text = "فعال بودن جاذبه"; isChecked = obj.hasGravity }

        layout.addView(txtInfo)
        layout.addView(edtColor)
        layout.addView(chkGravity)

        androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("Inspector (تنظیمات شیء)")
            .setView(layout)
            .setPositiveButton("ثبت") { _, _ ->
                obj.colorHex = edtColor.text.toString()
                obj.hasGravity = chkGravity.isChecked
                storage.saveProject(gameObjects)
            }
            .setNegativeButton("حذف شیء") { _, _ ->
                gameObjects.remove(obj)
                viewport.selectedObject = null
                storage.saveProject(gameObjects)
            }
            .show()
    }

    override fun onPause() {
        super.onPause()
        storage.saveProject(gameObjects)
    }
}
