package com.persiangulf.gameengine.model

data class GameObject(
    var id: String,
    var name: String,
    var type: String,
    var is3D: Boolean = false,
    var x: Float,
    var y: Float,
    var z: Float = 0f,
    var width: Float,
    var height: Float,
    var depth: Float = 40f,
    var color: String = "#38bdf8",
    var speed: Float = 200f,
    var jumpPower: Float = 600f,
    var script: String = ""
)
