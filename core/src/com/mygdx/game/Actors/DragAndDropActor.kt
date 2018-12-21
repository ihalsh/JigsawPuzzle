package com.mygdx.game.Actors

import com.badlogic.gdx.math.Interpolation
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.scenes.scene2d.InputEvent
import com.badlogic.gdx.scenes.scene2d.InputListener
import com.badlogic.gdx.scenes.scene2d.Stage
import com.badlogic.gdx.scenes.scene2d.actions.Actions.moveTo
import com.badlogic.gdx.scenes.scene2d.actions.Actions.scaleTo
import com.mygdx.game.Utils.Constants.Companion.DROP_TARGET_ACTOR
import java.lang.Float.MAX_VALUE

/**
 * Enables drag-and-drop functionality for actors.
 */
class DragAndDropActor(x: Float, y: Float, s: Stage, var isDraggable: Boolean = false)
    : BaseActor(x, y, s) {

    private val self = this@DragAndDropActor
    private var grabOffsetY = 0f
    private var grabOffsetX = 0f

    private var dropTarget: DropTargetActor? = null

    //variables to automatically move this actor to the center of another actor,
    // or move the actor to its original position
    private var startPositionX = 0f
    private var startPositionY = 0f

    init {
        addListener(object : InputListener() {
            override fun touchDown(event: InputEvent, offsetX: Float, offsetY: Float,
                                   pointer: Int, button: Int): Boolean {

                with(self) {

                    //cancel the drag action and stop
                    if (!isDraggable) return false

                    startPositionX = self.x
                    startPositionY = self.y

                    grabOffsetX = offsetX
                    grabOffsetY = offsetY
                    toFront()

                    onDragStart()
                }
                return true
            }

            override fun touchDragged(event: InputEvent, offsetX: Float, offsetY: Float,
                                      pointer: Int) {
                with(self) {
                    val deltaX = offsetX - grabOffsetX
                    val deltaY = offsetY - grabOffsetY
                    moveBy(deltaX, deltaY)
                }
            }

            override fun touchUp(event: InputEvent, offsetX: Float, offsetY: Float,
                                 pointer: Int, button: Int) {

                with(self) {

                    setDropTarget(null)

                    // keep track of distance to closest object
                    var closestDistance = MAX_VALUE

                    BaseActor.getList(stage, DROP_TARGET_ACTOR).forEach { actor ->
                        val target = actor as DropTargetActor
                        if (target.isTargetable && overlaps(target)) {
                            val currentDistance = Vector2.dst(self.x, self.y, target.x, target.y)
                            // check if this target is even closer
                            if (currentDistance < closestDistance) {
                                setDropTarget(target)
                                closestDistance = currentDistance
                            }
                        }
                    }
                    onDrop()
                }
            }
        })
    }

    fun hasDropTarget(): Boolean = dropTarget != null

    fun setDropTarget(dt: DropTargetActor?) {
        dropTarget = dt
    }

    fun getDropTarget() = dropTarget

    fun moveToActor(other: BaseActor) {
        val x = other.x + (other.width - width) / 2
        val y = other.y + (other.height - height) / 2
        addAction(moveTo(x, y, 0.50f, Interpolation.pow3))
    }

    fun moveToStart() = addAction(moveTo(startPositionX, startPositionY,
            0.50f,
            Interpolation.pow3))

    fun onDragStart() {
        addAction(scaleTo(1.1f, 1.1f, 0.25f))
    }

    fun onDrop() {
        addAction(scaleTo(1.00f, 1.00f, 0.25f))
    }
}