package com.mygdx.game.Actors

import com.badlogic.gdx.scenes.scene2d.Stage

class DropTargetActor(x: Float, y: Float, s: Stage) : BaseActor(x, y, s) {

    var isTargetable: Boolean = false

    init {
        isTargetable = true
    }
}