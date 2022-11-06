package com.application.foundation.features.common.model

import com.application.foundation.App.Companion.injector
import com.application.foundation.features.common.model.preferences.PreferencesNames
import com.application.foundation.features.common.model.utils.ExponentialBackOff
import com.application.foundation.features.common.model.utils.RequestError
import com.application.foundation.network.RequestHandlerBaseResponseNullBody
import com.application.foundation.utils.CommonUtils
import kotlinx.coroutines.Job

class Acquisition : RequestBase<Any, Any>() {

	private val acquisitionSourcePreference = injector.getStringPreference(PreferencesNames.ACQUISITION_SOURCE, null as String?)
	private val isAcquisitionSourceRegisteredPreference = injector.getBooleanPreference(PreferencesNames.IS_ACQUISITION_SOURCE_REGISTERED, false)

	private val retry = ExponentialBackOff(3000, 2.5f, 5)

	private var acquisitionSourceJob: Job? = null


	var acquisitionSource: String?
		get() = acquisitionSourcePreference.get()
		private set(value) = acquisitionSourcePreference.set(value)


	override fun abort() {
		super.abort()
		abortStaff()
	}

	private fun abortStaff() {
		abortRequest()

		acquisitionSourceJob?.let {
			it.cancel()
			acquisitionSourceJob = null
		}
	}

	override fun clear() {
		super.clear()

		acquisitionSource = null
	}

	fun requestAcquisitionIfRequired() {

		if (!isUpdating && acquisitionSource != null && !isAcquisitionSourceRegisteredPreference.get()) {

			retry.reset()
			requestAcquisitionStaff()
		}
	}

	fun requestAcquisitionIfRequired(acquisitionSource: String) {
		if (!isUpdating && this.acquisitionSource == null) {

			this.acquisitionSource = acquisitionSource

			retry.reset()
			requestAcquisitionStaff()

			updateAnalyticsDimensions(acquisitionSource)
		}
	}

	private fun requestAcquisitionStaff() {

		continueRequest(injector.webClient.requestAcquisition(acquisitionSource!!,
				object : RequestHandlerBaseResponseNullBody(this@Acquisition) {

					public override fun onReceiveValidResult() {
						isAcquisitionSourceRegisteredPreference.set(true)
						stopRequest()
					}

					override fun onError(error: RequestError<*>) {
						val delay = retry.nextBackOffMillis()
						if (delay == -1) {
							super.onError(error)
						} else {
							acquisitionSourceJob = CommonUtils.launchDelayed(delay.toLong()) {
								acquisitionSourceJob = null
								requestAcquisitionStaff()
							}
						}
					}
				}))
	}

	private fun updateAnalyticsDimensions(source: String) {
//		injector.intercom.updateUser(UserAttributes.Builder()
//				.withCustomAttribute(Deeplink.ACQUISITION_SOURCE, source)
//				.build())
//
//		injector.firebaseAnalytics.setUserProperty(Deeplink.ACQUISITION_SOURCE, source)
//
//		YandexMetrica.reportUserProfile(UserProfile.newBuilder()
//				.apply(Attribute.customString(Deeplink.ACQUISITION_SOURCE).withValue(source))
//				.build())
	}
}