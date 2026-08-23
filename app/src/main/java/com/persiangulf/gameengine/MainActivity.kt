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
            gameObjects.add(GameObject3D("1", "Player", "player", 150f, 500f, 0f, 1f, 1f, 1f, "#38bdf8", "grid", false))
            gameObjects.add(GameObject3D("2", "Enemy_Box", "enemy", 600f, 500f, 0f, 1.2f, 1.2f, 1f, "#ef4444", "solid", true))
            storage.saveProject(gameObjects)
        } else {
            gameObjects.addAll(loaded)
        }

        val mainLayout = FrameLayout(this)
        viewport = EngineViewport(this, gameObjects)
        mainLayout.addView(viewport)

        // نوار ابزار اصلی بالای موتور «پارس»
        val topBar = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            setBackgroundColor(Color.parseColor("#cc0f172a"))
            setPadding(15, 15, 15, 15)
        }

        val btnAdd = Button(this).apply { text = "+ ساخت شیء"; setOnClickListener { addNewObject() } }
        val btnInspect = Button(this).apply { text = "⚙ تنظیمات"; setOnClickListener { openInspector() } }
        val btnFx = Button(this).apply {
            text = "💥 افکت ذرات"
            setOnClickListener {
                viewport.selectedObject?.let { obj -> viewport.spawnExplosion(obj.x + 50f, obj.y + 50f) }
            }
        }

        topBar.addView(btnAdd)
        topBar.addView(btnInspect)
        topBar.addView(btnFx)

        val topParams = FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.WRAP_CONTENT)
        topParams.gravity = Gravity.TOP
        mainLayout.addView(topBar, topParams)

        setContentView(mainLayout)
    }

    private fun addNewObject() {
        val newObj = GameObject3D(
            id = System.currentTimeMillis().toString(),
            name = "Object_${gameObjects.size}",
            type = "cube",
            x = 400f,
            y = 300f,
            colorHex = "#facc15",
            textureStyle = "grid"
        )
        gameObjects.add(newObj)
        storage.saveProject(gameObjects)
        Toast.makeText(this, "شیء جدید در موتور پارس ساخته شد!", Toast.LENGTH_SHORT).show()
    }

    private fun openInspector() {
        val obj = viewport.selectedObject
        if (obj == null) {
            Toast.makeText(this, "لطفاً ابتدا یک شیء را انتخاب کنید!", Toast.LENGTH_SHORT).show()
            return
        }

        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(30, 30, 30, 30)
        }

        val edtColor = EditText(this).apply { hint = "کد رنگ"; setText(obj.colorHex) }
        val chkGravity = CheckBox(this).apply { text = "فعال بودن جاذبه"; isChecked = obj.hasGravity }

        layout.addView(TextView(this).apply { text = "تنظیمات موتور پارس - ${obj.name}" })
        layout.addView(edtColor)
        layout.addView(chkGravity)

        androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("Pars Inspector")
            .setView(layout)
            .setPositiveButton("ذخیره") { _, _ ->
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
