package com.application.foundation.features.common.view.utils

import android.animation.Animator
import android.animation.ValueAnimator.AnimatorUpdateListener
import android.animation.ValueAnimator
import androidx.annotation.CallSuper

abstract class UpdateListener : AnimatorUpdateListener, Animator.AnimatorListener {

	var isCancelled = false
		private set

	var isEnded = false
		private set

	override fun onAnimationUpdate(animation: ValueAnimator) {}

	override fun onAnimationStart(animation: Animator) {}

	@CallSuper
	override fun onAnimationEnd(animation: Animator) {
		isEnded = true

		if (!isCancelled) {
			onAnimationEndWithoutCancel(animation)
		}
	}

	open fun onAnimationEndWithoutCancel(animation: Animator) {}

	@CallSuper
	override fun onAnimationCancel(animation: Animator) {
		isCancelled = true
	}

	override fun onAnimationRepeat(animation: Animator) {}
}