package com.mygdx.game.Utils

import com.mygdx.game.Actors.DropTargetActor

class Constants {

    companion object {
        //General
        const val WORLD_WIDTH = 800f
        const val WORLD_HEIGHT = 600f
        const val GAME_OVER_DELAY = 0.75f

        val DROP_TARGET_ACTOR = DropTargetActor::class.qualifiedName.toString()

    }
}