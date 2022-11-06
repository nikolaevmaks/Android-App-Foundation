package com.application.foundation.network

import androidx.annotation.AnyThread
import com.application.foundation.features.profile.model.dto.ProfileType
import okhttp3.OkHttpClient

interface WebClientInterface {

	val client: OkHttpClient

	@AnyThread
	fun populateHeaders(builder: RequestBuilder, accessTokenRequiredIfExists: Boolean)

	@AnyThread
	fun populateHeaders(builder: RequestBuilder, accessToken: String?)


	fun requestLogin(login: String, password: String, handler: WebResponseHandler<*>): WebRequestInterface

	fun requestRegister(firstName: String, lastName: String, email: String, password: String,
						handler: WebResponseHandler<*>): WebRequestInterface

	fun requestPasswordResetSendCode(login: String, handler: WebResponseHandler<*>): WebRequestInterface

	fun requestRefreshToken(handler: WebResponseHandler<*>): WebRequestInterface


	fun requestProducts(categoryId: Long, page: Int, productCountPerPage: Int, handler: WebResponseHandler<*>): WebRequestInterface


	fun requestDeliveryAddress(store: ProfileType.Store, handler: WebResponseHandler<*>): WebRequestInterface

	fun requestDeliveryAddressAdd(firstName: String?,
								  lastName: String?,
								  countryCode: String?,
								  city: String?,
								  streetAddress1: String?,
								  streetAddress2: String?,
								  postcode: String?,
								  state: String?,
								  handler: WebResponseHandler<*>): WebRequestInterface

	fun requestAcquisition(acquisitionSource: String, handler: WebResponseHandler<*>): WebRequestInterface

	fun requestStripeKey(handler: WebResponseHandler<*>): WebRequestInterface
}