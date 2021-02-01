/**
 * Author: Chimte.com
 */
package com.lhd.view.materialseekbar

import android.content.Context
import android.graphics.*
import android.graphics.drawable.ColorDrawable
import android.text.TextPaint
import android.util.AttributeSet
import android.view.View
import android.view.ViewConfiguration
import androidx.core.graphics.toRect
import kotlin.math.*

class MaterialSeekBar @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    /**
     * Rect Draw View
     */

    private val rectView = RectF()
    private val rectBackground = RectF()
    private val rectProgress = RectF()

    private val rectThumb = RectF()
    private val rectThumbStroke = RectF()

    private val rectTextIndicator = Rect()

    //===========================================================================

    /**
     *  Paint Draw View
     */

    private val paintThumb = Paint(Paint.ANTI_ALIAS_FLAG)
    private val paintThumbStroke = Paint(Paint.ANTI_ALIAS_FLAG)

    private val paintBackground = Paint(Paint.ANTI_ALIAS_FLAG)
    private val paintProgress = Paint(Paint.ANTI_ALIAS_FLAG)

    private val paintTextIndicator = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
        textAlign = Paint.Align.CENTER
    }

    //===========================================================================

    /**
     * Attributes for Drawing
     */

    private var thumbSize = 0f
    private var thumbStrokeSize = 0f
    private var thumbShadowRadius = 0f

    private var seekBarHeight = 0f
    private var seekBarProgressHeight = 0f
    private var seekBarStrokeSize = 0f
    private var backgroundCornersRadius = 0f
    private var progressCornersRadius = 0f

    private var textIndicatorBottom = 0f

    private var indicatorMode: IndicatorMode = IndicatorMode.ALWAYS_SHOW
    private var indicatorFormat: IndicatorFormat = IndicatorFormat.FLOAT
    private var indicatorScaleFloat = 1 //Number after dot with float value

    //===========================================================================

    /**
     * Attributes progress
     */

    private var progress: Float = 0f
    private var max: Float = 100f

    //===========================================================================

    /**
     * Move value
     */
    var isMovingThumb = false
        private set
    private val touchPointF = PointF()
    private val touchSlop by lazy {
        ViewConfiguration.get(context).scaledTouchSlop
    }

    //===========================================================================

    //region lifecycle

    /**
     * lifecycle
     */

    init {
        setLayerType(View.LAYER_TYPE_SOFTWARE, null);
        attrs?.let {
            val ta = context.obtainStyledAttributes(it, R.styleable.MaterialSeekBar)

            thumbSize = ta.getDimension(R.styleable.MaterialSeekBar_ms_thumb_size, dpToPixel(20f))
            val strokeSize =
                ta.getDimension(R.styleable.MaterialSeekBar_ms_thumb_stroke_size, dpToPixel(0f))
            if (strokeSize > 0) {
                thumbStrokeSize = thumbSize - strokeSize
            }
            //Set color for thumb and it's stroke, but they swap together for matching color
            paintThumb.color =
                ta.getColor(R.styleable.MaterialSeekBar_ms_thumb_stroke_color, Color.RED)
            paintThumbStroke.color =
                ta.getColor(R.styleable.MaterialSeekBar_ms_thumb_color, Color.BLUE)
            thumbShadowRadius =
                ta.getDimension(R.styleable.MaterialSeekBar_ms_thumb_shadow_radius, 0f)
            val thumbShadowColor =
                ta.getColor(R.styleable.MaterialSeekBar_ms_thumb_shadow_color, Color.BLACK)
            if (thumbShadowRadius > 0) {
                paintThumb.setShadowLayer(thumbShadowRadius, 0f, 0f, thumbShadowColor)
            }

            seekBarHeight =
                ta.getDimension(R.styleable.MaterialSeekBar_ms_seek_bar_height, dpToPixel(8f))
            paintBackground.strokeWidth = seekBarHeight
            paintBackground.color =
                ta.getColor(R.styleable.MaterialSeekBar_ms_seek_bar_background_color, Color.GRAY)
            paintBackground.strokeCap = Paint.Cap.ROUND
            seekBarStrokeSize =
                ta.getDimension(R.styleable.MaterialSeekBar_ms_seek_bar_stroke_size, 0f)
            seekBarProgressHeight = seekBarHeight - seekBarStrokeSize
            paintProgress.strokeWidth = seekBarProgressHeight
            paintProgress.color =
                ta.getColor(R.styleable.MaterialSeekBar_ms_seek_bar_progress_color, Color.BLUE)
            paintProgress.strokeCap = Paint.Cap.ROUND

            backgroundCornersRadius =
                ta.getDimension(R.styleable.MaterialSeekBar_ms_seek_bar_corners, 0f)
            progressCornersRadius = ta.getDimension(
                R.styleable.MaterialSeekBar_ms_seek_bar_progress_corners,
                backgroundCornersRadius
            )

            paintTextIndicator.textSize =
                ta.getDimension(R.styleable.MaterialSeekBar_ms_text_indicator_size, 0f)
            paintTextIndicator.color =
                ta.getColor(R.styleable.MaterialSeekBar_ms_text_indicator_color, Color.BLACK)
            textIndicatorBottom =
                ta.getDimension(R.styleable.MaterialSeekBar_ms_text_indicator_bottom, 0f)
            val textIndicatorValue = ta.getInt(
                R.styleable.MaterialSeekBar_ms_indicator_mode,
                IndicatorMode.ALWAYS_SHOW.value
            )
            indicatorMode = when (textIndicatorValue) {
                IndicatorMode.HIDDEN.value -> IndicatorMode.HIDDEN
                IndicatorMode.ONLY_FOCUS.value -> IndicatorMode.ONLY_FOCUS
                else -> IndicatorMode.ALWAYS_SHOW
            }
            val indicatorFormatValue = ta.getInt(
                R.styleable.MaterialSeekBar_ms_text_indicator_format,
                IndicatorFormat.FLOAT.value
            )
            indicatorFormat = when (indicatorFormatValue) {
                IndicatorFormat.INTEGER.value -> IndicatorFormat.INTEGER
                else -> IndicatorFormat.FLOAT
            }
            indicatorScaleFloat =
                ta.getInt(R.styleable.MaterialSeekBar_ms_text_indicator_float_scale_count, 1)
            if (indicatorScaleFloat < 0)
                indicatorScaleFloat = 1

            max = ta.getFloat(R.styleable.MaterialSeekBar_ms_max, 100f)
            progress = ta.getFloat(R.styleable.MaterialSeekBar_ms_progress, 0f)

            ta.recycle()
        }
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val widthSize = MeasureSpec.getSize(widthMeasureSpec)
        var minHeightForVisible = max(
            thumbSize + thumbShadowRadius * 2,
            seekBarHeight
        ).toInt() + paddingTop + paddingBottom
        if (paintTextIndicator.textSize > 0 && indicatorMode != IndicatorMode.HIDDEN) {
            paintTextIndicator.getTextBounds("1", 0, 1, rectTextIndicator)
            minHeightForVisible += textIndicatorBottom.toInt() + rectTextIndicator.height() //* 2 //+ getAdditionalPadding()
        }
        val resultHeight = measureDimension(
            minHeightForVisible,
            heightMeasureSpec
        )
        setMeasuredDimension(widthSize, resultHeight)
    }

    private fun measureDimension(desiredSize: Int, measureSpec: Int): Int {
        var result: Int
        val specMode = MeasureSpec.getMode(measureSpec)
        val specSize = MeasureSpec.getSize(measureSpec)
        if (specMode == MeasureSpec.EXACTLY) {
            result = specSize
        } else {
            result = desiredSize
            if (specMode == MeasureSpec.AT_MOST) {
                result = min(result, specSize)
            }
        }
        return result
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        rectView.set(0f + paddingLeft, 0f + paddingTop, w - paddingRight, h - paddingBottom)
        if (paintTextIndicator.textSize > 0f && indicatorMode != IndicatorMode.HIDDEN) {
            val spaceBottomBar =
                if (thumbSize + thumbShadowRadius * 2 > seekBarHeight) thumbSize / 2f + thumbShadowRadius else seekBarHeight / 2f
            val barCenterY = rectView.bottom - spaceBottomBar
            rectBackground.set(
                rectView.left + thumbSize / 2f,
                barCenterY - seekBarHeight / 2f,
                rectView.right - thumbSize / 2f,
                barCenterY + seekBarHeight / 2f
            )
        } else {
            rectBackground.set(
                rectView.left + thumbSize / 2f,
                rectView.centerY() - seekBarHeight / 2f,
                rectView.right - thumbSize / 2f,
                rectView.centerY() + seekBarHeight / 2f
            )
        }
        rectProgress.set(rectBackground)

        rectThumb.top = rectBackground.centerY() - thumbSize / 2f
        rectThumb.bottom = rectBackground.centerY() + thumbSize / 2f

        rectThumbStroke.top = rectBackground.centerY() - thumbStrokeSize / 2f
        rectThumbStroke.bottom = rectBackground.centerY() + thumbStrokeSize / 2f

        invalidateThumbWithProgress()
        super.onSizeChanged(w, h, oldw, oldh)
    }

    override fun onDraw(canvas: Canvas?) {
        canvas?.let {
            drawView(canvas)
        }
    }

    private fun drawView(canvas: Canvas) {

        canvas.drawLine(
            rectBackground.left,
            rectBackground.centerY(),
            rectBackground.right,
            rectBackground.centerY(), paintBackground
        )
        canvas.drawLine(
            rectProgress.left,
            rectProgress.centerY(),
            rectThumb.centerX(),
            rectProgress.centerY(), paintProgress
        )

        canvas.drawOval(rectThumb, paintThumb)
        if (thumbStrokeSize > 0)
            canvas.drawOval(rectThumbStroke, paintThumbStroke)

        if (indicatorMode != IndicatorMode.HIDDEN) {
            drawCurrentIndicatorValue(canvas)
        }
    }

    private fun drawCurrentIndicatorValue(canvas: Canvas) {
        val stringValue =
            if (indicatorFormat == IndicatorFormat.INTEGER || indicatorScaleFloat == 0) {
                "${progress.toInt()}"
            } else {
                "${progress.scale(indicatorScaleFloat)}"
            }
        paintTextIndicator.getTextBounds(stringValue, 0, stringValue.length - 1, rectTextIndicator)
        canvas.drawText(
            stringValue,
            rectThumb.centerX(),
            rectThumb.top - textIndicatorBottom - thumbShadowRadius,
            paintTextIndicator
        )
    }

    private fun invalidateThumbWithProgress() {
        val centerThumb = progress.ProgressToPixelPosition()
        rectThumb.left = centerThumb - thumbSize / 2f
        rectThumb.right = centerThumb + thumbSize / 2f
        rectThumbStroke.left = centerThumb - thumbStrokeSize / 2f
        rectThumbStroke.right = centerThumb + thumbStrokeSize / 2f
    }

    //endregion

    //region calculating

    private fun getStartViewPixel() = rectBackground.left

    private fun getUsableSeekBarWidth() = rectBackground.width()

    private fun Number.ProgressToPixelPosition(): Float {
        val value = this.toFloat()
        return (value / max) * getUsableSeekBarWidth() + getStartViewPixel()
    }

    //endregion

    //region others

    enum class IndicatorMode(var value: Int) {
        HIDDEN(0), ONLY_FOCUS(1), ALWAYS_SHOW(2)
    }

    enum class IndicatorFormat(var value: Int) {
        INTEGER(0), FLOAT(1)
    }

    //endregion
}