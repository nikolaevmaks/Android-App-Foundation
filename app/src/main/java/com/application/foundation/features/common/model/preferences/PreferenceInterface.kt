package com.application.foundation.features.common.model.preferences

import android.content.SharedPreferences

interface PreferenceInterface<T> {
	val key: String
	val default: T

	fun get(): T

	fun setWithoutApply(value: T)
	fun setWithoutApply(value: T, transaction: TransactionInterface)
	fun set(value: T)
	fun setSync(value: T)

	fun putInEditor(editor: SharedPreferences.Editor)
	fun enableSynchronization()
}