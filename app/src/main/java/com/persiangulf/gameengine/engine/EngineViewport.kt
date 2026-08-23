package com.persiangulf.gameengine.engine

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.view.MotionEvent
import android.view.SurfaceHolder
import android.view.SurfaceView
import com.persiangulf.gameengine.model.GameObject3D

class EngineViewport(context: Context, val objects: MutableList<GameObject3D>) : SurfaceView(context), Runnable, SurfaceHolder.Callback {
    private var thread: Thread? = null
    private var running = false
    private val paint = Paint()
    var selectedObject: GameObject3D? = null
    private var lastTouchX = 0f
    private var lastTouchY = 0f
    var isPlayMode = false

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
            if (dt < 0.1f) {
                if (isPlayMode) updatePhysics(dt)
                drawScene()
            }
            try { Thread.sleep(16) } catch (e: InterruptedException) { e.printStackTrace() }
        }
    }

    private fun updatePhysics(dt: Float) {
        for (obj in objects) {
            if (obj.hasGravity && obj.y < 600f) {
                obj.velocityY += 980f * dt * obj.mass
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

        // رسم گرید زمین (Grid Axis)
        paint.color = Color.parseColor("#334155")
        paint.strokeWidth = 3f
        for (i in 0..1080 step 100) canvas.drawLine(i.toFloat(), 0f, i.toFloat(), 2000f, paint)
        for (i in 0..2000 step 100) canvas.drawLine(0f, i.toFloat(), 1080f, i.toFloat(), paint)

        // رسم خط زمین اصلی
        paint.color = Color.parseColor("#22c55e")
        paint.strokeWidth = 6f
        canvas.drawLine(0f, 660f, 1080f, 660f, paint)

        // رندر اشیاء سه‌بعدی با افکت پرسپکتیو ساده
        for (obj in objects) {
            val width = 100f * obj.scaleX
            val height = 100f * obj.scaleY
            val depthOffset = obj.z * 0.4f

            // سایه
            paint.color = Color.parseColor("#000000")
            paint.alpha = 80
            canvas.drawOval(obj.x, 650f, obj.x + width, 670f, paint)

            // بدنه سه‌بعدی
            paint.color = Color.parseColor(if (obj.colorHex.startsWith("#")) obj.colorHex else "#38bdf8")
            paint.alpha = 255
            canvas.drawRect(obj.x, obj.y, obj.x + width, obj.y + height, paint)

            // وجه سه‌بعدی (3D Extrude Effect)
            paint.alpha = 150
            canvas.drawRect(obj.x + depthOffset, obj.y - depthOffset, obj.x + width + depthOffset, obj.y + height - depthOffset, paint)

            // کادر انتخاب (Selection Gizmo)
            if (obj == selectedObject) {
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
                objects.forEach { it.isSelected = (it == selectedObject) }
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
