package com.mygdx.game.Actors

import com.badlogic.gdx.graphics.g2d.Animation
import com.badlogic.gdx.graphics.g2d.TextureRegion
import com.badlogic.gdx.scenes.scene2d.Stage

class PuzzlePiece(x: Float, y: Float, s: Stage, animation: Animation<TextureRegion>,
                  var row: Int = 0,
                  var col: Int = 0,
                  var puzzleArea: PuzzleArea? = null) : DragAndDropActor(x, y, s, animation) {

    val isCorrectlyPlaced: Boolean
        get() = (hasPuzzleArea() && row == puzzleArea!!.row && col == puzzleArea!!.col)

    fun clearPuzzleArea() {
        puzzleArea = null
    }

    fun hasPuzzleArea(): Boolean = puzzleArea != null

    // override methods from DragAndDropActor class
    override fun onDragStart() {
        super.onDragStart()
        if (hasPuzzleArea()) {
            puzzleArea!!.isTargetable = true
            clearPuzzleArea()
        }
    }

    override fun onDrop() {
        super.onDrop()
        if (hasDropTarget()) {
            val pa = getDropTarget() as PuzzleArea
            moveToActor(pa)
            puzzleArea = pa
            pa.isTargetable = false
        }
    }
}