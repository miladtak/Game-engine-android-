package com.persiangulf.gameengine.engine

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.view.MotionEvent
import android.view.SurfaceHolder
import android.view.SurfaceView
import com.persiangulf.gameengine.model.GameObject
import com.persiangulf.gameengine.script.ScriptParser

class GameEngineView(context: Context, private val objects: MutableList<GameObject>) : SurfaceView(context), Runnable, SurfaceHolder.Callback {
    private var thread: Thread? = null
    private var running = false
    private val paint = Paint()
    private val scriptParser = ScriptParser()
    private var player: GameObject? = objects.find { it.type == "player" } ?: objects.firstOrNull()
    private var playerVelocityY = 0f
    private val gravity = 850f
    var isGrounded = false

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

    override fun run() {
        var lastTime = System.nanoTime()
        while (running) {
            val now = System.nanoTime()
            val dt = (now - lastTime) / 1000000000f
            lastTime = now
            if (dt < 0.1f) { update(dt); draw() }
            try { Thread.sleep(16) } catch (e: InterruptedException) { e.printStackTrace() }
        }
    }

    private fun update(dt: Float) {
        player?.let { p ->
            if (!isGrounded) playerVelocityY += gravity * dt
            p.y += playerVelocityY * dt
            isGrounded = false

            for (obj in objects) {
                if (obj.type == "platform" || obj.type == "box_3d") {
                    if (p.x < obj.x + obj.width && p.x + p.width > obj.x &&
                        p.y + p.height >= obj.y && p.y + p.height <= obj.y + 30f && playerVelocityY > 0) {
                        p.y = obj.y - p.height
                        playerVelocityY = 0f
                        isGrounded = true
                    }
                }
                scriptParser.execute(obj)
            }
        }
    }

    private fun draw() {
        if (!holder.surface.isValid) return
        val canvas: Canvas = holder.lockCanvas()
        canvas.drawColor(Color.parseColor("#090d16"))
        val sortedObjects = objects.sortedWith(compareBy({ it.z }, { it.y }))
        for (obj in sortedObjects) {
            paint.color = Color.parseColor(if (obj.color.startsWith("#")) obj.color else "#38bdf8")
            if (obj.is3D || obj.type == "box_3d") {
                val dOff = obj.depth * 0.35f
                paint.alpha = 120
                canvas.drawRect(obj.x + dOff, obj.y - dOff, obj.x + obj.width + dOff, obj.y + obj.height - dOff, paint)
                paint.alpha = 255
                canvas.drawRect(obj.x, obj.y, obj.x + obj.width, obj.y + obj.height, paint)
            } else {
                paint.alpha = 255
                canvas.drawRect(obj.x, obj.y, obj.x + obj.width, obj.y + obj.height, paint)
            }
        }
        holder.unlockCanvasAndPost(canvas)
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (event.action == MotionEvent.ACTION_DOWN && isGrounded) {
            playerVelocityY = -680f
            isGrounded = false
        }
        return true
    }
}
