package com.application.foundation.features.common.model.preferences

interface TransactionInterface {

	fun putPreference(preference: PreferenceInterface<*>): TransactionInterface
	fun remove(key: String): TransactionInterface

	fun end()
	fun endSync()


	fun <T> put(preference: PreferenceInterface<T>, value: T): TransactionInterface {
		preference.setWithoutApply(value, this)
		return this
	}
}