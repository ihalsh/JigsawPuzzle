package com.mygdx.game.Actors

import com.badlogic.gdx.graphics.g2d.Animation
import com.badlogic.gdx.graphics.g2d.TextureRegion
import com.badlogic.gdx.scenes.scene2d.Stage

open class DropTargetActor(x: Float, y: Float, s: Stage, animation: Animation<TextureRegion>)
    : BaseActor(x, y, s, animation) {

    var isTargetable: Boolean = false

    init {
        isTargetable = true
    }
}