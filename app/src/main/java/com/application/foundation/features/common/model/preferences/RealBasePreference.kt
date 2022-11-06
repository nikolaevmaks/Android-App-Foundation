package com.application.foundation.features.common.model.preferences

import android.content.Context
import android.content.SharedPreferences
import android.preference.PreferenceManager
import com.application.foundation.App.Companion.injector

abstract class RealBasePreference<T> @JvmOverloads constructor(
		key: String,
		default: T,
		preferenceFileName: String? = null

) : BasePreference<T>(key, default) {

	companion object {

		fun getPrefs(preferenceFileName: String?): SharedPreferences {
			return if (preferenceFileName == null) {
				PreferenceManager.getDefaultSharedPreferences(injector.applicationContext)
			} else {
				injector.applicationContext.getSharedPreferences(preferenceFileName, Context.MODE_PRIVATE)
			}
		}
	}


	//@JvmStatic
	protected val prefs: SharedPreferences = getPrefs(preferenceFileName)

	final override fun set(value: T) {
		if (entry.isSynchronized) {
			synchronized(entry) { setStaff(value, true) }
		} else {
			setStaff(value, true)
		}
	}

	final override fun setSync(value: T) {
		if (entry.isSynchronized) {
			synchronized(entry) { setStaff(value, false) }
		} else {
			setStaff(value, false)
		}
	}

	private fun setStaff(value: T, apply: Boolean) {

		setWithoutApplyStaff(value)

		val editor = prefs.edit()
		if (value == null) {
			editor.remove(key)
		} else {
			saveWithoutApply(value, editor)
		}

		if (apply) {
			editor.apply()
		} else {
			editor.commit()
		}
	}

	final override fun putInEditor(editor: SharedPreferences.Editor) {
		if (entry.isSynchronized) {
			synchronized(entry) { putInEditorStaff(editor) }
		} else {
			putInEditorStaff(editor)
		}
	}

	private fun putInEditorStaff(editor: SharedPreferences.Editor) {
		val value = get()
		if (value == null) {
			editor.remove(key)
		} else {
			saveWithoutApply(value, editor)
		}
	}

	protected abstract fun saveWithoutApply(value: T, editor: SharedPreferences.Editor)
}