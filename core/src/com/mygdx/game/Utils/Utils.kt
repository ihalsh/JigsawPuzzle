package com.mygdx.game.Utils

import com.badlogic.gdx.scenes.scene2d.Event
import com.badlogic.gdx.scenes.scene2d.InputEvent

class Utils {

    companion object {
        //check whether a mouse event corresponds to a button click
        fun isTouchDownEvent(e: Event): Boolean = e is InputEvent && e.type == InputEvent.Type.touchDown
    }

}