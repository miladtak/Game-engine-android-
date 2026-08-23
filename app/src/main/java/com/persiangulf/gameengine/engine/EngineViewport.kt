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
import com.persiangulf.gameengine.model.GameObject3D
import com.persiangulf.gameengine.model.Particle

class EngineViewport(context: Context, val objects: MutableList<GameObject3D>) : SurfaceView(context), Runnable, SurfaceHolder.Callback {
    private var thread: Thread? = null
    private var running = false
    private val paint = Paint()
    var selectedObject: GameObject3D? = null
    private var lastTouchX = 0f
    private var lastTouchY = 0f
    var isPlayMode = false
    
    val particles = mutableListOf<Particle>()
    private val toneGen = ToneGenerator(AudioManager.STREAM_MUSIC, 80)

    // موقعیت نور اصلی در محیط
    var lightX = 300f
    var lightY = 100f

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

    fun spawnExplosion(x: Float, y: Float) {
        for (i in 0..15) {
            particles.add(
                Particle(
                    x, y,
                    (-300..300).random().toFloat(),
                    (-300..300).random().toFloat(),
                    listOf("#facc15", "#ef4444", "#f97316").random()
                )
            )
        }
        toneGen.startTone(ToneGenerator.TONE_PROP_BEEP, 40)
    }

    override fun run() {
        var lastTime = System.nanoTime()
        while (running) {
            val now = System.nanoTime()
            val dt = (now - lastTime) / 1000000000f
            lastTime = now
            if (dt < 0.1f) {
                if (isPlayMode) updateGameLogic(dt)
                updateParticles(dt)
                drawScene()
            }
            try { Thread.sleep(16) } catch (e: InterruptedException) { e.printStackTrace() }
        }
    }

    private fun updateParticles(dt: Float) {
        val pIter = particles.iterator()
        while (pIter.hasNext()) {
            val p = pIter.next()
            p.x += p.vx * dt
            p.y += p.vy * dt
            p.life -= dt * 2f
            if (p.life <= 0f) pIter.remove()
        }
    }

    private fun updateGameLogic(dt: Float) {
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
        canvas.drawColor(Color.parseColor("#090d16"))

        // ۱. رسم نور محیطی (Light Source Glow)
        paint.color = Color.parseColor("#fef08a")
        paint.alpha = 40
        canvas.drawCircle(lightX, lightY, 180f, paint)
        paint.alpha = 255
        canvas.drawCircle(lightX, lightY, 20f, paint)

        // ۲. رسم گرید و زمین
        paint.color = Color.parseColor("#1e293b")
        paint.strokeWidth = 2f
        for (i in 0..1080 step 100) canvas.drawLine(i.toFloat(), 0f, i.toFloat(), 2000f, paint)
        
        paint.color = Color.parseColor("#22c55e")
        paint.strokeWidth = 6f
        canvas.drawLine(0f, 660f, 1080f, 660f, paint)

        // ۳. رندر ذرات (Particle Effects)
        for (p in particles) {
            paint.color = Color.parseColor(p.colorHex)
            paint.alpha = (p.life * 255).toInt().coerceIn(0, 255)
            canvas.drawCircle(p.x, p.y, 8f * p.life, paint)
        }

        // ۴. رندر اشیاء با سایه پویا بر اساس منبع نور
        for (obj in objects) {
            val width = 100f * obj.scaleX
            val height = 100f * obj.scaleY

            // محاسبه زوایای سایه متناسب با موقعیت نور
            val shadowOffsetX = (obj.x - lightX) * 0.2f
            val shadowOffsetY = (650f - lightY) * 0.1f

            // سایه متحرک سه‌بعدی
            paint.color = Color.parseColor("#000000")
            paint.alpha = 90
            canvas.drawOval(obj.x + shadowOffsetX, 650f + shadowOffsetY, obj.x + width + shadowOffsetX, 670f + shadowOffsetY, paint)

            // بدنه شیء
            paint.color = Color.parseColor(if (obj.colorHex.startsWith("#")) obj.colorHex else "#38bdf8")
            paint.alpha = 255
            canvas.drawRect(obj.x, obj.y, obj.x + width, obj.y + height, paint)

            // خطوط تکسچر/بافت
            if (obj.textureStyle == "grid") {
                paint.color = Color.BLACK
                paint.strokeWidth = 3f
                canvas.drawLine(obj.x, obj.y + height / 2, obj.x + width, obj.y + height / 2, paint)
                canvas.drawLine(obj.x + width / 2, obj.y, obj.x + width / 2, obj.y + height, paint)
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
