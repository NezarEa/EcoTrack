package com.ecotrack.Content.Dashbord.Adapter

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.util.AttributeSet
import android.view.View
import androidx.core.content.withStyledAttributes
import com.ecotrack.R

class GaugeView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val rectF = RectF()

    // Default values
    private var centerCircleColor = Color.BLUE
    private var arcColor = Color.BLUE
    private var needleColor = Color.parseColor("#FF000000") // BlueViolet
    private var backgroundColor = Color.parseColor("#FFFFFFFF") // Turquoise

    private var centerCircleRadius = 0f
    private var arcStrokeWidth = 0f
    private var needleLength = 0f
    private var needleStrokeWidth = 0f

    private var startAngle = 180f
    private var sweepAngle = 180f
    private var currentValue = 0f // 0 to 100

    init {
        setBackgroundColor(backgroundColor)

        // Get attributes from XML if provided
        if (attrs != null) {
            context.withStyledAttributes(attrs, R.styleable.GaugeView) {
                centerCircleColor = getColor(R.styleable.GaugeView_centerCircleColor, centerCircleColor)
                arcColor = getColor(R.styleable.GaugeView_arcColor, arcColor)
                needleColor = getColor(R.styleable.GaugeView_needleColor, needleColor)
                backgroundColor = getColor(R.styleable.GaugeView_gaugeBackgroundColor, backgroundColor)
                currentValue = getFloat(R.styleable.GaugeView_initialValue, currentValue)
                startAngle = getFloat(R.styleable.GaugeView_startAngle, startAngle)
                sweepAngle = getFloat(R.styleable.GaugeView_sweepAngle, sweepAngle)
                setBackgroundColor(backgroundColor)
            }
        }
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)

        val minDimension = Math.min(w, h)
        centerCircleRadius = minDimension * 0.15f
        arcStrokeWidth = minDimension * 0.03f
        needleLength = minDimension * 0.35f
        needleStrokeWidth = minDimension * 0.01f

        rectF.set(
            arcStrokeWidth / 2,
            arcStrokeWidth / 2,
            width - arcStrokeWidth / 2,
            height - arcStrokeWidth / 2
        )
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        val centerX = width / 2f
        val centerY = height / 2f

        // Draw the arc
        paint.style = Paint.Style.STROKE
        paint.color = arcColor
        paint.strokeWidth = arcStrokeWidth
        paint.strokeCap = Paint.Cap.ROUND

        // Draw the arc from 180 degrees (9 o'clock) to 0 degrees (3 o'clock)
        canvas.drawArc(rectF, startAngle, sweepAngle, false, paint)

        // Draw the center circle
        paint.style = Paint.Style.FILL
        paint.color = centerCircleColor
        canvas.drawCircle(centerX, centerY, centerCircleRadius, paint)

        // Draw the needle
        paint.style = Paint.Style.STROKE
        paint.color = needleColor
        paint.strokeWidth = needleStrokeWidth

        // Calculate the angle for the needle based on the current value
        val needleAngle = startAngle + (sweepAngle * currentValue / 100f)
        val radian = Math.toRadians(needleAngle.toDouble())
        val endX = centerX + Math.cos(radian) * needleLength
        val endY = centerY + Math.sin(radian) * needleLength

        canvas.drawLine(centerX, centerY, endX.toFloat(), endY.toFloat(), paint)
    }

    /**
     * Set the current value of the gauge (0-100)
     */
    fun setValue(value: Float) {
        currentValue = value.coerceIn(0f, 100f)
        invalidate()
    }

    /**
     * Set the colors of the gauge
     */
    fun setColors(centerCircle: Int, arc: Int, needle: Int, background: Int) {
        centerCircleColor = centerCircle
        arcColor = arc
        needleColor = needle
        backgroundColor = background
        setBackgroundColor(backgroundColor)
        invalidate()
    }
}