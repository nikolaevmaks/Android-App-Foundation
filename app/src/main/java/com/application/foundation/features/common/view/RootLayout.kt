package com.application.foundation.features.common.view

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.view.ViewGroup
import com.application.foundation.features.common.view.utils.OnMeasureListener
import com.google.android.material.appbar.AppBarLayout
import kotlin.math.max

class RootLayout : ViewGroup {

	private var appBarLayout: AppBarLayout? = null
	private lateinit var viewProgressInitial: View
	private lateinit var viewContent: View

	private var onMeasureListener: OnMeasureListener? = null


	constructor(context: Context) : super(context)

	constructor(context: Context, attrs: AttributeSet) : super(context, attrs)

	constructor(context: Context, attrs: AttributeSet, defStyle: Int) : super(context, attrs, defStyle)

	fun init(appBarLayout: AppBarLayout?, viewProgressInitial: View, viewContent: View) {
		this.appBarLayout = appBarLayout
		this.viewProgressInitial = viewProgressInitial
		this.viewContent = viewContent
	}


	fun setOnMeasureListener(onMeasureListener: OnMeasureListener) {
		this.onMeasureListener = onMeasureListener
	}


	companion object {
		@JvmOverloads
		@JvmStatic
		fun measureView(view: View, maxWidth: Int = 0, maxHeight: Int = 0): Int {
			val lp = view.layoutParams

			var leftMargin = 0
			var topMargin = 0
			var rightMargin = 0
			var bottomMargin = 0

			if (lp is MarginLayoutParams) {
				leftMargin = lp.leftMargin
				topMargin = lp.topMargin
				rightMargin = lp.rightMargin
				bottomMargin = lp.bottomMargin
			}

			val widthMeasureSpec = MeasureSpec.makeMeasureSpec(
					if (lp.width != LayoutParams.MATCH_PARENT && lp.width != LayoutParams.WRAP_CONTENT) lp.width else max(0, maxWidth - leftMargin - rightMargin),

					if (lp.width == LayoutParams.MATCH_PARENT) MeasureSpec.EXACTLY else
						if (lp.width == LayoutParams.WRAP_CONTENT) (if (maxWidth == 0) MeasureSpec.UNSPECIFIED else MeasureSpec.AT_MOST)
						else MeasureSpec.EXACTLY)

			val heightMeasureSpec = MeasureSpec.makeMeasureSpec(
					if (lp.height != LayoutParams.MATCH_PARENT && lp.height != LayoutParams.WRAP_CONTENT) lp.height else max(0, maxHeight - topMargin - bottomMargin),

					if (lp.height == LayoutParams.MATCH_PARENT) MeasureSpec.EXACTLY else
						if (lp.height == LayoutParams.WRAP_CONTENT) (if (maxHeight == 0) MeasureSpec.UNSPECIFIED else MeasureSpec.AT_MOST)
						else MeasureSpec.EXACTLY)


			view.measure(widthMeasureSpec, heightMeasureSpec)

			return view.measuredHeight + topMargin + bottomMargin
		}

		@JvmStatic
		fun layoutHidden(view: View) {
			view.layout(0, 0, 0, 0)
		}
	}

	override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {

		onMeasureListener?.onBeforeMeasure(widthMeasureSpec, heightMeasureSpec)


		val parentWidth = MeasureSpec.getSize(widthMeasureSpec)
		val parentHeight = MeasureSpec.getSize(heightMeasureSpec)


		val height = appBarLayout?.let { measureView(it, parentWidth, parentHeight) } ?: 0


		measureView(viewProgressInitial, parentWidth, parentHeight - height)
		measureView(viewContent, parentWidth, parentHeight - height)


		setMeasuredDimension(parentWidth, parentHeight)


		onMeasureListener?.onAfterMeasure(widthMeasureSpec, heightMeasureSpec)
	}

	override fun onLayout(changed: Boolean, l: Int, t: Int, r: Int, b: Int) {

		appBarLayout?.let { it.layout(0, 0, it.measuredWidth, it.measuredHeight) }


		val height = appBarLayout?.measuredHeight ?: 0

		viewProgressInitial.layout(0, height, viewProgressInitial.measuredWidth, height + viewProgressInitial.measuredHeight)

		viewContent.layout(0, height, viewContent.measuredWidth, height + viewContent.measuredHeight)
	}


	override fun generateLayoutParams(attrs: AttributeSet): LayoutParams {
		return LayoutParams(context, attrs)
	}

	override fun generateDefaultLayoutParams(): LayoutParams {
		return LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT)
	}

	override fun generateLayoutParams(p: LayoutParams): LayoutParams {
		return LayoutParams(p)
	}
}