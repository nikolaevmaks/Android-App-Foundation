package com.application.foundation.features.common.presenter

import android.content.Context
import android.content.Intent
import android.os.Bundle
import com.application.foundation.features.common.view.MainActivity

object Router {

	fun goToMainActivityClearTask(context: Context) {

		Intent(context, mainActivityClass).apply {
			addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
			context.startActivity(this)
		}
	}

	fun goToMainActivity(activity: StartActivityInterface, clearTask: Boolean, extra: Bundle?) {

		Intent(activity.context, mainActivityClass).apply {
			if (clearTask) {
				addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
			} else {
				addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
			}
			if (extra != null) {
				putExtra(BasePresenter.EXTRA_BUNDLE, extra)
			}
			activity.startActivity(this)
		}
	}

	@JvmStatic
	val mainActivityClass: Class<*>
		get() = MainActivity::class.java
}