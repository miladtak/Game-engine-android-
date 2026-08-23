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
import kotlin.math.sin

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

    var cameraOffsetX = 0f

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

    fun movePlayer(dir: Float) {
        val player = objects.find { it.type == "player" } ?: return
        player.x += dir * 12f
        player.isWalking = true
        player.animFrame += 0.2f
    }

    fun stopPlayer() {
        val player = objects.find { it.type == "player" } ?: return
        player.isWalking = false
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
        val player = objects.find { it.type == "player" }
        player?.let {
            cameraOffsetX = it.x - 400f
        }

        for (obj in objects) {
            if (obj.hasGravity && obj.y < 500f) {
                obj.velocityY += 980f * dt
                obj.y += obj.velocityY * dt
                if (obj.y >= 500f) {
                    obj.y = 500f
                    obj.velocityY = 0f
                }
            }
        }
    }

    private fun drawScene() {
        if (!holder.surface.isValid) return
        val canvas: Canvas = holder.lockCanvas()
        canvas.drawColor(Color.parseColor("#090d16"))

        // ۱. گرید جهان بازی همراه با افست دوربین
        paint.color = Color.parseColor("#1e293b")
        paint.strokeWidth = 2f
        for (i in -1000..3000 step 100) {
            val screenX = i.toFloat() - cameraOffsetX
            canvas.drawLine(screenX, 0f, screenX, 2000f, paint)
        }
        
        paint.color = Color.parseColor("#22c55e")
        paint.strokeWidth = 6f
        canvas.drawLine(-1000f - cameraOffsetX, 600f, 3000f - cameraOffsetX, 600f, paint)

        // ۲. رندر ذرات
        for (p in particles) {
            paint.color = Color.parseColor(p.colorHex)
            paint.alpha = (p.life * 255).toInt().coerceIn(0, 255)
            canvas.drawCircle(p.x - cameraOffsetX, p.y, 8f * p.life, paint)
        }

        // ۳. رندر اشیاء و کاراکتر اسکلتی موتور «پارس»
        for (obj in objects) {
            val renderX = obj.x - cameraOffsetX
            val renderY = obj.y

            if (obj.type == "player") {
                drawSkeletalCharacter(canvas, renderX, renderY, obj)
            } else {
                // رسم اشیاء عادی
                val width = 100f * obj.scaleX
                val height = 100f * obj.scaleY
                paint.color = Color.parseColor(if (obj.colorHex.startsWith("#")) obj.colorHex else "#ef4444")
                paint.alpha = 255
                canvas.drawRect(renderX, renderY, renderX + width, renderY + height, paint)
            }
        }
        holder.unlockCanvasAndPost(canvas)
    }

    private fun drawSkeletalCharacter(canvas: Canvas, x: Float, y: Float, obj: GameObject3D) {
        val legAngle = if (obj.isWalking) sin(obj.animFrame.toDouble()).toFloat() * 30f else 0f
        val armAngle = if (obj.isWalking) sin((obj.animFrame + 3.14f).toDouble()).toFloat() * 25f else 0f

        // ۱. سایه
        paint.color = Color.BLACK
        paint.alpha = 80
        canvas.drawOval(x - 30f, 595f, x + 30f, 610f, paint)

        // ۲. پاها (Legs)
        paint.color = Color.parseColor("#1e293b")
        paint.strokeWidth = 10f
        paint.alpha = 255
        // پای چپ
        canvas.drawLine(x - 10f, y + 40f, x - 10f + legAngle, y + 90f, paint)
        // پای راست
        canvas.drawLine(x + 10f, y + 40f, x + 10f - legAngle, y + 90f, paint)

        // ۳. تنه / بدن (Torso)
        paint.color = Color.parseColor(obj.colorHex)
        canvas.drawRect(x - 20f, y - 20f, x + 20f, y + 40f, paint)

        // ۴. دست‌ها (Arms)
        paint.color = Color.parseColor("#0284c7")
        paint.strokeWidth = 8f
        // دست چپ
        canvas.drawLine(x - 20f, y - 10f, x - 35f - armAngle, y + 25f, paint)
        // دست راست
        canvas.drawLine(x + 20f, y - 10f, x + 35f + armAngle, y + 25f, paint)

        // ۵. سر و چشم‌ها (Head & Eyes)
        paint.color = Color.parseColor("#fde047")
        canvas.drawCircle(x, y - 40f, 20f, paint)
        // چشم‌ها
        paint.color = Color.BLACK
        canvas.drawCircle(x + 8f, y - 43f, 3f, paint)
        canvas.drawCircle(x - 8f, y - 43f, 3f, paint)
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (isPlayMode) return true
        val x = event.x + cameraOffsetX
        val y = event.y

        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                selectedObject = objects.findLast { obj ->
                    x >= obj.x - 50f && x <= obj.x + 50f && y >= obj.y - 50f && y <= obj.y + 50f
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
