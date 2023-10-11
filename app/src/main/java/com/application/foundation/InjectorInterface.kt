package com.application.foundation

import android.content.Context
import androidx.annotation.AnyThread
import androidx.annotation.MainThread
import com.application.foundation.features.checkout.model.Stripe
import com.application.foundation.features.common.model.Acquisition
import com.application.foundation.features.common.model.DetectCountry
import com.application.foundation.features.common.model.Models
import com.application.foundation.features.common.model.RequestBase
import com.application.foundation.features.common.model.preferences.PreferenceInterface
import com.application.foundation.features.profile.model.DeliveryAddress
import com.application.foundation.features.profile.model.Profile
import com.application.foundation.features.profile.model.Token
import com.application.foundation.network.WebClientInterface
import com.squareup.moshi.Moshi
import kotlinx.coroutines.CoroutineScope
import com.application.foundation.utils.Analytics
import java.lang.reflect.Type

interface InjectorInterface {

	@get:AnyThread
	val application: App
	@get:AnyThread
	val applicationContext: Context
	@get:AnyThread
	val applicationScope: CoroutineScope


	@MainThread
	fun <ViewModel : Any> getViewModel(className: String, viewModelTag: String?): ViewModel?

	@MainThread
	fun <ViewModel : Any> setViewModel(className: String, viewModelTag: String?, viewModel: ViewModel)

	@MainThread
	fun removeViewModels(className: String)

	@MainThread
	fun abortViewModels()


	fun <T : Int?> getIntPreference(key: String, defaultValue: T): PreferenceInterface<T>
	fun <T : Long?> getLongPreference(key: String, defaultValue: T): PreferenceInterface<T>
	fun <T : String?> getStringPreference(key: String, defaultValue: T): PreferenceInterface<T>

	fun <T : Boolean?> getBooleanPreference(key: String, defaultValue: T): PreferenceInterface<T>

	fun <T : Enum<T>> getEnumPreference(key: String, clazz: Class<T>, defaultValue: T?): PreferenceInterface<T?>
	fun <T : Enum<T>> getEnumPreferenceNotNull(key: String, clazz: Class<T>, defaultValue: T): PreferenceInterface<T>

	fun <T> getObjectPreference(key: String, type: Type, defaultValue: T, preferenceFileName: String? = null): PreferenceInterface<T>


	val moshi: Moshi
	val webClient: WebClientInterface

	val models: Models

	val acquisition: Acquisition
	val token: Token
	val profile: Profile

	val userUUID: String?
	val isAuthorized: Boolean

	val detectCountry: DetectCountry
	val deliveryAddress: DeliveryAddress

	val stripe: Stripe

	val analytics: Analytics
}

inline fun <reified Class : Any, reified ViewModelTag : Any> InjectorInterface.abortViewModel() {
	val viewModel = getViewModel<Any>(className = Class::class.java.name, viewModelTag = ViewModelTag::class.java.simpleName)

	if (viewModel != null && viewModel is RequestBase<*, *>) {
		viewModel.abort()
	}
}