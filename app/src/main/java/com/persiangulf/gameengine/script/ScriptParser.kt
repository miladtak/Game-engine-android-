package com.persiangulf.gameengine.script

import com.persiangulf.gameengine.model.GameObject

class ScriptParser {
    fun execute(obj: GameObject, command: String) {
        val parts = command.lowercase().trim().split(" ")
        if (parts.isEmpty()) return

        when (parts[0]) {
            "move_right" -> obj.x += obj.speed * 0.016f
            "move_left" -> obj.x -= obj.speed * 0.016f
            "jump" -> {
                // فرمان پرش سفارشی از طریق اسکریپت
            }
            "set_color" -> {
                if (parts.size > 1) obj.color = parts[1]
            }
        }
    }
}
