package com.application.foundation.features.common.model.preferences

import android.content.SharedPreferences
import com.application.foundation.utils.LogUtils

class StringPreference<T: String?>(key: String, defaultValue: T) : RealBasePreference<T>(key, defaultValue) {

	companion object {
		private val TAG = StringPreference::class.java.simpleName
	}

	init {
		get()
	}

	override fun load(): T {

		return try {
			prefs.getString(key, default) as T
		} catch (e: Exception) {
			LogUtils.logE(TAG, e.message)
			default
		}
	}

	override fun saveWithoutApply(value: T, editor: SharedPreferences.Editor) {
		editor.putString(key, value)
	}
}