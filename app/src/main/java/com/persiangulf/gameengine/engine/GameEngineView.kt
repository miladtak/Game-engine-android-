package com.persiangulf.gameengine.engine

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.view.MotionEvent
import android.view.SurfaceHolder
import android.view.SurfaceView
import com.persiangulf.gameengine.model.GameObject

class GameEngineView(context: Context, private val objects: MutableList<GameObject>) : SurfaceView(context), Runnable, SurfaceHolder.Callback {
    
    private var thread: Thread? = null
    private var running = false
    private val paint = Paint()
    
    private var player: GameObject? = objects.find { type == "player" } || objects.firstOrNull()
    private var playerVelocityY = 0f
    private val gravity = 850f
    private var isGrounded = false

    init {
        holder.addCallback(this)
    }

    override fun surfaceCreated(holder: SurfaceHolder) {
        running = true
        thread = Thread(this)
        thread?.start()
    }

    override fun surfaceChanged(holder: SurfaceHolder, format: Int, width: Int, height: Int) {}

    override fun surfaceDestroyed(holder: SurfaceHolder) {
        var retry = true
        running = false
        while (retry) {
            try {
                thread?.join()
                retry = false
            } catch (e: InterruptedException) {
                e.printStackTrace()
            }
        }
    }

    override fun run() {
        var lastTime = System.nanoTime()
        while (running) {
            val now = System.nanoTime()
            val dt = (now - lastTime) / 1000000000f
            lastTime = now

            if (dt < 0.1f) {
                update(dt)
                draw()
            }
            try {
                Thread.sleep(16)
            } catch (e: InterruptedException) {
                e.printStackTrace()
            }
        }
    }

    private fun update(dt: Float) {
        player?.let { p ->
            // اعمال فیزیک جاذبه
            if (!isGrounded) {
                playerVelocityY += gravity * dt
            }
            p.y += playerVelocityY * dt

            // بررسی برخورد با زمین و سکوها
            isGrounded = false
            for (obj in objects) {
                if (obj.type == "platform" || obj.type == "box_3d") {
                    if (p.x < obj.x + obj.width && p.x + p.width > obj.x &&
                        p.y + p.height >= obj.y && p.y + p.height <= obj.y + 25f && playerVelocityY > 0) {
                        p.y = obj.y - p.height
                        playerVelocityY = 0f
                        isGrounded = true
                    }
                }
            }
        }
    }

    private fun draw() {
        if (!holder.surface.isValid) return
        val canvas: Canvas = holder.lockCanvas()
        
        # رنگ پس‌زمینه محیط بازی (خلیج فارس / دارک فانتزی)
        canvas.drawColor(Color.parseColor("#090d16"))

        // مرتب‌سازی لایه‌ها برای نمایش صحیح دوبعدی و سه‌بعدی (براساس Z و Y)
        val sortedObjects = objects.sortedWith(compareBy({ it.z }, { it.y }))

        for (obj in sortedObjects) {
            paint.color = Color.parseColor(if (obj.color.startsWith("#")) obj.color else "#38bdf8")
            
            if (obj.is3D) {
                // رندر سه‌بعدی جعبه‌ای (مکعب ایزومتریک)
                val dOff = obj.depth * 0.35f
                paint.alpha = 140
                canvas.drawRect(obj.x + dOff, obj.y - dOff, obj.x + obj.width + dOff, obj.y + obj.height - dOff, paint)
                
                paint.alpha = 255
                canvas.drawRect(obj.x, obj.y, obj.x + obj.width, obj.y + obj.height, paint)
            } else {
                // رندر دوبعدی استاندارد
                paint.alpha = 255
                canvas.drawRect(obj.x, obj.y, obj.x + obj.width, obj.y + obj.height, paint)
            }
        }

        holder.unlockCanvasAndPost(canvas)
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (event.action == MotionEvent.ACTION_DOWN) {
            if (isGrounded) {
                playerVelocityY = -680f
                isGrounded = false
            }
        }
        return true
    }
}
