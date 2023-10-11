package com.application.foundation.features.common.view.utils

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.drawable.Drawable
import android.graphics.text.LineBreaker
import android.text.Layout
import android.text.StaticLayout
import android.text.TextPaint
import android.text.TextUtils
import android.util.AttributeSet
import android.view.View
import android.view.ViewGroup
import androidx.annotation.ColorRes
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.core.content.ContextCompat
import com.application.foundation.utils.CommonUtils
import java.util.*
import kotlin.math.ceil
import kotlin.math.max
import kotlin.math.min

/**
 * Custom TextView implementation which allows to answer questions
 * about text layout, such as ellipsized text or not, maximum available lines count
 * depending on the available text width and height
 * see:
 * fun measureLayout(width: Int, maxHeight: Int, forceMeasure: Boolean): MeasureResult
 */
class FlexibleTextView : View {

	private var layoutNullable: StaticLayout? = null
	private val layout: StaticLayout
		get() = layoutNullable!!

	private var textPaintNullable: TextPaint? = null
	private val textPaint: TextPaint
		get() = textPaintNullable!!



	constructor(context: Context) : super(context)
	constructor(context: Context, attrs: AttributeSet) : super(context, attrs)


	var allowRequestLayout = true

	private fun requestLayoutCustom() {
		if (allowRequestLayout) {
			requestLayoutIfRequired()
		}
	}

	var text: CharSequence = ""
		set(value) {
			if (field != value) {
				field = value

				requestLayoutCustom()
			}
		}

	fun setTextRes(@StringRes stringRes: Int) {
		text = resources.getText(stringRes)
	}


	var textColor: Int = 0
		set(color) {
			if (field != color) {
				clearPaint()

				field = color

				requestLayoutCustom()
			}
		}

	fun setTextColorRes(@ColorRes colorRes: Int) {
		textColor = ContextCompat.getColor(context, colorRes)
	}



	var textSize: Float = 0f
		set(size) {
			if (field != size) {
				clearPaint()

				field = size

				requestLayoutCustom()
			}
		}

	fun setTextSizeDp(sizeDp: Float) {
		textSize = CommonUtils.dpToPxPrecise(context, sizeDp)
	}

	fun setTextSizeSp(sizeSp: Float) {
		textSize = CommonUtils.spToPxPrecise(context, sizeSp)
	}



	var typeface: Typeface? = null
		set(typeface) {
			if (field != typeface) {
				clearPaint()

				field = typeface

				requestLayoutCustom()
			}
		}



	var lineSpacingExtra: Float = 0f
		set(lineSpacingExtra) {
			field = lineSpacingExtra

			requestLayoutCustom()
		}


	var ellipsize: TextUtils.TruncateAt? = null
		set(ellipsize) {
			field = ellipsize

			requestLayoutCustom()
		}

	var alignment: Layout.Alignment = Layout.Alignment.ALIGN_NORMAL
		set(alignment) {
			field = alignment

			requestLayoutCustom()
		}


	var hyphenationFrequency: Int = Layout.HYPHENATION_FREQUENCY_NONE
		set(hyphenationFrequency) {
			field = hyphenationFrequency

			requestLayoutCustom()
		}

	var breakStrategy: Int = Layout.BREAK_STRATEGY_SIMPLE
		set(breakStrategy) {
			field = breakStrategy

			requestLayoutCustom()
		}


	var drawablePadding: Int = 0
		set(drawablePadding) {
			field = drawablePadding

			requestLayoutCustom()
		}

	fun setDrawablePaddingDp(sizeDp: Float) {
		drawablePadding = CommonUtils.dpToPx(context, sizeDp)
	}


	var drawableLeft: Drawable? = null
		set(drawableLeft) {
			field = drawableLeft?.apply {
				setBounds(0, 0, intrinsicWidth, intrinsicHeight)
			}

			requestLayoutCustom()
		}

	fun setDrawableLeftRes(@DrawableRes drawableRes: Int) {
		drawableLeft = ContextCompat.getDrawable(context, drawableRes)
	}


	private val drawableWidth: Int
		get() = if (drawableLeft == null) 0 else drawableLeft!!.intrinsicWidth + drawablePadding

	private val drawableHeight: Int
		get() = if (drawableLeft == null) 0 else drawableLeft!!.intrinsicHeight



	private fun clearPaint() {
		textPaintNullable = null
	}

	private fun createPaintIfRequired() {

		if (textPaintNullable == null) {

			val paint = Paint().apply {
				isAntiAlias = true

				color = this@FlexibleTextView.textColor
				typeface = this@FlexibleTextView.typeface
				textSize = this@FlexibleTextView.textSize
			}

			textPaintNullable = TextPaint(paint)
		}
	}


	class MeasureResult(
		val	isEnoughSpace: Boolean,
		val isTextMeasured: Boolean,
		val linesCountAvailable: Int
	)

	@SuppressLint("NewApi")
	fun measureLayout(width: Int, maxLinesCount: Int): MeasureResult {

		val width = max(0, width - paddingLeft - paddingRight - drawableWidth)

		createLayout(width, maxLinesCount)

		return MeasureResult(isEnoughSpace = width != 0 && !isTextEllipsized,
			isTextMeasured = true,
			maxLinesCount)
	}


	fun measureLayout(width: Int, maxHeight: Int, forceMeasure: Boolean): MeasureResult {
		return measureLayout(width, maxHeight, maxLinesCount = Int.MAX_VALUE, forceMeasure)
	}

	fun measureLayout(width: Int, maxHeight: Int, maxLinesCount: Int, forceMeasure: Boolean): MeasureResult {

		val width = max(0, width - paddingLeft - paddingRight - drawableWidth)
		val height = max(0f, maxHeight.toFloat() - paddingTop - paddingBottom)

		createPaintIfRequired()

		val linesCountAvailable = TextDimensionsUtils.getTextMaxLinesCount(
			height,
			textLineHeight,
			lineSpacingExtra)

		val isTextMeasured = linesCountAvailable > 0 || forceMeasure
		if (isTextMeasured) {
			createLayout(width, min(max(1, linesCountAvailable), maxLinesCount))
		}

		return MeasureResult(isEnoughSpace =
		width != 0 && height != 0f && height - drawableHeight >= 0 &&

				linesCountAvailable > 0 &&
				maxHeight - (max(layout.height, drawableHeight) + paddingTop + paddingBottom) + CommonUtils.dpToPx(context, 4) >= 0 && // CommonUtils.dpToPx(context, 4) is for additional safety
				!isTextEllipsized,

			isTextMeasured = isTextMeasured,

			linesCountAvailable)
	}

	private val textLineHeight: Float
		get() = TextDimensionsUtils.getTextLineHeight(textPaint)


	val linesCount: Int
		get() = layout.lineCount


	@SuppressLint("NewApi")
	private fun createLayout(width: Int, maxLinesCount: Int = Int.MAX_VALUE) {

		createPaintIfRequired()

		layoutNullable = StaticLayout.Builder.obtain(text, 0, text.length, textPaint, width)
			.setLineSpacing(lineSpacingExtra, 1f)
			.setEllipsize(ellipsize)

			.setHyphenationFrequency(hyphenationFrequency)
			.setBreakStrategy(breakStrategy)

			.setMaxLines(maxLinesCount)

			.setAlignment(alignment)

			.build()

		measureAsLayout()
	}

	private fun measureAsLayout() {
		if (layoutNullable != null) {
			setMeasuredDimension(layout.width + paddingLeft + paddingRight + drawableWidth,
				paddingTop + paddingBottom + max(layout.height, drawableHeight))
		}
	}

	val isTextEllipsized: Boolean
		get() = layout.text.toString().lowercase(Locale.getDefault()) !=
				text.toString().lowercase(Locale.getDefault())


	override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {

		createPaintIfRequired()


		var textWidth: Int? = null
		if (layoutParams.width != ViewGroup.LayoutParams.WRAP_CONTENT &&
			layoutParams.width != ViewGroup.LayoutParams.MATCH_PARENT) {
			textWidth = layoutParams.width
		}

		var textHeight: Int? = null
		if (layoutParams.height != ViewGroup.LayoutParams.WRAP_CONTENT &&
			layoutParams.height != ViewGroup.LayoutParams.MATCH_PARENT) {
			textHeight = layoutParams.height
		}


		var mode = MeasureSpec.getMode(widthMeasureSpec)
		var size = MeasureSpec.getSize(widthMeasureSpec) - paddingLeft - paddingRight - drawableWidth

		textWidth = if (mode == MeasureSpec.AT_MOST || mode == MeasureSpec.UNSPECIFIED) {

			if (textWidth == null) {
				val desiredWidth = ceil(Layout.getDesiredWidth(text, textPaint))

				if (mode == MeasureSpec.AT_MOST) min(desiredWidth.toInt(), size) else desiredWidth.toInt()
			} else {
				if (mode == MeasureSpec.AT_MOST) min(textWidth, size) else textWidth
			}

		} else {
			size
		}


		mode = MeasureSpec.getMode(heightMeasureSpec)
		size = MeasureSpec.getSize(heightMeasureSpec) - paddingTop - paddingBottom


		createLayout(textWidth)


		textHeight = if (mode == MeasureSpec.AT_MOST || mode == MeasureSpec.UNSPECIFIED) {
			if (textHeight == null) {

				if (mode == MeasureSpec.AT_MOST) min(layout.height, size) else layout.height
			} else {
				if (mode == MeasureSpec.AT_MOST) min(textHeight, size) else textHeight
			}
		} else {
			size
		}


		setMeasuredDimension(textWidth + paddingLeft + paddingRight + drawableWidth,
			paddingTop + paddingBottom + max(textHeight, drawableHeight))
	}

	override fun onDraw(canvas: Canvas) {
		super.onDraw(canvas)

		if (layoutNullable == null) {
			return
		}

		canvas.apply {
			save()

			val drawableLeftShift = if (drawableLeft != null) drawableLeft!!.intrinsicWidth + drawablePadding else 0
			val layoutTopShift_ = (measuredHeight - paddingTop - paddingBottom - layout.height) / 2
			val layoutTopShift = layoutTopShift_ - CommonUtils.dpToPxPrecise(context, if (drawableLeft == null) 0 else 1)

			translate(paddingLeft.toFloat() + drawableLeftShift,
				paddingTop.toFloat() + layoutTopShift)

			layout.draw(canvas)

			if (drawableLeft != null) {

				translate(-drawableLeftShift.toFloat(),
					-layoutTopShift + (measuredHeight - paddingTop - paddingBottom - drawableLeft!!.intrinsicHeight) / 2)

				drawableLeft!!.draw(canvas)
			}

			canvas.restore()
		}
	}
}