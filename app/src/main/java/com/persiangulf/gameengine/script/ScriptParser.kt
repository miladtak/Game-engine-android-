package com.persiangulf.gameengine.script

import com.persiangulf.gameengine.model.GameObject

class ScriptParser {
    fun execute(obj: GameObject) {
        if (obj.script.isEmpty()) return
        val commands = obj.script.lowercase().split(";")
        for (cmd in commands) {
            val parts = cmd.trim().split(" ")
            if (parts.isEmpty()) continue
            when (parts[0]) {
                "move_right" -> obj.x += obj.speed * 0.016f
                "move_left" -> obj.x -= obj.speed * 0.016f
                "move_up" -> obj.y -= obj.speed * 0.016f
                "move_down" -> obj.y += obj.speed * 0.016f
                "set_color" -> { if (parts.size > 1) obj.color = parts[1] }
            }
        }
    }
}
