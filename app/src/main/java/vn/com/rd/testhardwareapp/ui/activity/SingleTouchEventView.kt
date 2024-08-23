package vn.com.rd.testhardwareapp.ui.activity

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.VelocityTracker
import android.view.View

class SingleTouchEventView(context: Context?, attrs: AttributeSet?) :
    View(context, attrs) {
    private val paint = Paint()
    private val path: Path = Path()
    private var mVelocityTracker: VelocityTracker? = null
    private val cellSize = 30 // Kích thước của các ô nhỏ
    private val touchedCells: MutableList<Pair<Int, Int>> = mutableListOf() // Lưu các ô đã chạm

    init {
        paint.isAntiAlias = true
        paint.strokeWidth = 3f
        paint.color = Color.BLACK
        paint.style = Paint.Style.STROKE
        paint.strokeJoin = Paint.Join.ROUND
    }

    override fun onDraw(canvas: Canvas) {
        canvas.drawPath(path, paint)
        val width = width
        val height = height

        // Vẽ các ô trên viền và đường chéo
        for (i in 0..width step cellSize) {
            for (j in 0..height step cellSize) {
                if (isBorderCell(i, j, width, height) || isDiagonalCell(i, j, width, height)) {
                    paint.color = if (touchedCells.contains(Pair(i, j))) Color.GREEN else Color.RED
                    canvas.drawRect(i.toFloat(), j.toFloat(), (i + cellSize).toFloat(), (j + cellSize).toFloat(), paint)
                }
            }
        }
    }

    private fun isBorderCell(x: Int, y: Int, width: Int, height: Int): Boolean {
        return (x == 0 || y == 0 || x >= width - cellSize || y >= height - cellSize)
    }

    private fun isDiagonalCell(x: Int, y: Int, width: Int, height: Int): Boolean {
        return (x == y) || (x == width - y)
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        val eventX = event.x
        val eventY = event.y
        val x = (eventX.toInt() / cellSize) * cellSize
        val y = (eventY.toInt() / cellSize) * cellSize

        // Kiểm tra xem ô có nằm trên viền hoặc đường chéo không
        if (isBorderCell(x, y, width, height) || isDiagonalCell(x, y, width, height)) {
            if (!touchedCells.contains(Pair(x, y))) {
                touchedCells.add(Pair(x, y))
                invalidate() // Vẽ lại giao diện
            }
        }

        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                path.moveTo(eventX, eventY)
                mVelocityTracker = VelocityTracker.obtain()
                mVelocityTracker?.addMovement(event)
                return true
            }

            MotionEvent.ACTION_MOVE -> {
                path.lineTo(eventX, eventY)
                mVelocityTracker?.addMovement(event)
            }

            MotionEvent.ACTION_UP -> {
                mVelocityTracker?.apply {
                    addMovement(event)
                    computeCurrentVelocity(1000)
                }
                mVelocityTracker?.recycle()
                mVelocityTracker = null
            }
            else -> return false
        }

        invalidate()
        return true
    }
}
