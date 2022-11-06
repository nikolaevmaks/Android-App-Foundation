package com.application.foundation.features.common.model.preferences

import android.content.SharedPreferences
import com.application.foundation.utils.LogUtils

class EnumPreference<T : Enum<T>>(key: String, private val clazz: Class<T>, defaultValue: T) : RealBasePreference<T>(key, defaultValue) {

	companion object {
		private val TAG = EnumPreference::class.java.simpleName
	}

	init {
		get()
	}

	override fun load(): T {
		return try {
			val strResult = prefs.getString(key, default.name)
			(if (strResult == null) null else java.lang.Enum.valueOf(clazz, strResult)) as T

		} catch (e: Exception) {
			LogUtils.logE(TAG, e.message)
			default
		}
	}

	override fun saveWithoutApply(value: T, editor: SharedPreferences.Editor) {
		editor.putString(key, value.name)
	}
}