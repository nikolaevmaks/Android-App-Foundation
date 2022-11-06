package com.application.foundation.features.common.model.preferences

import android.annotation.SuppressLint
import android.content.SharedPreferences
import com.application.foundation.App.Companion.isTesting
import com.application.foundation.features.common.model.preferences.RealBasePreference.Companion.getPrefs

class Transaction private constructor(private val preferenceFileName: String?) : TransactionInterface {

	private var editor: SharedPreferences.Editor? = null
	private var isEnded = false

	companion object {

		@JvmOverloads
		fun begin(preferenceFileName: String? = null): TransactionInterface {
			return if (isTesting) TransactionMock() else Transaction(preferenceFileName)
		}

		fun transaction(preferenceFileName: String? = null, block: TransactionInterface.() -> Unit) {
			begin(preferenceFileName).apply(block).end()
		}
	}


	// need to create Editor only if putPreference called
	@SuppressLint("CommitPrefEdits")
	private fun checkEditor() {
		if (editor == null) {
			editor = getPrefs(preferenceFileName).edit()
		}
	}

	override fun putPreference(preference: PreferenceInterface<*>): TransactionInterface {
		if (isEnded) {
			throw RuntimeException("Transaction putPreference() after end() or endSync() called!")
		}

		checkEditor()

		preference.putInEditor(editor!!)
		return this
	}

	override fun remove(key: String): TransactionInterface {
		if (isEnded) {
			throw RuntimeException("Transaction remove() after end() or endSync() called!")
		}

		checkEditor()

		BasePreference.remove(key, null, editor)
		return this
	}

	override fun end() {
		editor?.apply()

		isEnded = true
	}

	override fun endSync() {
		editor?.commit()

		isEnded = true
	}
}