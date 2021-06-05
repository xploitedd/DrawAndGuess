package pt.isel.pdm.drag.game.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Vector2D(val x: Float, val y: Float): Parcelable {

    companion object {

        /**
         * Converts a string to a Vector object
         * @param vectorString the string to be converted in the format (x,y)
         * @return a valid Vector2D object
         */
        fun fromString(vectorString: String): Vector2D {
            val pos = vectorString.substring(1, vectorString.length - 1)
                .split(",")

            val x = pos[0].toFloat()
            val y = pos[1].toFloat()
            return Vector2D(x, y)
        }

    }

    override fun toString() = "($x,$y)"

}

@Parcelize
class DrawingBoard(private var lines: List<List<Vector2D>>) : Iterable<List<Vector2D>>, Parcelable {

    companion object {

        /**
         * Gets a board without lines
         * @return a blank DrawingBoard
         */
        fun getBlankBoard() = DrawingBoard(listOf())

        /**
         * Converts the specified string into a DrawingBoard object
         * @param boardStr the string to convert
         * @return a new DrawingBoard representing the string
         */
        fun boardFromString(boardStr: String) = DrawingBoard(
            boardStr.split(";")
                .filter { it.isNotBlank() }
                .map {
                    it.split(":")
                        .filter { p -> p.isNotBlank() }
                        .map { p -> Vector2D.fromString(p) }
                }
        )

    }

    /**
     * Property used for this object Parcel
     * @see Parcelable
     */
    val boardString get() = toString()

    /**
     * Adds a specified line to the current board
     * @return A new DrawingBoard with the new line and the old DrawingBoard lines
     */
    operator fun plus(line: List<Vector2D>): DrawingBoard {
        val newList = mutableListOf<List<Vector2D>>().apply {
            addAll(lines)
            add(line)
        }

        return DrawingBoard(newList)
    }

    override fun iterator(): Iterator<List<Vector2D>> = lines.iterator()

    override fun toString() = lines.fold(StringBuilder()) { acc, line ->
        val lineStr = line.fold(StringBuilder()) { acc2, point -> acc2.append("$point:") }
            .toString()

        acc.append("$lineStr;")
    }.toString()

}
