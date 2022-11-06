package com.application.foundation.features.common.view.utils

interface OnMeasureListener {
	fun onBeforeMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int)
	fun onAfterMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int)
}