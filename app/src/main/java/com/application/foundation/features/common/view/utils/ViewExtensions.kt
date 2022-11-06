package com.application.foundation.features.common.view.utils

import android.app.Activity
import android.content.Context
import android.graphics.Paint
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.annotation.ColorRes
import androidx.annotation.LayoutRes
import androidx.core.content.ContextCompat

fun ViewGroup.inflate(@LayoutRes resource: Int,
					  attachToRoot: Boolean = false): View = LayoutInflater.from(context).inflate(resource, this, attachToRoot)


fun Activity.inflate(@LayoutRes resource: Int): View = LayoutInflater.from(this).inflate(resource, null)


fun TextView.setTextColorRes(@ColorRes color: Int) {
	setTextColor(ContextCompat.getColor(context, color))
}

fun View.setBackgroundColorRes(@ColorRes color: Int) {
	setBackgroundColor(ContextCompat.getColor(context, color))
}


fun TextView.setTextSizeDp(sizeDp: Float) {
	setTextSize(TypedValue.COMPLEX_UNIT_DIP, sizeDp)
}


fun Paint.setColorRes(@ColorRes color: Int, context : Context) {
	setColor(ContextCompat.getColor(context, color))
}



// if view is not attached to window isLaidOut is false, but view can be already measured and after attach to window there is no need to layout
// how to test: ProductAdapter next description flicker when drop is expired
fun View.requestLayoutIfRequired(): Boolean {
	return if (!isAttachedToWindow || isLaidOut && !isLayoutRequested) {
		requestLayout()
		true
	} else {
		false
	}
}