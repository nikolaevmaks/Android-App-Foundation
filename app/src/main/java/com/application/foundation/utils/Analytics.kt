package com.application.foundation.utils

import android.os.Bundle
import androidx.annotation.AnyThread
import com.application.foundation.features.common.view.MainActivity
import com.application.foundation.features.profile.view.ProfileFragment
import com.application.foundation.features.shop.view.ProductsFragment
import java.util.HashMap
import java.util.regex.Pattern

class Analytics {

	private val delayedEvents: MutableMap<String, MutableMap<String, String?>> = HashMap()

	companion object {

		private fun mapToFirebaseBundle(attributes: Map<String, String?>): Bundle {
			return Bundle().apply {
				for ((key, value) in attributes) {
					putString(prepareNameForFirebaseAnalytics(key), value)
				}
			}
		}

		private val FIREBASE_ANALYTICS_NAME_PATTERN = Pattern.compile("\\W")

		private fun prepareNameForFirebaseAnalytics(name: String): String {
			// https://firebase.google.com/docs/reference/android/com/google/firebase/analytics/FirebaseAnalytics.Event
			// https://developer.android.com/reference/java/util/regex/Pattern.html
			return FIREBASE_ANALYTICS_NAME_PATTERN.matcher(name).replaceAll("_")
		}


		private fun createAttributes(vararg attributes: Pair<String, String?>): MutableMap<String, String?> {
			return hashMapOf(*attributes)
		}


		private const val ERROR_EVENT_NAME = "Error"
	}


	private enum class Screen {
		Main,
		Products,
		Profile
	}

	private val screens: Map<Class<*>, Screen> = hashMapOf(
		MainActivity::class.java to Screen.Main,
		ProductsFragment::class.java to Screen.Products,
		ProfileFragment::class.java to Screen.Profile
	)



	fun screenOpened(screenObject: Any, attributes: Map<String, String?>?) {

		val screen = screens[screenObject.javaClass]
		if (screen != null) {
			when (screen) {
				Screen.Main -> tagScreen("Main", attributes)
				Screen.Products -> tagScreen("Products", attributes)
				Screen.Profile -> tagScreen("Profile", attributes)
			}
		}
	}

	fun tagScreen(screen: String) {
		tagScreen(screen, null)
	}

	fun tagScreen(screen: String, attributes: Map<String, String?>?) {
		if (attributes == null) {
			tagEvent(screen)
		} else {
			tagEvent(screen, attributes)
		}
	}

	private val defaultAttributes: MutableMap<String, String?> = HashMap(0)


	private fun tagEvent(eventName: String,
						 attributes: Map<String, String?> = defaultAttributes,
						 customerValueIncrease: Long = 0,
						 useDelayedEvent: Boolean = true) {

		var attributes = attributes

		if (useDelayedEvent) {
			val delayedEvent: MutableMap<String, String?>? = delayedEvents[eventName]
			if (delayedEvent != null) {

				delayedEvent.putAll(attributes)
				attributes = delayedEvent

				clearDelayedEvent(eventName)
			}
		}

		populateBaseAttributes(attributes)

		logToAppMetrica(eventName, attributes)
		logToIntercom(eventName, attributes)

		logToFirebaseAnalytics(eventName, attributes)

		// nice to have events with crash
		logToCrashlytics(eventName, attributes)
	}

	private fun populateBaseAttributes(attributes: Map<String, String?>) {}


	private fun logToAppMetrica(eventName: String, attributes: Map<String, String?>) {

		val map = buildMap<String, Any?>(attributes.size) {
			attributes.forEach {
				put(it.key, it.value)
			}
		}

		//YandexMetrica.reportEvent(eventName, map)
	}

	private fun logToAppAnalytics(eventName: String, attributes: Map<String, String?>) {
		//analyticsEventsHandler.logEvent(eventName, attributes)
	}

	private fun logToFirebaseAnalytics(eventName: String, attributes: Map<String, String?>) {
		//firebaseAnalytics.logEvent(prepareNameForFirebaseAnalytics(eventName), mapToFirebaseBundle(attributes))
	}

	private fun logToIntercom(eventName: String, attributes: Map<String, String?>) {
		//intercom.logEvent(eventName, attributes)
	}

	private fun logToCrashlytics(eventName: String, attributes: Map<String, String?>) {
		//injector.crashlytics.log("$eventName   $attributes")
	}



	private fun clearDelayedEvent(delayedEvent: String) {
		delayedEvents.remove(delayedEvent)
	}

	private fun setDelayedAttribute(delayedEvent: String, attribute: String, value: String?) {
		prepareDelayedAttribute(delayedEvent)[attribute] = value
	}

	private fun setDelayedAttribute(delayedEvent: String, attribute: String, value: Int?) {
		prepareDelayedAttribute(delayedEvent)[attribute] = value?.toString()
	}

	private fun setDelayedAttribute(delayedEvent: String, attribute: String, value: Long?) {
		prepareDelayedAttribute(delayedEvent)[attribute] = value?.toString()
	}

	private fun setDelayedAttribute(delayedEvent: String, attribute: String, value: Boolean?) {
		prepareDelayedAttribute(delayedEvent)[attribute] = value?.toString()
	}


	private fun prepareDelayedAttribute(delayedEvent: String): MutableMap<String, String?> {
		return delayedEvents[delayedEvent] ?: HashMap<String, String?>().also {
			delayedEvents[delayedEvent] = it
		}
	}

	private fun getDelayedAttributeString(delayedEvent: String, attribute: String): String? {
		return delayedEvents[delayedEvent]?.get(attribute)
	}

	private fun getDelayedAttributeInt(delayedEvent: String, attribute: String): Int? {
		return getDelayedAttributeString(delayedEvent, attribute)?.toInt()
	}

	private fun getDelayedAttributeIntNotNull(delayedEvent: String, attribute: String): Int {
		return getDelayedAttributeInt(delayedEvent, attribute) ?: 0
	}

	private fun getDelayedAttributeBoolean(delayedEvent: String, attribute: String): Boolean? {
		return getDelayedAttributeString(delayedEvent, attribute)?.let {
			"true" == it
		}
	}


	private fun sendDelayedEvent(delayedEvent: String, remove: Boolean = true) {
		sendDelayedEvent(delayedEvent, 0, remove)
	}

	private fun sendDelayedEvent(delayedEvent: String, customerValueIncrease: Long, remove: Boolean) {
		val attributes = delayedEvents[delayedEvent]
		if (attributes != null) {
			tagEvent(delayedEvent, attributes, customerValueIncrease, useDelayedEvent = false)
			if (remove) {
				clearDelayedEvent(delayedEvent)
			}
		}
	}



	enum class Source {
		Home, Profile
	}



	fun connectWithFacebookClicked(source: Source) {
		tagEvent("Connect with Facebook button", createAttributes("source" to source.name))
	}

	fun signUpWithEmailClicked(source: Source) {
		tagEvent("Sign Up with Email button", createAttributes("source" to source.name))
	}



	// Registration

	fun startRegistration(source: Source) {
		setRegistrationSource(source)
		setAuthorizationSource(source)
	}

	private fun setRegistrationSource(source: Source) {
		setDelayedAttribute("Registration successful", "source", source.name)
	}

	fun setRegistrationType(byFacebook: Boolean) {
		setDelayedAttribute("Registration successful", "type", if (byFacebook) "facebook" else "email")
	}

	fun setRegistrationEmail(email: String?) {
		setDelayedAttribute("Registration successful", "email", email)
	}

	fun setRegistrationFromInvite() {
		setDelayedAttribute("Registration successful", "from invite", true)
	}

	fun sendRegistration() {
		sendDelayedEvent("Registration successful")
	}


	// Authorization

	private fun setAuthorizationSource(source: Source) {
		setDelayedAttribute("Login successful", "source", source.name)
	}

	fun setAuthorizationType(byFacebook: Boolean) {
		setDelayedAttribute("Login successful", "type", if (byFacebook) "facebook" else "email")
	}

	fun setAuthorizationEmail(email: String?) {
		setDelayedAttribute("Login successful", "email", email)
	}

	fun setAuthorizationFromInvite() {
		setDelayedAttribute("Login successful", "from invite", true)
	}

	fun sendAuthorization() {
		sendDelayedEvent("Login successful")
	}


	///

	fun tabBarClicked(button: Source, prevSource: Source) {
		tagEvent("Tab bar", createAttributes("button" to button.name, "source" to prevSource.name))
	}




	///

	fun sendLogout() {
		tagEvent("Logout")
	}



	fun appLaunched() {
		tagEvent("Launch event")
	}

	fun appInstalled() {
		tagEvent("Install event")
	}

	fun appUpdated(versionCode: Int) {
		tagEvent("App update event", createAttributes("version code" to versionCode.toString()))
	}




	@AnyThread
	fun logError(target: String) {
		logError(target, null as String?)
	}

	@AnyThread
	fun logError(target: String, message: String?) {
		logError(target, message, null)
	}

	@AnyThread
	fun logError(target: String, message: String?, attributes: Map<String, String?>?) {

		var newAttributes: MutableMap<String, String?>? = null
		if (message != null) {
			newAttributes = if (attributes == null) HashMap(mapCapacity(2)) else HashMap(mapCapacity(attributes.size + 2))
			if (attributes != null) {
				newAttributes.putAll(attributes)
			}

			newAttributes["message"] = message
		}

		logErrorStaff(target, attributes, newAttributes)
	}

	@AnyThread
	fun logError(target: String, attributes: Map<String, String?>?) {
		logErrorStaff(target, attributes, null)
	}

	@AnyThread
	private fun logErrorStaff(target: String, attributes: Map<String, String?>?, newAttributes: MutableMap<String, String?>?) {

		val map = newAttributes ?: if (attributes == null) HashMap(mapCapacity(1)) else
				HashMap<String, String?>(mapCapacity(attributes.size + 1)).apply { putAll(attributes) }

		populateBaseErrorAttributes(target, map)

		tagEvent(ERROR_EVENT_NAME, map)
	}


	private fun populateBaseErrorAttributes(target: String, attributes: MutableMap<String, String?>) {
		attributes["target"] = target
	}



	@AnyThread
	fun logWebResponseError(
			target: String, message: String?, request: String?,
			method: String, url: String, responseCode: Int?, response: String?
	) {
		var attributes: MutableMap<String, String?> = HashMap(mapCapacity(7))

		populateBaseAttributes(attributes)

		attributes["target"] = target
		attributes["message"] = message

		attributes["response code"] = responseCode?.toString()
		attributes["method"] = method
		attributes["url"] = url

		logToAppMetrica(ERROR_EVENT_NAME, attributes)
		logToFirebaseAnalytics(ERROR_EVENT_NAME, attributes)


		// request and response can be very long
		attributes = HashMap(attributes)
		attributes["request"] = request
		attributes["response"] = response

		logToIntercom(ERROR_EVENT_NAME, attributes)
		logToAppAnalytics(ERROR_EVENT_NAME, attributes)
		// nice to have events with crash
		logToCrashlytics(ERROR_EVENT_NAME, attributes)
	}
}