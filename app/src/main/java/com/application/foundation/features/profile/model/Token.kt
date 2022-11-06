package com.application.foundation.features.profile.model

import com.application.foundation.App.Companion.injector
import com.application.foundation.features.common.model.RequestBase
import com.application.foundation.features.common.model.preferences.PreferencesNames
import com.application.foundation.features.common.model.preferences.Transaction
import com.application.foundation.features.common.model.preferences.TransactionInterface
import com.application.foundation.features.profile.model.dto.TokenType
import com.application.foundation.network.RequestHandlerBaseResponse

class Token : RequestBase<Any, Any>() {

	private val accessTokenPreference = injector.getObjectPreference<TokenType?>(PreferencesNames.ACCESS_TOKEN, TokenType::class.java, null)

	override fun clear() {
		super.clear()

		Transaction.transaction {
			clear(this)
		}
	}

	fun clear(transaction: TransactionInterface) {
		super.clear()

		transaction.put(accessTokenPreference, null)
	}

	fun requestRefreshToken() {
		if (!isUpdating) {
			startRequest(injector.webClient.requestRefreshToken(object : RequestHandlerBaseResponse<TokenType>(TokenType::class.java, this@Token) {

				override fun onReceiveValidResult(body: TokenType) {
					accessTokenPreference.set(TokenType(body.accessToken, body.expires, body.refreshToken))

					stopRequest()
				}
			}))
		}
	}

	val isTokenExists: Boolean
		get() = accessTokenPreference.get() != null

	val data: TokenType?
		get() = accessTokenPreference.get()

	val accessToken: String?
		get() = data?.accessToken


	fun putInTransaction(transaction: TransactionInterface, token: TokenType) {
		transaction.put(accessTokenPreference, token)
	}
}