package ru.netology.customviews.ui

import android.animation.AnimatorSet
import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.PointF
import android.graphics.RectF
import android.util.AttributeSet
import android.view.View
import android.view.animation.LinearInterpolator
import androidx.core.content.withStyledAttributes
import ru.netology.customviews.R
import ru.netology.customviews.utils.AndroidUtils
import java.lang.Integer.min
import kotlin.random.Random

class StatsView @JvmOverloads constructor(
    context: Context,
    attributeSet: AttributeSet? = null,
    defStyleAttr: Int = 0,
    defStyleRes: Int = 0,
) : View(
    context,
    attributeSet,
    defStyleAttr,
    defStyleRes,
) {

    private var textSize = AndroidUtils.dp(context, 20).toFloat()
    private var lineWidth = AndroidUtils.dp(context, 5)
    private var colors = emptyList<Int>()
    private var attributes: Int = 0

    init {
        context.withStyledAttributes(attributeSet, R.styleable.StatsView) {
            textSize = getDimension(R.styleable.StatsView_textSize, textSize)
            lineWidth = getDimension(R.styleable.StatsView_lineWidth, lineWidth.toFloat()).toInt()
            attributes = getInt(R.styleable.StatsView_attributes, 1)
            colors = listOf(
                getColor(R.styleable.StatsView_color1, generateRandomColor()),
                getColor(R.styleable.StatsView_color2, generateRandomColor()),
                getColor(R.styleable.StatsView_color3, generateRandomColor()),
                getColor(R.styleable.StatsView_color4, generateRandomColor()),
            )
        }
    }

    var data: List<Float> = emptyList()
        set(value) {
            field = value
            update()
        }

    private var progress: MutableList<Float> = mutableListOf(0F)

    private var radius = 0F
    private var center = PointF()
    private var oval = RectF()
    private val paint = Paint(
        Paint.ANTI_ALIAS_FLAG
    ).apply {
        strokeWidth = lineWidth.toFloat()
        style = Paint.Style.STROKE
        strokeJoin = Paint.Join.ROUND
        strokeCap = Paint.Cap.ROUND
    }
    private val textPaint = Paint(
        Paint.ANTI_ALIAS_FLAG
    ).apply {
        textSize = this@StatsView.textSize
        style = Paint.Style.FILL
        textAlign = Paint.Align.CENTER
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        radius = min(w, h) / 2F - lineWidth
        center = PointF(w / 2F, h / 2F)
        oval = RectF(
            center.x - radius,
            center.y - radius,
            center.x + radius,
            center.y + radius,
        )
    }

    override fun onDraw(canvas: Canvas) {
        if (data.isEmpty()) {
            return
        }

        var startAngle = -90F
        data.forEachIndexed { index, datum ->
            val angle = datum / data.sum() * 360F * data.size / 4
            paint.color = colors.getOrElse(index) { generateRandomColor() }
            when (attributes) {
                0 -> {
                    canvas.drawArc(
                        oval,
                        startAngle + (progress[0] * 360),
                        angle * progress[0],
                        false,
                        paint
                    )
                    startAngle += angle
                }
                1 -> {
                    canvas.drawArc(
                        oval,
                        startAngle,
                        angle * progress[index],
                        false,
                        paint
                    )
                    startAngle += angle
                }
                2 -> {
                    canvas.drawArc(
                        oval,
                        startAngle - 45F,
                        -angle * progress[0] / 2,
                        false,
                        paint
                    )
                    canvas.drawArc(
                        oval,
                        startAngle - 45F,
                        angle * progress[0] / 2,
                        false,
                        paint
                    )
                    startAngle += angle
                }
            }
        }

        canvas.drawText(
            "%.2f%%".format(data[0] / (data[0] * 4) * data.size * 100),
            center.x,
            center.y + textPaint.textSize / 4,
            textPaint
        )
    }

    private fun update() {

        when (attributes) {
            0 -> rotationBidirectional()
            1 -> sequential()
            2 -> rotationBidirectional()
        }
    }

    @SuppressLint("Recycle")
    private fun rotationBidirectional() {
        val valueAnimator: ValueAnimator? = null
        valueAnimator?.let {
            it.removeAllListeners()
            it.cancel()
        }
        ValueAnimator.ofFloat(0F, 1F).apply {
            addUpdateListener { anim ->
                progress[0] = anim.animatedValue as Float
                invalidate()
            }
            duration = 2000
            interpolator = LinearInterpolator()
        }.also {
            it.start()
        }
    }

    private fun sequential() {
        progress = (0..data.count()).map { 0F } as MutableList<Float>
        var valueAnimator: List<ValueAnimator> = emptyList()
        valueAnimator.forEach {
            it.removeAllListeners()
            it.cancel()
        }
        valueAnimator = (0..data.count()).map {
            ValueAnimator.ofFloat(0F, 1F).apply {
                addUpdateListener { anim ->
                    progress[it] = anim.animatedValue as Float
                    invalidate()
                }
                duration = 1000
                interpolator = LinearInterpolator()
            }
        }
        AnimatorSet().apply {
            playSequentially(valueAnimator)
        }.start()
    }

    private fun generateRandomColor() = Random.nextInt(0xFF000000.toInt(), 0xFFFFFFFF.toInt())
}