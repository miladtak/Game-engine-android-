package com.persiangulf.gameengine.engine

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.media.AudioManager
import android.media.ToneGenerator
import android.view.MotionEvent
import android.view.SurfaceHolder
import android.view.SurfaceView
import com.persiangulf.gameengine.model.Bullet
import com.persiangulf.gameengine.model.GameObject3D

class EngineViewport(context: Context, val objects: MutableList<GameObject3D>) : SurfaceView(context), Runnable, SurfaceHolder.Callback {
    private var thread: Thread? = null
    private var running = false
    private val paint = Paint()
    var selectedObject: GameObject3D? = null
    private var lastTouchX = 0f
    private var lastTouchY = 0f
    var isPlayMode = false
    
    val bullets = mutableListOf<Bullet>()
    private val toneGen = ToneGenerator(AudioManager.STREAM_MUSIC, 80)

    init { holder.addCallback(this) }

    override fun surfaceCreated(holder: SurfaceHolder) {
        running = true
        thread = Thread(this)
        thread?.start()
    }
    override fun surfaceChanged(holder: SurfaceHolder, format: Int, width: Int, height: Int) {}
    override fun surfaceDestroyed(holder: SurfaceHolder) {
        running = false
        try { thread?.join() } catch (e: InterruptedException) { e.printStackTrace() }
    }

    fun shootBullet() {
        val player = objects.find { it.type == "player" } ?: objects.firstOrNull()
        player?.let {
            bullets.add(Bullet(it.x + (100f * it.scaleX), it.y + (50f * it.scaleY)))
            toneGen.startTone(ToneGenerator.TONE_PROP_BEEP, 50)
        }
    }

    override fun run() {
        var lastTime = System.nanoTime()
        while (running) {
            val now = System.nanoTime()
            val dt = (now - lastTime) / 1000000000f
            lastTime = now
            if (dt < 0.1f) {
                if (isPlayMode) updateGameLogic(dt)
                drawScene()
            }
            try { Thread.sleep(16) } catch (e: InterruptedException) { e.printStackTrace() }
        }
    }

    private fun updateGameLogic(dt: Float) {
        // فیزیک گلوله‌ها
        val bIterator = bullets.iterator()
        while (bIterator.hasNext()) {
            val bullet = bIterator.next()
            bullet.x += bullet.speedX * dt
            if (bullet.x > 1200f) {
                bIterator.remove()
                continue
            }

            // برخورد گلوله با اشیاء (Collision Detection)
            for (obj in objects) {
                if (obj.type != "player" && bullet.x >= obj.x && bullet.x <= obj.x + (100f * obj.scaleX) &&
                    bullet.y >= obj.y && bullet.y <= obj.y + (100f * obj.scaleY)) {
                    obj.health -= 25
                    toneGen.startTone(ToneGenerator.TONE_CDMA_PIP, 40)
                    if (obj.health <= 0) obj.colorHex = "#475569" // تغییر رنگ به علامت نابودی
                    bIterator.remove()
                    break
                }
            }
        }

        // فیزیک جاذبه
        for (obj in objects) {
            if (obj.hasGravity && obj.y < 600f) {
                obj.velocityY += 980f * dt
                obj.y += obj.velocityY * dt
                if (obj.y >= 600f) {
                    obj.y = 600f
                    obj.velocityY = 0f
                }
            }
        }
    }

    private fun drawScene() {
        if (!holder.surface.isValid) return
        val canvas: Canvas = holder.lockCanvas()
        canvas.drawColor(Color.parseColor("#0f172a"))

        // گرید زمین
        paint.color = Color.parseColor("#334155")
        paint.strokeWidth = 2f
        for (i in 0..1080 step 100) canvas.drawLine(i.toFloat(), 0f, i.toFloat(), 2000f, paint)

        // خط زمین
        paint.color = Color.parseColor("#22c55e")
        paint.strokeWidth = 6f
        canvas.drawLine(0f, 660f, 1080f, 660f, paint)

        // رندر گلوله‌ها
        paint.color = Color.parseColor("#ef4444")
        for (bullet in bullets) {
            canvas.drawCircle(bullet.x, bullet.y, bullet.radius, paint)
        }

        // رندر اشیاء
        for (obj in objects) {
            val width = 100f * obj.scaleX
            val height = 100f * obj.scaleY

            // سایه
            paint.color = Color.parseColor("#000000")
            paint.alpha = 80
            canvas.drawOval(obj.x, 650f, obj.x + width, 670f, paint)

            // بدنه
            paint.color = Color.parseColor(if (obj.colorHex.startsWith("#")) obj.colorHex else "#38bdf8")
            paint.alpha = 255
            canvas.drawRect(obj.x, obj.y, obj.x + width, obj.y + height, paint)

            // نوار سلامتی در صورت آسیب
            if (obj.health < 100) {
                paint.color = Color.parseColor("#22c55e")
                canvas.drawRect(obj.x, obj.y - 15f, obj.x + (width * (obj.health / 100f)), obj.y - 5f, paint)
            }

            // کادر انتخاب
            if (obj == selectedObject && !isPlayMode) {
                paint.style = Paint.Style.STROKE
                paint.color = Color.parseColor("#f59e0b")
                paint.strokeWidth = 5f
                canvas.drawRect(obj.x - 10f, obj.y - 10f, obj.x + width + 10f, obj.y + height + 10f, paint)
                paint.style = Paint.Style.FILL
            }
        }
        holder.unlockCanvasAndPost(canvas)
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (isPlayMode) return true
        val x = event.x
        val y = event.y

        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                selectedObject = objects.findLast { obj ->
                    val w = 100f * obj.scaleX
                    val h = 100f * obj.scaleY
                    x >= obj.x && x <= obj.x + w && y >= obj.y && y <= obj.y + h
                }
                lastTouchX = x
                lastTouchY = y
            }
            MotionEvent.ACTION_MOVE -> {
                selectedObject?.let { obj ->
                    obj.x += x - lastTouchX
                    obj.y += y - lastTouchY
                    lastTouchX = x
                    lastTouchY = y
                }
            }
        }
        return true
    }
}
