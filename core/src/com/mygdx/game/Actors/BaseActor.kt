package com.mygdx.game.Actors

import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.g2d.Animation
import com.badlogic.gdx.graphics.g2d.Batch
import com.badlogic.gdx.graphics.g2d.TextureRegion
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import com.badlogic.gdx.math.*
import com.badlogic.gdx.math.Intersector.MinimumTranslationVector
import com.badlogic.gdx.math.MathUtils.clamp
import com.badlogic.gdx.scenes.scene2d.Group
import com.badlogic.gdx.scenes.scene2d.Stage
import com.mygdx.game.Utils.Constants.Companion.WORLD_HEIGHT
import com.mygdx.game.Utils.Constants.Companion.WORLD_WIDTH

/**
 * Extend functionality of the LibGDX Actor class.
 */
open class BaseActor(x: Float,
                     y: Float,
                     stage: Stage,
                     protected var animation: Animation<TextureRegion>? = null,
                     private var elapsedTime: Float = 0.toFloat(),
                     protected var animationPaused: Boolean = false,
                     protected val velocityVec: Vector2 = Vector2(0f, 0f),
                     private val accelerationVec: Vector2 = Vector2(0f, 0f),
                     private val acceleration: Float = 0f,
                     private val maxSpeed: Float = 3000f,
                     private val deceleration: Float = 0f,
                     private val boundaryPolygon: Polygon = Polygon(),
                     protected val numSides: Int = 4) : Group() {

    init {
        debug = false

        setPosition(x, y)

        // set size and origin
        with(animation!!.getKeyFrame(0f)) {
            setSize(regionWidth.toFloat(), regionHeight.toFloat())
            setOrigin(regionWidth.toFloat() / 2, regionHeight.toFloat() / 2)
        }

        // set collision polygon
        setBoundaryPoly()

        stage.addActor(this)
    }

    companion object {

        //get list of actors
        fun getList(stage: Stage, className: String): ArrayList<BaseActor> {
            val list = ArrayList<BaseActor>()
            var theClass: Class<*>? = null
            try {
                theClass = Class.forName(className)
            } catch (error: Exception) {
                error.printStackTrace()
            }
            for (a in stage.actors) if (theClass!!.isInstance(a)) list.add(a as BaseActor)
            return list
        }

        fun count(stage: Stage, className: String): Int = getList(stage, className).size

        //set world bounds
        val worldBounds = Rectangle(0f, 0f, WORLD_WIDTH, WORLD_HEIGHT)

        fun setWorldBounds(width: Float = WORLD_WIDTH,
                           height: Float = WORLD_HEIGHT,
                           baseActor: BaseActor? = null) {
            worldBounds.set(if (baseActor != null) Rectangle(0f, 0f, baseActor.width, baseActor.height)
            else Rectangle(0f, 0f, width, height))
        }
    }

    fun setBoundaryPoly(w: Float = width, h: Float = height, x: Float = getX(), y: Float = getY()) {
        if (numSides > 4) {
            val vertices = FloatArray(2 * numSides)
            for (i in 0 until numSides) {
                val angle = i * 6.28f / numSides
                // x-coordinate
                vertices[2 * i] = w / 2 * MathUtils.cos(angle) + w / 2
                // y-coordinate
                vertices[2 * i + 1] = h / 2 * MathUtils.sin(angle) + h / 2
            }
            boundaryPolygon.apply {
                setVertices(vertices)
                setPosition(x, y)
            }
        } else {
            val vertices = FloatArray(2 * numSides).apply {
                this[0] = 0f
                this[1] = 0f
                this[2] = w
                this[3] = 0f
                this[4] = w
                this[5] = h
                this[6] = 0f
                this[7] = h
            }
            boundaryPolygon.apply {
                setVertices(vertices)
                setPosition(x, y)
            }
        }
    }

    /**START Movement section*/
    fun setSpeed(speed: Float) {
        // if length is zero, then assume motion angle is zero degrees
        if (velocityVec.len() == 0f)
            velocityVec.set(speed, 0f)
        else
            velocityVec.setLength(speed)
    }

    fun getSpeed(): Float = velocityVec.len()

    fun setMotionAngle(angle: Float) {
        velocityVec.setAngle(angle)
    }

    fun getMotionAngle(): Float = velocityVec.angle()

    fun accelerateAtAngle(angle: Float) {
        accelerationVec.add(Vector2(acceleration, 0f).setAngle(angle))
    }

    fun accelerateForward() = accelerateAtAngle(rotation)

    fun isMoving(): Boolean = getSpeed() > 0

    fun applyPhysics(delta: Float) {
        // apply acceleration
        velocityVec.add(accelerationVec.x * delta, accelerationVec.y * delta)
        var speed = getSpeed()
        // decrease speed (decelerate) when not accelerating
        if (accelerationVec.len() == 0f)
            speed -= deceleration * delta
        // keep speed within set bounds
        speed = clamp(speed, 0f, maxSpeed)
        // update velocity
        setSpeed(speed)
        // apply velocity
        moveBy(velocityVec.x * delta, velocityVec.y * delta)
        // reset acceleration
        accelerationVec.set(0f, 0f)
    }

    fun boundToWorld(padX:Float = 0f, padY:Float = 0f) {
        // check left edge
        if (x < padX) x = padX
        // check right edge
        if (x + Math.max(width, height) + padX > worldBounds.width) x =
                worldBounds.width - Math.max(width, height) - padX
        // check bottom edge
        if (y < padY) y = padY
        // check top edge
        if (y + Math.max(width, height) + padY > worldBounds.height) y =
                worldBounds.height - Math.max(width, height) - padY
    }

    fun wrapAroundWorld() {
        if (x + width < 0)
            x = worldBounds.width
        if (x > worldBounds.width)
            x = -width
        if (y + height < 0)
            y = worldBounds.height
        if (y > worldBounds.height)
            y = -height
    }

    /**END Movement section*/

    fun alignCamera() {
        val cam = stage.camera
        val v = stage.viewport
        // center camera on actor
        cam.position.set(x + originX, y + originY, 0f)
        // bound camera to layout
        cam.position.x = MathUtils.clamp(cam.position.x,
                cam.viewportWidth / 2, worldBounds.width - cam.viewportWidth / 2)
        cam.position.y = MathUtils.clamp(cam.position.y,
                cam.viewportHeight / 2, worldBounds.height - cam.viewportHeight / 2)
        cam.update()
    }

    fun centerAtPosition(x: Float, y: Float) = setPosition(x - width / 2, y - height / 2)

    fun centerAtActor(other: BaseActor) =
            centerAtPosition(other.x + other.width / 2, other.y + other.height / 2)

    fun setOpacity(opacity: Float) {
        this.color.a = opacity
    }

    fun isWithinDistance(distance: Float, other: BaseActor): Boolean {
        val poly1 = getBoundaryPolygon(x, y, originX, originY, rotation, scaleX, scaleY)
        scaleX = (width + 2 * distance) / width
        scaleY = (height + 2 * distance) / height
        poly1.setScale(scaleX, scaleY)
        val poly2 = other.getBoundaryPolygon(other.x, other.y, other.originX, other.originY,
                other.rotation, other.scaleX, other.scaleY)
        // initial test to improve performance
        return if (!poly1.boundingRectangle.overlaps(poly2.boundingRectangle)) false
        else Intersector.overlapConvexPolygons(poly1, poly2)
    }

    fun overlaps(other: BaseActor): Boolean {
        val poly1 = getBoundaryPolygon(x, y, originX, originY, rotation, scaleX, scaleY)
        val poly2 = other.getBoundaryPolygon(other.x, other.y, other.originX, other.originY,
                other.rotation, other.scaleX, other.scaleY)
        // initial test to improve performance
        return if (!poly1.boundingRectangle.overlaps(poly2.boundingRectangle)) false
        else Intersector.overlapConvexPolygons(poly1, poly2)
    }

    fun preventOverlap(other: BaseActor): Vector2? {
        val poly1 = getBoundaryPolygon(x, y, originX, originY, rotation, scaleX, scaleY)
        val poly2 = other.getBoundaryPolygon(other.x, other.y, other.originX, other.originY,
                other.rotation, other.scaleX, other.scaleY)
        // initial test to improve performance
        if (!poly1.boundingRectangle.overlaps(poly2.boundingRectangle)) return null
        val mtv = MinimumTranslationVector()
        if (!Intersector.overlapConvexPolygons(poly1, poly2, mtv)) return null
        moveBy(mtv.normal.x * mtv.depth, mtv.normal.y * mtv.depth)
        return mtv.normal
    }

    private fun getBoundaryPolygon(x: Float, y: Float, originX: Float, originY: Float,
                                   rotation: Float, scaleX: Float, scaleY: Float) = boundaryPolygon
            .apply {
                setPosition(x, y)
                setOrigin(originX, originY)
                this.rotation = rotation
                setScale(scaleX, scaleY)
            }


    fun isAnimationFinished(): Boolean = animation!!.isAnimationFinished(elapsedTime)

    override fun act(delta: Float) {
        super.act(delta)
        if (!animationPaused) elapsedTime += delta
    }

    override fun draw(batch: Batch, parentAlpha: Float) {
        // apply color tint effect
        val c = color
        batch.setColor(c.r, c.g, c.b, c.a)
        if (isVisible) {
            batch.draw(animation!!.getKeyFrame(elapsedTime),
                    x, y, originX, originY,
                    width, height, scaleX, scaleY, rotation)
        }
        super.draw(batch, parentAlpha)
    }

    override fun drawDebug(shapes: ShapeRenderer) {
        super.drawDebugBounds(shapes)
        shapes.set(ShapeRenderer.ShapeType.Line)
        shapes.color = Color.RED
        shapes.polygon(getBoundaryPolygon(x, y, originX, originY, rotation, scaleX, scaleY)
                .transformedVertices)
    }
}