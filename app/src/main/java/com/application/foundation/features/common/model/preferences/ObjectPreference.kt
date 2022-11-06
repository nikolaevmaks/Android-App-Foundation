package com.application.foundation.features.common.model.preferences

import android.content.SharedPreferences
import com.application.foundation.App.Companion.injector
import com.application.foundation.utils.LogUtils
import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.JsonDataException
import java.io.IOException
import java.lang.reflect.Type

class ObjectPreference<T>(key: String, private val type: Type, defaultValue: T, preferenceFileName: String? = null) :
		RealBasePreference<T>(key, defaultValue, preferenceFileName) {

	companion object {
		private val TAG = ObjectPreference::class.java.simpleName
	}

	init {
		get()
	}

	override fun load(): T {
		val result: String? = try {
			prefs.getString(key, null)
		} catch (e: Exception) {
			LogUtils.logE(TAG, e.message)
			null
		}

		return if (result == null) {
			default
		} else {
			try {
				val adapter: JsonAdapter<T> = injector.moshi.adapter(type)
				adapter.fromJson(result) as T

			} catch (e: JsonDataException) {
				LogUtils.logE(TAG, e.message)
				default
			} catch (e: IOException) {
				LogUtils.logE(TAG, e.message)
				default
			}
		}
	}

	override fun saveWithoutApply(value: T, editor: SharedPreferences.Editor) {
		val adapter: JsonAdapter<T> = injector.moshi.adapter(type)
		editor.putString(key, adapter.toJson(value))
	}
}