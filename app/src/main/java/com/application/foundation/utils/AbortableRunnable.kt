package com.application.foundation.utils

import com.application.foundation.features.common.model.utils.Abortable
import kotlin.jvm.Volatile

abstract class AbortableRunnable : Runnable, Abortable {

	@Volatile
	var isAborted = false
		private set

	final override fun run() {
		if (isAborted) {
			onProcessAbort()
		} else {
			execute()
		}
	}

	protected abstract fun execute()

	protected fun onProcessAbort() {}

	// TODO remove isAborted
	override fun abort() {
		isAborted = true
		CommonUtils.removeCallbacksOnMainThread(this)
	}
}