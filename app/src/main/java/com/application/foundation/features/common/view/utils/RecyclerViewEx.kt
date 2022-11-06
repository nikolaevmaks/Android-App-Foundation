package com.application.foundation.features.common.view.utils

import android.content.Context
import android.util.AttributeSet
import androidx.recyclerview.widget.RecyclerView

class RecyclerViewEx: RecyclerView {

	var parentWidth: Int = 0
		private set

	var parentHeight: Int = 0
		private set


	constructor(context: Context) : super(context)

	constructor(context: Context, attrs: AttributeSet) : super(context, attrs)


	override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {

		parentWidth = MeasureSpec.getSize(widthMeasureSpec)
		parentHeight = MeasureSpec.getSize(heightMeasureSpec)

		super.onMeasure(widthMeasureSpec, heightMeasureSpec)
	}
}