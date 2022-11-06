package com.application.foundation.features.common.model.preferences

import android.content.SharedPreferences
import com.application.foundation.utils.LogUtils

class IntPreference<T: Int?>(key: String, defaultValue: T) : RealBasePreference<T>(key, defaultValue) {

	companion object {
		private val TAG = IntPreference::class.java.simpleName
	}

	init {
		get()
	}

	override fun load(): T {
		return try {
			if (prefs.contains(key)) {
				prefs.getInt(key, -1) as T
			} else {
				default
			}
		} catch (e: Exception) {
			LogUtils.logE(TAG, e.message)
			default
		}
	}

	override fun saveWithoutApply(value: T, editor: SharedPreferences.Editor) {
		editor.putInt(key, value!!)
	}
}