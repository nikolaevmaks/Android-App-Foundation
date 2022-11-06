package com.application.foundation.features.profile.model

import androidx.annotation.AnyThread
import com.application.foundation.App.Companion.injector
import com.application.foundation.features.common.model.RequestBase
import com.application.foundation.features.common.model.preferences.PreferenceInterface
import com.application.foundation.features.common.model.preferences.PreferencesNames
import com.application.foundation.features.common.model.preferences.Transaction
import com.application.foundation.features.common.model.preferences.TransactionInterface
import com.application.foundation.features.common.model.utils.RequestError
import com.application.foundation.features.profile.model.dto.ProfileType
import com.application.foundation.features.profile.model.dto.ProfileWithTokenType
import com.application.foundation.network.RequestHandlerBaseResponse
import com.application.foundation.network.RequestHandlerBaseResponseNullBody
import java.util.*

class Profile : RequestBase<Any, Profile.State>() {

	companion object {

		@JvmStatic
		fun getProfilePreference(): PreferenceInterface<ProfileType?> {
			return injector.getObjectPreference(PreferencesNames.PROFILE, ProfileType::class.java, null)
		}

		////
		private val isAuthorizedPreference = injector.getBooleanPreference(PreferencesNames.IS_AUTHORIZED, false)
		private val userUUIDPreference = injector.getStringPreference<String?>(PreferencesNames.USER_UUID, null)

		@get:AnyThread
		var userUUID: String?
			get() = userUUIDPreference.get()
			set(uuid) {
				userUUIDPreference.set(uuid)
			}

		@get:AnyThread
		val isAuthorized: Boolean
			get() = isAuthorizedPreference.get()
	}



	private val profilePreference = getProfilePreference()


	// required for analytics
	private val idPreference = injector.getLongPreference<Long?>(PreferencesNames.PROFILE_ID, null)
	private val emailPreference = injector.getStringPreference<String?>(PreferencesNames.PROFILE_EMAIL, null)

	private val token: Token



	val data: ProfileType?
		get() = profilePreference.get()



	@get:AnyThread
	val id: Long?
		get() = idPreference.get()

	@get:AnyThread
	val userEmail: String?
		get() = emailPreference.get()

	init {
		token = injector.token
	}


	override fun clear() {
		super.clear()

		throw RuntimeException("not supported!")
	}

	fun clear(transaction: TransactionInterface) {
		super.clear()

		transaction.put(isAuthorizedPreference, false)
				   .put(profilePreference, null)
				   .put(idPreference, null)
			       .put(emailPreference, null)

		injector.application.updateCrashlyticsUser()

//		injector.intercom.logout()
//		injector.intercom.registerUnidentifiedUser()
//
//		injector.firebaseAnalytics.setUserId(null)
//
//		YandexMetrica.reportUserProfile(UserProfile.newBuilder()
//				.apply(Attribute.customString("id").withValueReset())
//				.apply(Attribute.customString("email").withValueReset())
//				.build())
	}


	fun requestLogin(login: String, password: String) {

		if (!isUpdating) {
			startRequest(State.Login,
					injector.webClient.requestLogin(login, password,

							object : RequestHandlerBaseResponse<ProfileWithTokenType>(ProfileWithTokenType::class.java, this@Profile) {

								override fun onReceiveValidResult(body: ProfileWithTokenType) {
									handleAuthorizationResponse(body, false)
								}

								override fun onError(error: RequestError<*>) {

									injector.models.onAuthorizationError()

									super.onError(error)
								}
							}))
		}
	}

	fun requestRegister(firstName: String, lastName: String, email: String, password: String) {

		if (!isUpdating) {
			startRequest(State.Register,
					injector.webClient.requestRegister(firstName, lastName, email, password,

							object : RequestHandlerBaseResponse<ProfileWithTokenType>(ProfileWithTokenType::class.java, this@Profile) {

								override fun onReceiveValidResult(body: ProfileWithTokenType, responseCode: Int) {
									handleAuthorizationResponse(body, responseCode == 201)
								}

								override fun onError(error: RequestError<*>) {

									injector.models.onAuthorizationError()

									super.onError(error)
								}
							}))
		}
	}

	private fun handleAuthorizationResponse(body: ProfileWithTokenType, isAccountCreated: Boolean) {

		val transaction = Transaction.begin()
				.put(profilePreference, body)
				.put(isAuthorizedPreference, true)

		token.putInTransaction(transaction, body.token)

		updateUserInfo(transaction)

		injector.application.login(isAccountCreated, transaction)

		transaction.end()

		stopRequest()
	}

	fun requestPasswordResetCode(login: String) {
		if (!isUpdating) {
			startRequest(State.PasswordResetCode,
				injector.webClient.requestPasswordResetSendCode(login,
					object : RequestHandlerBaseResponseNullBody(this@Profile) {

						override fun onReceiveValidResult() {
							stopRequest()
						}
					}))
		}
	}


	private fun updateUserInfo(transaction: TransactionInterface) {

		injector.deliveryAddress.updateFromProfileIfEmpty()

		val id = data!!.id
		val email = data!!.email

//		var intercomRegistration: Registration? = null
//
//		if (id != this.id) {
//			injector.firebaseAnalytics.setUserId(id.toString())
//
//			intercomRegistration = Registration.create().withUserId(id.toString())
//		}
//
//		if (email != null && email != userEmail) {
//
//			if (intercomRegistration == null) {
//				intercomRegistration = Registration.create()
//			}
//			intercomRegistration!!.withEmail(email)
//		}
//
//		if (intercomRegistration != null) {
//			injector.intercom.registerIdentifiedUser(intercomRegistration)
//		}
//
//
//		val builderAppMetrica = UserProfile.newBuilder()
//				.apply(Attribute.customString("id").withValue(id.toString()))
//		if (email == null) {
//			builderAppMetrica.apply(Attribute.customString("email").withValueReset())
//		} else {
//			builderAppMetrica.apply(Attribute.customString("email").withValue(email))
//		}
//		YandexMetrica.reportUserProfile(builderAppMetrica.build())

		if (id != this.id) {
			transaction.put(idPreference, id)
		}
		if (email != userEmail) {
			transaction.put(emailPreference, email)
		}

		injector.application.updateCrashlyticsUser()
	}

	enum class State {
		Login, Register, PasswordResetCode
	}
}