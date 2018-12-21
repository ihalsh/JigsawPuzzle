package com.mygdx.game.Actors

import com.badlogic.gdx.graphics.g2d.Animation
import com.badlogic.gdx.graphics.g2d.TextureRegion
import com.badlogic.gdx.scenes.scene2d.Stage
import com.mygdx.game.Utils.Assets.borderAnimation

class PuzzleArea(x: Float, y: Float, s: Stage, var row: Int = 0, var col: Int = 0,
                 animation: Animation<TextureRegion> = borderAnimation)
    : DropTargetActor(x, y, s, animation = animation) {

}