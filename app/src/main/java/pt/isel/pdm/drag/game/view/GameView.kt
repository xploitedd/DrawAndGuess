package pt.isel.pdm.drag.game.view

import android.content.Context
import android.graphics.Canvas
import android.graphics.CornerPathEffect
import android.graphics.Paint
import android.util.AttributeSet
import android.view.View
import pt.isel.pdm.drag.R
import pt.isel.pdm.drag.game.model.DrawingBoard
import pt.isel.pdm.drag.game.model.Vector2D

class GameView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL_AND_STROKE
        color = resources.getColor(R.color.colorAccent, context.theme)
        strokeWidth = 10f

        // used to improve the line
        strokeJoin = Paint.Join.ROUND
        strokeCap = Paint.Cap.ROUND
        pathEffect = CornerPathEffect(10f)
        isDither = true
    }

    init {
        setLayerType(LAYER_TYPE_HARDWARE, paint)
        setBackgroundColor(resources.getColor(R.color.colorPrimary, context.theme))
    }

    var board: DrawingBoard = DrawingBoard.getBlankBoard()
        set(value) {
            field = value
            invalidate()
        }

    /**
     * Calculate the transformed point coordinates for the current
     * screen size and orientation
     * @param vector2D relative point coordinates in portrait orientation
     * @return the new vector for the player screen
     */
    private fun getTransformedPoint(vector2D: Vector2D): Vector2D {
        val width = this.width
        val height = this.height

        if (width > height)
            return Vector2D(vector2D.y * width, height - vector2D.x * height)

        return Vector2D(vector2D.x * width, vector2D.y * height)
    }

    override fun onDraw(canvas: Canvas?) {
        canvas?.let { can ->
            board.forEach {
                if (it.size == 1) {
                    val point = getTransformedPoint(it[0])
                    can.drawPoint(point.x, point.y, paint)
                } else {
                    for (i in 1 until it.size) {
                        val point1 = getTransformedPoint(it[i - 1])
                        val point2 = getTransformedPoint(it[i])

                        can.drawLine(point1.x, point1.y, point2.x, point2.y, paint)
                    }
                }
            }
        }
    }

}