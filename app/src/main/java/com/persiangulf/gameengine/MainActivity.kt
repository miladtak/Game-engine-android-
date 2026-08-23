package com.persiangulf.gameengine

import android.graphics.Color
import android.os.Bundle
import android.view.Gravity
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.persiangulf.gameengine.engine.GameEngineView
import com.persiangulf.gameengine.model.GameObject
import com.persiangulf.gameengine.storage.ProjectStorage

class MainActivity : AppCompatActivity() {
    private lateinit var storage: ProjectStorage
    private val gameObjects = mutableListOf<GameObject>()
    private lateinit var rootLayout: FrameLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        storage = ProjectStorage(this)
        val loaded = storage.loadProject()
        if (loaded.isEmpty()) {
            gameObjects.add(GameObject("1", "Player", "player", false, 100f, 300f, 0f, 60f, 60f, 40f, "#38bdf8", 200f, ""))
            gameObjects.add(GameObject("2", "Ground", "platform", true, 0f, 650f, 0f, 1080f, 100f, 50f, "#1e293b", 0f, ""))
            storage.saveProject(gameObjects)
        } else {
            gameObjects.addAll(loaded)
        }

        rootLayout = FrameLayout(this)
        setContentView(rootLayout)
        showEngineUI()
    }

    private fun showEngineUI() {
        rootLayout.removeAllViews()
        val engineView = GameEngineView(this, gameObjects)
        rootLayout.addView(engineView)

        // پنل ابزارهای موتور در بالای صفحه
        val topPanel = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER
            setBackgroundColor(Color.parseColor("#99090d16"))
            setPadding(20, 20, 20, 20)
        }

        val btnAdd = Button(this).textButton("افزودن شیء +") { showAddObjectDialog() }
        val btnPlay = Button(this).textButton("اجرای بازی ▶") { 
            Toast.makeText(this, "روی صفحه ضربه بزنید تا بازیکن بپرد!", Toast.LENGTH_LONG).show()
        }

        topPanel.addView(btnAdd)
        topPanel.addView(btnPlay)
        
        val params = FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.WRAP_CONTENT)
        params.gravity = Gravity.TOP
        rootLayout.addView(topPanel, params)
    }

    private fun showAddObjectDialog() {
        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(40, 40, 40, 40)
        }

        val edtName = EditText(this).apply { hint = "نام شیء (مثل Coin یا Enemy)" }
        val spinnerType = Spinner(this).apply {
            adapter = ArrayAdapter(this@MainActivity, android.R.layout.simple_spinner_dropdown_item, listOf("platform (سکو)", "coin (سکه)", "enemy (دشمن)", "box_3d (جعبه سه‌بعدی)"))
        }
        val edtScript = EditText(this).apply { hint = "اسکریپت (مثل: move_right; set_color #ff0000)" }

        layout.addView(edtName)
        layout.addView(spinnerType)
        layout.addView(edtScript)

        androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("ساخت شیء جدید در موتور")
            .setView(layout)
            .setPositiveButton("ساخت") { _, _ ->
                val name = if (edtName.text.isNotEmpty()) edtName.text.toString() else "Object"
                val typeSel = spinnerType.selectedItem.toString().split(" ")[0]
                val script = edtScript.text.toString()
                
                val newObj = GameObject(
                    id = System.currentTimeMillis().toString(),
                    name = name,
                    type = typeSel,
                    is3D = typeSel == "box_3d",
                    x = (100..400).random().toFloat(),
                    y = 200f,
                    width = 60f,
                    height = 60f,
                    color = if(typeSel == "coin") "#facc15" else if(typeSel == "enemy") "#ef4444" else "#10b981",
                    script = script
                )
                gameObjects.add(newObj)
                storage.saveProject(gameObjects)
                showEngineUI()
                Toast.makeText(this, "شیء با موفقیت اضافه شد!", Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton("لغو", null)
            .show()
    }

    private fun Button.textButton(text: String, onClick: () -> Unit): Button {
        this.text = text
        this.setOnClickListener { onClick() }
        this.setBackgroundColor(Color.parseColor("#38bdf8"))
        this.setTextColor(Color.parseColor("#090d16"))
        val params = LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT)
        params.setMargins(10, 0, 10, 0)
        this.layoutParams = params
        return this
    }

    override fun onPause() {
        super.onPause()
        storage.saveProject(gameObjects)
    }
}
