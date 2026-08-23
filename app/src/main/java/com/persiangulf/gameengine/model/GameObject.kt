package com.persiangulf.gameengine.model

data class GameObject(
    var id: String,
    var name: String,
    var type: String, // 'player', 'platform', 'enemy', 'coin', 'goal'
    var x: Float,
    var y: Float,
    var width: Float,
    var height: Float,
    var color: String,
    var speed: Float = 200f,
    var jumpPower: Float = 500f,
    var health: Int = 3
)
