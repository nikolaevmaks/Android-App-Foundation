package com.application.foundation

import android.content.Context
import androidx.annotation.AnyThread
import com.application.foundation.common.model.preferences.BasePreferenceMock
import com.application.foundation.features.checkout.model.Stripe
import com.application.foundation.features.common.model.preferences.PreferenceInterface
import com.application.foundation.network.WebClient
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import org.mockito.Mockito
import java.lang.reflect.Type

class MockInjector : BaseInjector() {

	@get:AnyThread
	override val application: App
		get() = get(App::class.java) { Mockito.mock(App::class.java) }

	@get:AnyThread
	override val applicationContext: Context
		get() = get(Context::class.java) { Mockito.mock(Context::class.java) }

	@get:AnyThread
	override val applicationScope: CoroutineScope
		get() = get(Injector.APPLICATION_SCOPE) { CoroutineScope(SupervisorJob() + Dispatchers.Main) }


	// Preferences

	override fun <T : Int?> getIntPreference(key: String, defaultValue: T): PreferenceInterface<T> {
		return BasePreferenceMock(key, defaultValue)
	}

	override fun <T : Long?> getLongPreference(key: String, defaultValue: T): PreferenceInterface<T> {
		return BasePreferenceMock(key, defaultValue)
	}

	override fun <T : String?> getStringPreference(key: String, defaultValue: T): PreferenceInterface<T> {
		return BasePreferenceMock(key, defaultValue)
	}

	override fun <T : Boolean?> getBooleanPreference(key: String, defaultValue: T): PreferenceInterface<T> {
		return BasePreferenceMock(key, defaultValue)
	}

	override fun <T : Enum<T>> getEnumPreference(key: String, clazz: Class<T>, defaultValue: T?): PreferenceInterface<T?> {
		return BasePreferenceMock(key, defaultValue)
	}

	override fun <T : Enum<T>> getEnumPreferenceNotNull(key: String, clazz: Class<T>, defaultValue: T): PreferenceInterface<T> {
		return BasePreferenceMock(key, defaultValue)
	}

	override fun <T> getObjectPreference(key: String, type: Type, defaultValue: T, preferenceFileName: String?): PreferenceInterface<T> {
		return BasePreferenceMock(key, defaultValue)
	}



	override val webClient: WebClient
		get() = get(WebClient::class.java) { WebClient(null) }


	override val stripe: Stripe
		get() = get(Stripe::class.java) { Mockito.mock(Stripe::class.java) }
}