package com.application.foundation.features.common.view.utils

import android.graphics.Paint
import android.graphics.Point
import android.graphics.RectF
import android.graphics.Typeface
import android.widget.TextView
import android.text.TextUtils
import android.text.TextPaint
import com.application.foundation.utils.CommonUtils

object TextDimensionsUtils {

	@JvmStatic
	fun getTextsPositionsWithCenterGravity(texts: Array<String>, paints: Array<Paint>, textsTopDivider: Int): Array<Point> {

		val textRects: Array<RectF> = Array(texts.size) { i -> getTextRect(texts[i], paints[i]) }

		val union: RectF = getTextRectsUnion(textsTopDivider, *textRects)
		var bottom = 0f

		return Array<Point>(textRects.size) { i -> run {

			val rect = textRects[i]

			Point((-rect.width()).toInt() / 2,
				(-union.height() / 2 + bottom - paints[i].ascent()).toInt())
				.also { bottom += rect.height() + textsTopDivider }
		}
		}
	}

	// pos.y will be baseline of text from middle vertical position
	@JvmStatic
	fun getTextRelativePos(text: String, paint: Paint, pos: Point) {
		pos.x = (-getTextWidth(text, paint) / 2).toInt()
		pos.y = getTextRelativePosY(paint)
	}

	@JvmStatic
	fun getTextRelativePosY(paint: Paint): Int {
		return (-(paint.ascent() + paint.descent())).toInt() / 2
	}

	@JvmStatic
	fun getTextRect(text: String, paint: Paint): RectF {
		return RectF().apply {
			left = 0f
			top = 0f
			right = getTextWidth(text, paint)
			bottom = -paint.ascent() + paint.descent()
		}
	}

	@JvmStatic
	fun getTextLineHeight(paint: Paint): Float {
		return -paint.ascent() + paint.descent()
	}

	@JvmStatic
	fun getTextLineHeight(typeface: Typeface, textSize: Int): Float {
		return getTextLineHeight(typeface, textSize.toFloat())
	}

	@JvmStatic
	fun getTextLineHeight(typeface: Typeface, textSize: Float): Float {

		return getTextLineHeight(Paint().apply {
			isAntiAlias = true
			this.typeface = typeface
			this.textSize = textSize
		}) //paint.fontSpacing
	}

	@JvmStatic
	@JvmOverloads
	fun getTextMaxLinesCount(textMaxHeight: Float, lineHeight: Float, lineSpacingExtra: Float = 0f): Int {
		// lineSpacingExtra - Extra spacing between lines of text. The value will not be applied for the last line of text. and for the first line also

		var height: Float = textMaxHeight
		var linesCount = 0

		while (height > 0) {
			height -= lineHeight

			if (height >= 0) {
				linesCount++
				height -= lineSpacingExtra
			}
		}
		return linesCount
	}

	@JvmStatic
	fun getTextWidth(text: String, paint: Paint): Float {
		return paint.measureText(text, 0, text.length)
	}

	@JvmStatic
	fun getTextWidth(textView: TextView): Float {
		val text = CommonUtils.getTextViewText(textView)
		return textView.paint.measureText(text, 0, text.length)
	}

	private fun getTextRectsUnion(textsTopDivider: Int, vararg textRects: RectF): RectF {
		val union = RectF()

		for (i in textRects.indices) {
			val rect = textRects[i]
			union.set(0f,
				0f,
				Math.max(union.right, rect.right),
				union.bottom + rect.height() + if (i == textRects.size - 1) 0 else textsTopDivider)
		}
		return union
	}

	@JvmStatic
	fun ellipsize(text: String, maxLengthPixelsAvailable: Float, paint: Paint): String {
		return TextUtils.ellipsize(text, TextPaint(paint), maxLengthPixelsAvailable, TextUtils.TruncateAt.END).toString()
	}
}