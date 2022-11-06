package com.application.foundation

import androidx.annotation.AnyThread
import androidx.annotation.MainThread
import com.application.foundation.features.common.model.Acquisition
import com.application.foundation.features.common.model.DetectCountry
import com.application.foundation.features.common.model.Models
import com.application.foundation.features.common.model.utils.Creator
import com.application.foundation.features.profile.model.DeliveryAddress
import com.application.foundation.features.profile.model.Profile
import com.application.foundation.features.profile.model.Token
import com.application.foundation.utils.Analytics
import com.application.foundation.utils.BigDecimalAdapter
import com.application.foundation.utils.DateYYYYMMDDTHHMMSS_SSSZ
import com.squareup.moshi.Moshi
import java.util.*
import java.util.concurrent.ConcurrentHashMap

abstract class BaseInjector : InjectorInterface {

	private val data: MutableMap<String, Any> = ConcurrentHashMap()

	@AnyThread
	fun <T> get(clazz: Class<*>, creator: Creator<T>): T = get(clazz.simpleName, creator)

	@AnyThread
	fun <T> get(key: String, creator: Creator<T>): T {

		var obj = data[key] as T?
		if (obj == null) {
			synchronized(this) {
				obj = data[key] as T?
				if (obj == null) {
					obj = creator.create()
					data[key] = obj!!
				}
			}
		}
		return obj!!
	}


	// <presenter class name<view model tag, view model>>
	private val viewModels: MutableMap<String, MutableMap<String?, Any>> = HashMap()

	@MainThread
	override fun <ViewModel> getViewModel(className: String, viewModelTag: String?): ViewModel? {
		val clazz: Map<String?, Any>? = viewModels[className]
		@Suppress("UNCHECKED_CAST")
		return if (clazz == null) null else clazz[viewModelTag] as ViewModel?
	}

	@MainThread
	override fun <ViewModel: Any> setViewModel(className: String, viewModelTag: String?, viewModel: ViewModel) {
		var clazz = viewModels[className]
		if (clazz == null) {
			clazz = HashMap(1)
			viewModels[className] = clazz
		}
		clazz[viewModelTag] = viewModel
	}

	@MainThread
	override fun removeViewModels(className: String) {
		viewModels.remove(className)
	}





	override val moshi: Moshi
		get() = get(Moshi::class.java) {
			Moshi.Builder()
					.add(BigDecimalAdapter)
					.add(DateYYYYMMDDTHHMMSS_SSSZ.MoshiAdapter)
					.build()
		}


	override val models: Models
		get() = get(Models::class.java) { Models() }


	override val acquisition: Acquisition
		get() = get(Acquisition::class.java) { Acquisition() }


	override val token: Token
		get() = get(Token::class.java) { Token() }

	override val profile: Profile
		get() = get(Profile::class.java) { Profile() }


	@get:AnyThread
	override val userUUID: String?
		get() = Profile.userUUID

	@get:AnyThread
	override val isAuthorized: Boolean
		get() = Profile.isAuthorized



	override val detectCountry: DetectCountry
		get() = get(DetectCountry::class.java) { DetectCountry() }

	override val deliveryAddress: DeliveryAddress
		get() = get(DeliveryAddress::class.java) { DeliveryAddress() }


	override val analytics: Analytics
		get() = get(Analytics::class.java) { Analytics() }
}