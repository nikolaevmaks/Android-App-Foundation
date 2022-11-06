package com.application.foundation.features.common.view.utils

import android.os.Build
import android.transition.Transition
import android.transition.TransitionManager
import android.view.ViewGroup

object TransitionUtils {

	@JvmStatic
	val isTransitionApiAvailable: Boolean
		get() = Build.VERSION.SDK_INT >= 23

	@JvmStatic
	fun beginDelayedTransitionIfPossible(sceneRoot: ViewGroup): Boolean {
		return if (isTransitionApiAvailable) {
			TransitionManager.beginDelayedTransition(sceneRoot)
			true
		} else {
			false
		}
	}

	@JvmStatic
	fun beginDelayedTransitionIfPossible(sceneRoot: ViewGroup, transition: Transition?): Boolean {
		return if (isTransitionApiAvailable) {
			TransitionManager.beginDelayedTransition(sceneRoot, transition)
			true
		} else {
			false
		}
	}

	@JvmStatic
	fun endTransitionsIfPossible(sceneRoot: ViewGroup): Boolean {
		return if (isTransitionApiAvailable) {
			TransitionManager.endTransitions(sceneRoot)
			true
		} else {
			false
		}
	}
}