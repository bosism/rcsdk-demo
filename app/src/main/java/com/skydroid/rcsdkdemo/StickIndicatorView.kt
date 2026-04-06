package com.skydroid.rcsdkdemo

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.util.AttributeSet
import android.view.View
import kotlin.math.min

class StickIndicatorView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
) : View(context, attrs) {

    private val density = resources.displayMetrics.density
    private val framePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#506A85")
        style = Paint.Style.STROKE
        strokeWidth = 2f * density
    }
    private val crosshairPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#9DB1C5")
        style = Paint.Style.STROKE
        strokeWidth = density
    }
    private val dotPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#1976D2")
        style = Paint.Style.FILL
    }
    private val fillPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#EAF2FB")
        style = Paint.Style.FILL
    }

    private var horizontalValue = 0
    private var verticalValue = 0

    fun setAxes(horizontal: Int, vertical: Int) {
        horizontalValue = horizontal.coerceIn(-1000, 1000)
        verticalValue = vertical.coerceIn(-1000, 1000)
        invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val size = min(width, height).toFloat()
        val left = (width - size) / 2f
        val top = (height - size) / 2f
        val rect = RectF(left, top, left + size, top + size)
        val padding = 10f * density
        val dotRadius = 10f * density
        val content = RectF(
            rect.left + padding,
            rect.top + padding,
            rect.right - padding,
            rect.bottom - padding,
        )

        canvas.drawRoundRect(rect, 12f * density, 12f * density, fillPaint)
        canvas.drawRoundRect(rect, 12f * density, 12f * density, framePaint)

        val centerX = rect.centerX()
        val centerY = rect.centerY()
        canvas.drawLine(content.left, centerY, content.right, centerY, crosshairPaint)
        canvas.drawLine(centerX, content.top, centerX, content.bottom, crosshairPaint)

        val dotX = centerX + (horizontalValue / 1000f) * (content.width() / 2f)
        val dotY = centerY - (verticalValue / 1000f) * (content.height() / 2f)
        canvas.drawCircle(dotX, dotY, dotRadius, dotPaint)
    }
}
