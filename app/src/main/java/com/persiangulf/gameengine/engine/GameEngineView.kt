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
    
    private var player: GameObject? = objects.find { it.type == "player" }
    private var playerVelocityY = 0f
    private val gravity = 800f
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
            if (!isGrounded) {
                playerVelocityY += gravity * dt
            }
            p.y += playerVelocityY * dt

            // فیزیک برخورد با سکوهای دوبعدی و سه‌بعدی
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
        
        // پس‌زمینه محیط بازی
        canvas.drawColor(Color.parseColor("#0f172a"))

        // مرتب‌سازی المان‌ها بر اساس عمق (برای نمایش صحیح سه‌بعدی و ایزومتریک)
        val sortedObjects = objects.sortedWith(compareBy({ it.z }, { it.y }))

        for (obj in sortedObjects) {
            paint.color = Color.parseColor(if (obj.color.startsWith("#")) obj.color else "#38bdf8")
            
            if (obj.is3D) {
                // شبیه‌سازی سه‌بعدی و جعبه‌ای با ترسیم لایه‌های مکعبی
                val depthOffset = obj.depth * 0.4f
                
                // سایه و وجه پایینی مکعب سه‌بعدی
                paint.alpha = 150
                canvas.drawRect(obj.x + depthOffset, obj.y - depthOffset, obj.x + obj.width + depthOffset, obj.y + obj.height - depthOffset, paint)
                
                // وجه اصلی مکعب سه‌بعدی
                paint.alpha = 255
                canvas.drawRect(obj.x, obj.y, obj.x + obj.width, obj.y + obj.height, paint)
            } else {
                // المان استاندارد دوبعدی
                paint.alpha = 255
                canvas.drawRect(obj.x, obj.y, obj.x + obj.width, obj.y + obj.height, paint)
            }
        }

        holder.unlockCanvasAndPost(canvas)
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (event.action == MotionEvent.ACTION_DOWN) {
            if (isGrounded) {
                playerVelocityY = -650f
                isGrounded = false
            }
        }
        return true
    }
}
