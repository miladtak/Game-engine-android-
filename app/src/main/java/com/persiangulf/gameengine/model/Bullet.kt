package com.persiangulf.gameengine.model

data class Bullet(
    var x: Float,
    var y: Float,
    var speedX: Float = 800f,
    var radius: Float = 12f,
    var isAlive: Boolean = true
)
