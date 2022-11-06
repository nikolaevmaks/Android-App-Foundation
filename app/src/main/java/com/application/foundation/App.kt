package com.application.foundation

import android.app.Application
import android.content.res.Configuration
import androidx.annotation.AnyThread
import androidx.annotation.VisibleForTesting
import com.application.foundation.features.common.model.preferences.TransactionInterface
import com.application.foundation.features.common.view.utils.Formatter
import com.application.foundation.features.profile.model.Profile
import com.application.foundation.utils.Analytics
import com.application.foundation.utils.CommonUtils
import com.application.foundation.utils.TimeUtils
import com.facebook.stetho.Stetho
import java.util.*

class App : Application() {

	companion object {

		lateinit var instance: App
			private set

		@JvmStatic
		@get:AnyThread
		@set:VisibleForTesting
		lateinit var injector: InjectorInterface

		@JvmStatic
		val isTesting: Boolean
			get() = !::instance.isInitialized
	}

	private var locale: String? = null
	private lateinit var analytics: Analytics


	override fun onCreate() {
		super.onCreate()

		// need for AppMetrica. it uses multiple processes
		// see https://appmetrica.yandex.com/docs/mobile-sdk-dg/concepts/android-errors.html#android-errors__third-party-library-init
		if (CommonUtils.isMainProcess(this)) {
			instance = this

			injector = Injector()


			if (injector.userUUID == null) {
				Profile.userUUID = CommonUtils.generateUUID()
			}
			updateCrashlyticsUser()

			initIntercom()
			initAppMetrica()
			initAnalyticsDimensions()

			initLocaleAware()

			if (Environment.IS_DEVELOPMENT || Environment.ENABLE_LOGGING) {
				Stetho.initializeWithDefaults(this)
			}

			injector.models.onAppLaunched()

			analytics = injector.analytics
		}
	}

	private fun initLocaleAware() {

		locale = Locale.getDefault().toString()

		Formatter.init()
		TimeUtils.init()
	}


	private fun initIntercom() {

		//Intercom.initialize(this, getString(R.string.intercom_api_key), getString(R.string.intercom_app_id))

//		if (!injector.isAuthorized) {
//			injector.intercom.registerUnidentifiedUser()
//		}
	}

	private fun initAppMetrica() {
//		val config = YandexMetricaConfig.newConfigBuilder(getString(R.string.app_metrica_api_key))
//			.handleFirstActivationAsUpdate(!injector.models.isFirstAppLaunch)
//			.build()
//
//		YandexMetrica.activate(this, config)
//		YandexMetrica.enableActivityAutoTracking(this)
	}

	private fun initAnalyticsDimensions() {

		val screenType = CommonUtils.getScreenType(injector.applicationContext).toString()
		val uuid = CommonUtils.getMd5Hash(injector.userUUID!!)

//		val builderIntercom = UserAttributes.Builder()
//			.withCustomAttribute(ANALYTICS_SCREEN_TYPE_FLAG, screenType)
//			.withCustomAttribute(ANALYTICS_UUID_FLAG, uuid)
//		injector.intercom.updateUser(builderIntercom.build())
//
//		injector.firebaseAnalytics.setUserProperty(ANALYTICS_SCREEN_TYPE_FLAG, screenType)
//		injector.firebaseAnalytics.setUserProperty(ANALYTICS_UUID_FLAG, uuid)
//
//		val builderAppMetrica = UserProfile.newBuilder()
//			.apply(Attribute.customString(ANALYTICS_SCREEN_TYPE_FLAG).withValue(screenType))
//			.apply(Attribute.customString(ANALYTICS_UUID_FLAG).withValue(uuid))
//		YandexMetrica.reportUserProfile(builderAppMetrica.build())
	}

	fun updateCrashlyticsUser() {
		val profileId = injector.profile.id

//		injector.crashlytics.setUserId(profileId?.toString() ?: "")
//		injector.crashlytics.setCustomKey("UUID", injector.userUUID!!)
//		injector.crashlytics.setCustomKey("email", injector.profile.userEmail ?: "")
	}

	fun login(isAccountCreated: Boolean, transaction: TransactionInterface) {

		injector.models.login(isAccountCreated, transaction)

		val profile = injector.profile.data

		analytics.apply {

			if (isAccountCreated) {
				setRegistrationType(profile!!.facebookId != null)
				setRegistrationEmail(profile.email)

				sendRegistration()
			}

			setAuthorizationType(profile!!.facebookId != null)
			setAuthorizationEmail(profile.email)

			sendAuthorization()
		}
	}

	fun logout(manually: Boolean) {
		if (injector.isAuthorized) {

			injector.models.logout()

			analytics.sendLogout()
		}
	}


	override fun onConfigurationChanged(newConfig: Configuration) {
		super.onConfigurationChanged(newConfig)

		val locale = Locale.getDefault().toString()
		if (locale != this.locale) {
			initLocaleAware()
		}
	}
}