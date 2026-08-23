package com.persiangulf.gameengine.model

data class GameObject(
    var id: String,
    var name: String,
    var type: String, // 'player', 'platform', 'enemy', 'coin', 'goal', 'box_3d'
    var is3D: Boolean = false, // تشخیص حالت سه‌بعدی یا دوبعدی
    var x: Float,
    var y: Float,
    var z: Float = 0f, // عمق در محیط سه‌بعدی
    var width: Float,
    var height: Float,
    var depth: Float = 50f, // ضخامت برای اشیاء سه‌بعدی
    var rotation: Float = 0f, // زاویه چرخش
    var color: String,
    var speed: Float = 200f,
    var jumpPower: Float = 500f,
    var health: Int = 3
)
