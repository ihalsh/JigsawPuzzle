package com.mygdx.game

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.InputMultiplexer
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.Animation
import com.badlogic.gdx.graphics.g2d.Animation.PlayMode.LOOP
import com.badlogic.gdx.graphics.g2d.TextureRegion
import com.badlogic.gdx.math.MathUtils.random
import com.badlogic.gdx.scenes.scene2d.Stage
import com.badlogic.gdx.scenes.scene2d.actions.Actions
import com.badlogic.gdx.scenes.scene2d.ui.Label
import com.badlogic.gdx.scenes.scene2d.ui.Table
import com.badlogic.gdx.utils.Scaling
import com.badlogic.gdx.utils.viewport.ScalingViewport
import com.mygdx.game.Actors.BaseActor
import com.mygdx.game.Actors.BaseActor.Companion.setWorldBounds
import com.mygdx.game.Actors.PuzzleArea
import com.mygdx.game.Actors.PuzzlePiece
import com.mygdx.game.Utils.Assets.assetManager
import com.mygdx.game.Utils.Assets.backgroundAnimation
import com.mygdx.game.Utils.Assets.labelStyle
import com.mygdx.game.Utils.Constants.Companion.GAME_OVER_DELAY
import com.mygdx.game.Utils.Constants.Companion.PUZZLE_PIECE
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
        private var endTimer: Float = 0f,
        private var gameOver: Boolean = false) : KtxScreen, KtxInputAdapter {

    //Adds backgroundAnimation
    private val background = BaseActor(0f, 0f, mainStage, backgroundAnimation)

    //Adds message
    private val messageLabel = Label("...", labelStyle).apply {
        color = Color.CYAN
        isVisible = false
        uiTable.add(this).expandX().expandY().bottom().pad(50f)
    }

    override fun show() {

        //Handle input from everywhere
        val im = Gdx.input.inputProcessor as InputMultiplexer
        im.addProcessor(this)
        im.addProcessor(uiStage)
        im.addProcessor(mainStage)

        //world bounds
        setWorldBounds(baseActor = background)

        //load images into PuzzlePiece objects randomly positioned on the left side of the screen
        addPuzzlePieces(2, 2)
    }

    private fun addPuzzlePieces(rows: Int, cols: Int) {
        val texture = assetManager.get<Texture>("sun.jpg")
        val imageWidth = texture.width
        val imageHeight = texture.height
        val pieceWidth = imageWidth / cols
        val pieceHeight = imageHeight / rows
        val temp = TextureRegion.split(texture, pieceWidth, pieceHeight)
        for (r in 0 until rows) {
            for (c in 0 until cols) {
                // create puzzle piece at random location on left half of screen
                val animation = Animation<TextureRegion>(1f, temp[r][c])
                        .apply { playMode = LOOP }
                PuzzlePiece(random(0, 400 - pieceWidth).toFloat(),
                        random(0, 600 - pieceHeight).toFloat(),
                        mainStage, animation).apply {
                    row = r
                    col = c
                }

                //set up a grid of PuzzleArea objects on the right side of the screen
                val marginX = (400 - imageWidth) / 2
                val marginY = (600 - imageHeight) / 2
                val areaX = 400 + marginX + pieceWidth * c
                val areaY = 600 - marginY - pieceHeight - pieceHeight * r
                PuzzleArea(areaX.toFloat(), areaY.toFloat(), mainStage).apply {
                    setSize(pieceWidth.toFloat(), pieceHeight.toFloat())
                    setBoundaryPoly()
                    row = r
                    col = c
                }
            }
        }
    }

    private fun restartLevel() {

        endTimer = 0f

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

        //Check if puzzle solved
        var solved = true
        BaseActor.getList(mainStage, PUZZLE_PIECE).forEach { actor ->
            if (!(actor as PuzzlePiece).isCorrectlyPlaced) solved = false
        }
        if (solved) messageLabel.apply {
            setText("Puzzle solved!")
            isVisible = true
        } else messageLabel.apply {
            setText("...")
            isVisible = false
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

