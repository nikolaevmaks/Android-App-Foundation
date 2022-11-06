package com.application.foundation

import android.content.Context
import androidx.annotation.AnyThread
import com.application.foundation.features.checkout.model.Stripe
import com.application.foundation.features.common.model.preferences.*
import com.application.foundation.network.WebClient
import com.application.foundation.network.WebClientInterface
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import java.lang.reflect.Type

class Injector : BaseInjector() {

	companion object {
		const val APPLICATION_SCOPE = "applicationScope"
	}

	@get:AnyThread
	override val application: App
		get() = App.instance

	@get:AnyThread
	override val applicationContext: Context
		get() = App.instance

	@get:AnyThread
	override val applicationScope: CoroutineScope
		get() = get(APPLICATION_SCOPE) { CoroutineScope(SupervisorJob() + Dispatchers.Main) }


	// Preferences

	override fun <T : Int?> getIntPreference(key: String, defaultValue: T): PreferenceInterface<T> {
		return IntPreference(key, defaultValue)
	}

	override fun <T : Long?> getLongPreference(key: String, defaultValue: T): PreferenceInterface<T> {
		return LongPreference(key, defaultValue)
	}

	override fun <T : String?> getStringPreference(key: String, defaultValue: T): PreferenceInterface<T> {
		return StringPreference(key, defaultValue)
	}

	override fun <T : Boolean?> getBooleanPreference(key: String, defaultValue: T): PreferenceInterface<T> {
		return BoolPreference(key, defaultValue)
	}


	override fun <T : Enum<T>> getEnumPreference(key: String, clazz: Class<T>, defaultValue: T?): PreferenceInterface<T?> {
		return EnumPreferenceNullable(key, clazz, defaultValue)
	}

	override fun <T : Enum<T>> getEnumPreferenceNotNull(key: String, clazz: Class<T>, defaultValue: T): PreferenceInterface<T> {
		return EnumPreference(key, clazz, defaultValue)
	}


	override fun <T> getObjectPreference(key: String, type: Type, defaultValue: T, preferenceFileName: String?): PreferenceInterface<T> {
		return ObjectPreference(key, type, defaultValue, preferenceFileName)
	}




	override val webClient: WebClientInterface
		get() = get(WebClient::class.java) { WebClient(applicationContext) }



	override val stripe: Stripe
		get() = get(Stripe::class.java) { Stripe() }
}