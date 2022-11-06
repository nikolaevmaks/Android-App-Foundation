package com.application.foundation.features.common.model.preferences

import android.content.SharedPreferences
import com.application.foundation.utils.LogUtils

class BoolPreference<T: Boolean?>(key: String, defaultValue: T) : RealBasePreference<T>(key, defaultValue) {

	companion object {
		private const val TAG = "BoolPreference"
	}

	init {
		get()
	}

	override fun load(): T {
		return try {
			if (prefs.contains(key)) {
				prefs.getBoolean(key, false) as T
			} else {
				default
			}
		} catch (e: Exception) {
			LogUtils.logE(TAG, e.message)
			default
		}
	}

	override fun saveWithoutApply(value: T, editor: SharedPreferences.Editor) {
		editor.putBoolean(key, value!!)
	}
}