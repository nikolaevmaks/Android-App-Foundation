package com.application.foundation.features.common.model.preferences

import android.content.SharedPreferences
import com.application.foundation.App.Companion.isTesting
import java.util.concurrent.ConcurrentHashMap

// https://jorosjavajams.wordpress.com/volatile-vs-synchronized/
abstract class BasePreference<T>(final override val key: String, final override val default: T) : PreferenceInterface<T> {

	companion object {

		private val values = ConcurrentHashMap<String, Entry<*>>()

		fun remove(key: String, preferenceFileName: String? = null, editor: SharedPreferences.Editor? = null) {
			var editor = editor

			if (!isTesting) {
				if (editor == null) {
					editor = RealBasePreference.getPrefs(preferenceFileName).edit()
					editor.remove(key)
					editor.apply()
				} else {
					editor.remove(key)
				}
			}
			values.remove(key)
		}
	}

	class Entry<T> {
		var value: T? = null

		@Volatile
		var isLoaded = false

		var isSynchronized = false
	}

	@JvmField
	val entry: Entry<T>


	// only one thread allowed to write to this preference, others allowed only to read
	// otherwise (very rare case) call enableSynchronization
	init {
		val entry: Entry<T> = Entry()
		val oldEntry = values.putIfAbsent(key, entry)
		this.entry = (oldEntry ?: entry) as Entry<T>
	}




	// call it right after this preference creation
	final override fun enableSynchronization() {
		entry.isSynchronized = true
	}

	final override fun get(): T {
		return if (entry.isSynchronized) {
			synchronized(entry) { return staff }
		} else {
			staff
		}
	}

	private val staff: T
		get() {
			if (!entry.isLoaded) {
				entry.value = load()
				entry.isLoaded = true
			}
			return entry.value as T
		}




	final override fun setWithoutApply(value: T) {
		if (entry.isSynchronized) {
			synchronized(entry) { setWithoutApplyStaff(value) }
		} else {
			setWithoutApplyStaff(value)
		}
	}

	final override fun setWithoutApply(value: T, transaction: TransactionInterface) {
		setWithoutApply(value)

		transaction.putPreference(this)
	}



	fun setWithoutApplyStaff(value: T) {
		entry.value = value
		entry.isLoaded = true
	}

	protected abstract fun load(): T
}