package com.application.foundation.features.common.model

import com.application.foundation.App.Companion.injector
import com.application.foundation.features.common.model.preferences.PreferencesNames
import com.application.foundation.features.common.model.preferences.Transaction
import com.application.foundation.features.common.model.preferences.TransactionInterface
import com.application.foundation.features.profile.model.DeliveryAddress
import com.application.foundation.features.profile.model.Profile
import com.application.foundation.features.profile.model.Token
import com.application.foundation.utils.CommonUtils

class Models {

	private val appVersionCodePreference = injector.getIntPreference(PreferencesNames.APP_VERSION_CODE, -1)
	private val appLaunchNumberPreference = injector.getIntPreference(PreferencesNames.APP_LAUNCH_NUMBER, 0)


	private lateinit var token: Token
	private lateinit var profile: Profile
	private lateinit var deliveryAddress: DeliveryAddress

	val isFirstAppLaunch = appVersionCodePreference.get() == -1
	val appLaunchNumber
		get() = appLaunchNumberPreference.get()

	fun onAppLaunched() {
		updatePreferences()

		appLaunchNumberPreference.set(appLaunchNumberPreference.get() + 1)

		injector.acquisition.requestAcquisitionIfRequired()
	}

	private fun updatePreferences() {

		if (!isFirstAppLaunch) {

			val transaction = Transaction.begin()

			if (appVersionCodePreference.get() <= 1111) {
				transaction
						.remove("preference name 1")
						.remove("preference name 2")
			}

			if (appVersionCodePreference.get() <= 2222) {
				transaction
						.remove("preference name 3")
						.remove("preference name 4")
			}

			transaction.end()
		}
	}

	private fun loadModels() {
		if (::token.isInitialized) {
			return
		}

		token = injector.token
		profile = injector.profile

		deliveryAddress = injector.deliveryAddress
	}

	// need to call in onViewCreationStarted of splash activity. splash activity is a launcher activity
	fun onSplashStarted() {

		if (appVersionCodePreference.get() == -1) {
			// just installed (not updated)
			injector.analytics.appInstalled()
		}

		injector.analytics.appLaunched()

		if (isAppUpdating) {
			appVersionCodePreference.set(CommonUtils.getVersionCode(injector.applicationContext))
		}
	}

	val isAppUpdating: Boolean
		get() {
			val appVersionCodeFromPreference = appVersionCodePreference.get()
			val appVersionCode = CommonUtils.getVersionCode(injector.applicationContext)
			return appVersionCodeFromPreference != appVersionCode
		}

	// called from App
	fun login(isAccountCreated: Boolean, transaction: TransactionInterface) {

		loadModels()

		abortAndClearAuthorizedRequests(true, transaction)

		onAuthorizationChanged()
	}

	// called from App
	fun logout() {

		loadModels()

		Transaction.transaction {
			abortAndClearAuthorizedRequests(false, this)
		}

		onAuthorizationChanged()
	}

	fun onAuthorizationError() {
		abortAndClearAuthorizedRequests(false)
		onAuthorizationChanged()
	}

	private fun onAuthorizationChanged() {
	}

	fun abortAndClearIfRequiredRequestsForDeeplink() {

		loadModels()

		val transaction = Transaction.begin()

		profile.abort()

		transaction.end()
	}

	private fun abortAndClearAuthorizedRequests(afterLogin: Boolean) {

		Transaction.transaction {
			abortAndClearAuthorizedRequests(afterLogin, this)
		}
	}

	private fun abortAndClearAuthorizedRequests(afterLogin: Boolean, transaction: TransactionInterface) {

		loadModels()

		if (!afterLogin) {
			token.abort()
			token.clear(transaction)

			profile.abort()
			profile.clear(transaction)
		}

		deliveryAddress.abort()
		deliveryAddress.clear()
	}
}