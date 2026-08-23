package com.persiangulf.gameengine.model

data class GameObject(
    var id: String,
    var name: String,
    var type: String, // 'player', 'platform', 'enemy', 'coin', 'box_3d', 'hazard'
    var is3D: Boolean = false,
    var x: Float,
    var y: Float,
    var z: Float = 0f, // عمق سه‌بعدی
    var width: Float,
    var height: Float,
    var depth: Float = 40f,
    var color: String,
    var speed: Float = 200f,
    var jumpPower: Float = 500f,
    var health: Int = 3,
    var customScript: String = "" // اسکریپت سفارشی کاربر برای شیء
)
