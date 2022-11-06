package com.application.foundation.common.model.preferences

import android.content.SharedPreferences
import com.application.foundation.features.common.model.preferences.BasePreference

internal class BasePreferenceMock<T>(key: String, defaultValue: T) : BasePreference<T>(key, defaultValue) {

	override fun load(): T {
		return default
	}

	override fun set(value: T) {
		setWithoutApply(value)
	}

	override fun setSync(value: T) {
		setWithoutApply(value)
	}

	override fun putInEditor(editor: SharedPreferences.Editor) {}
}