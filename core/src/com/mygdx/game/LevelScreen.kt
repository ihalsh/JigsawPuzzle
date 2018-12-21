package com.mygdx.game

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.InputMultiplexer
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.math.MathUtils.lerp
import com.badlogic.gdx.math.MathUtils.random
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.scenes.scene2d.Stage
import com.badlogic.gdx.scenes.scene2d.ui.Label
import com.badlogic.gdx.scenes.scene2d.ui.Table
import com.badlogic.gdx.utils.Scaling
import com.badlogic.gdx.utils.viewport.ScalingViewport
import com.mygdx.game.Actors.*
import com.mygdx.game.Actors.BaseActor.Companion.setWorldBounds
import com.mygdx.game.Utils.Assets.assetManager
import com.mygdx.game.Utils.Assets.backgroundAnimation
import com.mygdx.game.Utils.Assets.backgroundMusic
import com.mygdx.game.Utils.Assets.bounceSound
import com.mygdx.game.Utils.Assets.brickBumpSound
import com.mygdx.game.Utils.Assets.itemAppearSound
import com.mygdx.game.Utils.Assets.itemCollectSound
import com.mygdx.game.Utils.Assets.labelStyle
import com.mygdx.game.Utils.Assets.wallBumpSound
import com.mygdx.game.Utils.Constants.Companion.BRICK
import com.mygdx.game.Utils.Constants.Companion.GAME_OVER_DELAY
import com.mygdx.game.Utils.Constants.Companion.ITEM
import com.mygdx.game.Utils.Constants.Companion.SPAWN_PROBABILITY
import com.mygdx.game.Utils.Constants.Companion.Type.*
import com.mygdx.game.Utils.Constants.Companion.WALL
import com.mygdx.game.Utils.Constants.Companion.WORLD_HEIGHT
import com.mygdx.game.Utils.Constants.Companion.WORLD_WIDTH
import ktx.app.KtxInputAdapter
import ktx.app.KtxScreen
import ktx.app.clearScreen


class LevelScreen(
        private val mainStage: Stage = Stage(ScalingViewport(Scaling.fit, WORLD_WIDTH, WORLD_HEIGHT)),
        private val uiStage: Stage = Stage(ScalingViewport(Scaling.fit, WORLD_WIDTH, WORLD_HEIGHT)),
        private val uiTable: Table = Table().apply {
            setFillParent(true)
            uiStage.addActor(this)
        },
        private var score: Int = 0,
        private var balls: Int = 3,
        private var endTimer: Float = 0f,
        private var gameOver: Boolean = false) : KtxScreen, KtxInputAdapter {

    //Adds backgroundAnimation
    private val background = BaseActor(0f, 0f, mainStage, backgroundAnimation)

    //Adds paddle
    private var paddle = Paddle(320f, 50f, mainStage)

    //Adds ball
    private var ball = Ball(0f, 0f, mainStage)

    //Labels
    private val scoreLabel = Label("Score: $score", labelStyle).apply { setFontScale(0.9f) }
    private val ballsLabel = Label("Balls: $balls", labelStyle).apply { setFontScale(0.9f) }
    private val messageLabel = Label("click to start", labelStyle).apply { color = Color.CYAN }

//    private val restartButton = Button(restartButtonStyle).apply {
//        color = Color.SKY
//        setOrigin(width / 2, height / 2)
//    }

    override fun touchDown(screenX: Int, screenY: Int, pointer: Int, button: Int): Boolean {
        if (ball.isPaused) {
            ball.isPaused = false
            messageLabel.isVisible = false
        }
        return false
    }

    override fun show() {

        //Handle input from everywhere
        val im = Gdx.input.inputProcessor as InputMultiplexer
        im.addProcessor(this)
        im.addProcessor(uiStage)
        im.addProcessor(mainStage)

        //world bounds
        setWorldBounds(baseActor = background)

        //Walls
        Wall(0f, 0f, 20f, 600f, mainStage) // left wall
        Wall(780f, 0f, 20f, 600f, mainStage) // right wall
        Wall(0f, 550f, 800f, 50f, mainStage)// top wall

        //Bricks
        val tempBrick = Brick(0f, 0f, mainStage)
        val brickWidth = tempBrick.width
        val brickHeight = tempBrick.height
        tempBrick.remove()
        val totalRows = 10
        val totalCols = 10
        val marginX = (800 - totalCols * brickWidth) / 2
        val marginY = 600 - totalRows * brickHeight - 120
        for (rowNum in 0 until totalRows) {
            for (colNum in 0 until totalCols) {
                val x = marginX + brickWidth * colNum
                val y = marginY + brickHeight * rowNum
                Brick(x, y, mainStage)
            }
        }

        //Position UI elements
        with(uiTable) {
            pad(5f)
            add(scoreLabel).top()
            add().expandX()
            add(ballsLabel).top()
            row()
            add(messageLabel).colspan(3).expandY()
        }

        //Play music
        with(backgroundMusic) {
            isLooping = true
            volume = 0.5f
            play()
        }
    }

    private fun restartLevel() {

        endTimer = 0f

//        with(mainStage) {
//            clear()
//        }

        //Play music
//        with(backgroundMusic) {
//            stop()
//            play()
    }

    private fun update(delta: Float) {
        //Stops the game
        if (gameOver) {
            endTimer += delta
            if (endTimer > GAME_OVER_DELAY) return
        }

        // update all actors
        uiStage.act(delta)
        mainStage.act(delta)

        //Align paddle with mouse
        with(paddle) {
            val mouseXworld = mainStage.viewport.unproject(Vector2(Gdx.input.x.toFloat(), 0f))
            x = mouseXworld.x - width / 2
            boundToWorld(padX = 20f)
        }

        //Adds ball and lock the ball into place
        if (ball.isPaused) {
            ball.x = paddle.x + paddle.width / 2 - ball.width / 2
            ball.y = paddle.y + paddle.height / 2 + ball.height / 2
        }

        //Bounce off the walls
        BaseActor.getList(mainStage, WALL).forEach { wall ->
            if (ball.overlaps(wall)) {
                ball.bounceOff(wall)
                wallBumpSound.play()
            }
        }

        //Bounce off the bricks
        BaseActor.getList(mainStage, BRICK).forEach { brick ->
            if (ball.overlaps(brick)) {
                ball.bounceOff(brick)
                brickBumpSound.play()
                brick.remove()

                //increase score
                score += 100
                scoreLabel.setText("Score: $score")

                //spawn bonus items
                if (random(0, 100) < SPAWN_PROBABILITY)
                    Item(0f, 0f, mainStage).apply { centerAtActor(brick) }
                itemAppearSound.play()
            }
        }

        //Implement bonus effects when the paddle overlaps the bonus item
        BaseActor.getList(mainStage, ITEM).forEach { item ->
            if (paddle.overlaps(item)) {
                val realItem = item as Item
                when (realItem.type) {
                    PADDLE_EXPAND -> paddle.width = paddle.width * 1.25f
                    PADDLE_SHRINK -> paddle.width = paddle.width * 0.80f
                    BALL_SPEED_UP -> ball.setSpeed(ball.getSpeed() * 1.50f)
                    BALL_SPEED_DOWN -> ball.setSpeed(ball.getSpeed() * 0.90f)
                }
                paddle.setBoundaryPoly()
                item.remove()
                itemCollectSound.play()
            }
        }

        //Bounce off the paddle
        if (ball.overlaps(paddle)) {
            val ballCenterX = ball.x + ball.width / 2
            val paddlePercentHit = (ballCenterX - paddle.x) / paddle.width
            val bounceAngle = lerp(150f, 30f, paddlePercentHit)
            ball.setMotionAngle(bounceAngle)
            bounceSound.play()
        }

        //Win if all bricks destroyed
        if (BaseActor.count(mainStage, BRICK) == 0) {
            messageLabel.apply {
                setText("You win!")
                color = Color.LIME
                isVisible = true
            }
            gameOver = true
        }

        //Respawn the ball and the paddle
        if (ball.y < -50 && BaseActor.count(mainStage, BRICK) > 0) {

            //remove ball
            ball.remove()

            //remove all bonus items
            BaseActor.getList(mainStage, ITEM).forEach { item -> item.remove() }

            //remove paddle
            paddle.apply {
                setPosition(-100f, 0f)
                remove()
            }
            paddle = Paddle(320f, 50f, mainStage)
            if (balls > 0) {
                balls -= 1
                ballsLabel.setText("Balls: $balls")
                ball = Ball(0f, 0f, mainStage)
                messageLabel.apply {
                    setText("Click to start")
                    color = Color.CYAN
                    isVisible = true
                }

            } else {
                messageLabel.apply {
                    setText("Game Over")
                    color = Color.RED
                    isVisible = true
                }
            }
        }
    }

    override fun render(delta: Float) {
        clearScreen(Color.BLACK.r, Color.BLACK.g, Color.BLACK.b)
        update(delta)
        mainStage.draw()
        uiStage.draw()
    }

    override fun resize(width: Int, height: Int) {
        mainStage.viewport.update(width, height)
        uiStage.viewport.update(width, height)
    }

    override fun dispose() {
        mainStage.clear()
        uiStage.clear()
        assetManager.dispose()
    }
}

