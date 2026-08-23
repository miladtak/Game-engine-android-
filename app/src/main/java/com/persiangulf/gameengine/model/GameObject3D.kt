package com.persiangulf.gameengine.model

data class GameObject3D(
    var id: String,
    var name: String,
    var x: Float = 0f,
    var y: Float = 0f,
    var z: Float = 0f,
    var scaleX: Float = 1f,
    var scaleY: Float = 1f,
    var scaleZ: Float = 1f,
    var colorHex: String = "#38bdf8",
    var hasGravity: Boolean = true,
    var mass: Float = 1.0f,
    var velocityY: Float = 0f,
    var isSelected: Boolean = false
)
