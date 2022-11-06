package com.application.foundation.features.common.model.preferences

internal class TransactionMock : TransactionInterface {

	override fun putPreference(preference: PreferenceInterface<*>): TransactionInterface {
		return this
	}

	override fun remove(key: String): TransactionInterface {
		BasePreference.remove(key, null, null)
		return this
	}

	override fun end() {}

	override fun endSync() {}
}