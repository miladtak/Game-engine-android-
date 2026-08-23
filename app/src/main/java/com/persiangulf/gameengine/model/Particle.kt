package com.persiangulf.gameengine.model

data class Particle(
    var x: Float,
    var y: Float,
    var vx: Float,
    var vy: Float,
    var colorHex: String = "#facc15",
    var life: Float = 1.0f
)
