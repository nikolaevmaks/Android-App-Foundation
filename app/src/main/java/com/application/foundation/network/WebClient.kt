package com.application.foundation.network

import android.content.Context
import androidx.annotation.AnyThread
import androidx.annotation.VisibleForTesting
import com.facebook.stetho.okhttp3.StethoInterceptor
import com.application.foundation.App.Companion.injector
import com.application.foundation.Environment
import com.application.foundation.features.common.model.DetectCountry
import com.application.foundation.features.common.model.dto.AcquisitionRequest
import com.application.foundation.features.common.view.utils.Formatter
import com.application.foundation.features.profile.model.dto.*
import com.application.foundation.utils.CommonUtils
import okhttp3.*
import okhttp3.logging.HttpLoggingInterceptor
import java.io.File
import java.util.*
import java.util.concurrent.TimeUnit

// context == null during testing
class WebClient(private val context: Context?): WebClientInterface {

	companion object {
		private val TAG = WebClient::class.java.simpleName

		private const val DISK_CACHE_NAME = "okhttp-app-cache"
		private const val DISK_CACHE_SIZE = 20 * 1024 * 1024 // 20MB

		const val ACCESS_TOKEN_HEADER_NAME = "X-Access-Token"

		private const val PRODUCTION_HOST = "https://production.com"
		private const val DEVELOPMENT_HOST = "https://test.com"

		private const val PATH_LOGIN = "/api/v1/users/login"
		private const val PATH_REGISTER = "/api/v1/users/register"
		private const val PATH_REFRESH_TOKEN = "/api/v1/users/refresh_token"
		private const val PATH_PASSWORD_RESET_SEND_CODE = "/api/v1/users/password/reset"

		private const val PATH_DELIVERY_ADDRESS = "/api/v1/users/address"
		private const val PATH_DELIVERY_ADDRESS_ADD = "/api/v1/users/address"

		private const val PATH_ACQUISITION = "/api/v1/users/acquisition"

		private const val PATH_STRIPE_KEY = "/api/v1/stripe"

		private const val PATH_PRODUCTS = "/api/v1/categories/%s/products?page=%s&per_page=%s"

		@JvmStatic
		val baseUrl: String
			get() = if (Environment.IS_DEVELOPMENT) DEVELOPMENT_HOST else PRODUCTION_HOST


		@JvmStatic
		fun getStoreCode(store: ProfileType.Store?): String {
			return when (store) {
				ProfileType.Store.UK -> DetectCountry.UK_COUNTRY_CODE
				ProfileType.Store.US -> DetectCountry.US_COUNTRY_CODE
				ProfileType.Store.WW -> "WW"
				else -> "WW" // not possible
			}
		}
	}


	override val client: OkHttpClient

	private val isTesting: Boolean = context == null

	@VisibleForTesting
	var lastRequest: WebRequest? = null
		private set

	init {
		val builder = OkHttpClient.Builder()

		builder.connectTimeout(30, TimeUnit.SECONDS)
				.readTimeout(30, TimeUnit.SECONDS)
				.writeTimeout(30, TimeUnit.SECONDS)

		if (!isTesting) {
			builder.cache(Cache(createCacheDir(), DISK_CACHE_SIZE.toLong()))
		}

		if (Environment.IS_DEVELOPMENT || Environment.ENABLE_LOGGING) {
			val logging = HttpLoggingInterceptor()
			logging.setLevel(HttpLoggingInterceptor.Level.BODY)
			builder.addInterceptor(logging)
		}

		builder.addNetworkInterceptor(StethoInterceptor())

		client = builder.build()
	}

	private fun createCacheDir(): File {
		val cache = File(context!!.cacheDir, DISK_CACHE_NAME)
		if (!cache.exists()) {
			cache.mkdirs()
		}
		return cache
	}

	private fun send(builder: WebRequest.Builder, handler: WebResponseHandler<*>?): WebRequestInterface {
		return send(builder, client, true, handler)
	}

	private fun send(builder: WebRequest.Builder, accessTokenRequiredIfExists: Boolean, handler: WebResponseHandler<*>?): WebRequestInterface {
		return send(builder, client, accessTokenRequiredIfExists, handler)
	}

	private fun send(builder: WebRequest.Builder,
					 client: OkHttpClient = this.client,
					 accessTokenRequiredIfExists: Boolean = true,
					 handler: WebResponseHandler<*>?): WebRequestInterface {

		return send(builder,
				client,
				if (accessTokenRequiredIfExists) injector.token.accessToken else null,
				handler)
	}

	private fun send(builder: WebRequest.Builder,
					 client: OkHttpClient = this.client,
					 accessToken: String?,
					 handler: WebResponseHandler<*>?): WebRequestInterface {

		populateHeaders(builder, accessToken)

		val request = builder.build(handler, client)

		if (isTesting) {
			lastRequest = request
		} else {
			request.send(request.request.url.toString() == baseUrl + PATH_REFRESH_TOKEN)
		}

		return request
	}

	@AnyThread
	override fun populateHeaders(builder: RequestBuilder, accessTokenRequiredIfExists: Boolean) {
		populateHeaders(builder, if (accessTokenRequiredIfExists) injector.token.accessToken else null)
	}

	@AnyThread
	override fun populateHeaders(builder: RequestBuilder, accessToken: String?) {

		builder.addHeader("Accept-Charset", "UTF-8")
				.addHeader("Accept", "application/json")

		if (!isTesting) {
			builder.addHeader("User-Agent",
					StringBuilder()
							.append("App Android")
							.append(" v")
							.append(CommonUtils.getVersionName(context!!))
							.toString())

		}

		if (accessToken != null) {
			builder.addHeader(ACCESS_TOKEN_HEADER_NAME, accessToken)
		}

		val uuid = injector.userUUID
		if (uuid != null) {
			builder.addHeader("X-Uuid", uuid)
		}
	}


	private fun getOkHttpClient(timeoutMinutes: Int): OkHttpClient {
		return client.newBuilder()
				.connectTimeout(timeoutMinutes.toLong(), TimeUnit.MINUTES)
				.readTimeout(timeoutMinutes.toLong(), TimeUnit.MINUTES)
				.writeTimeout(timeoutMinutes.toLong(), TimeUnit.MINUTES)
				.build()
	}

	override fun requestLogin(login: String, password: String, handler: WebResponseHandler<*>): WebRequestInterface {

		val builder = WebRequest.Builder()
				.url(baseUrl + PATH_LOGIN)

		val request = LoginRequest(
			login = login,
			password = password
		)

		builder.bodyJson(WebRequest.HttpMethod.Post, request)
		return send(builder, false, handler)
	}

	override fun requestRegister(firstName: String, lastName: String, email: String, password: String,
						handler: WebResponseHandler<*>): WebRequestInterface {

		val builder = WebRequest.Builder()
				.url(baseUrl + PATH_REGISTER)

		val request = RegistrationRequest(
			firstName = firstName,
			lastName = lastName,
			email = email,
			password = password
		)

		builder.bodyJson(WebRequest.HttpMethod.Post, request)
		return send(builder, false, handler)
	}


	override fun requestPasswordResetSendCode(login: String, handler: WebResponseHandler<*>): WebRequestInterface {

		val builder = WebRequest.Builder()
				.url(baseUrl + PATH_PASSWORD_RESET_SEND_CODE)

		val request = ResetPasswordSendCodeRequest(login)

		builder.bodyJson(WebRequest.HttpMethod.Post, request)
		return send(builder, false, handler)
	}



	override fun requestRefreshToken(handler: WebResponseHandler<*>): WebRequestInterface {

		val builder = WebRequest.Builder()
				.url(baseUrl + PATH_REFRESH_TOKEN)

		val token = injector.token.data
		val request = RefreshTokenRequest(token!!.refreshToken)

		builder.bodyJson(WebRequest.HttpMethod.Post, request)
		return send(builder, false, handler)
	}


	override fun requestProducts(categoryId: Long, page: Int, productCountPerPage: Int, handler: WebResponseHandler<*>): WebRequestInterface {

		val builder = WebRequest.Builder()
				.url(baseUrl +
						Formatter.formatStringForMachine(PATH_PRODUCTS, categoryId, page, productCountPerPage))
		return send(builder, handler)
	}


	override fun requestDeliveryAddress(store: ProfileType.Store, handler: WebResponseHandler<*>): WebRequestInterface {
		val builder = WebRequest.Builder()
				.url(baseUrl + Formatter.formatStringForMachine(PATH_DELIVERY_ADDRESS, getStoreCode(store)))

		return send(builder, handler)
	}

	override fun requestDeliveryAddressAdd(firstName: String?,
								  lastName: String?,
								  countryCode: String?,
								  city: String?,
								  streetAddress1: String?,
								  streetAddress2: String?,
								  postcode: String?,
								  state: String?,
								  handler: WebResponseHandler<*>): WebRequestInterface {
		val builder = WebRequest.Builder()
				.url(baseUrl + PATH_DELIVERY_ADDRESS_ADD)

		builder.bodyJson(WebRequest.HttpMethod.Post, DeliveryAddressType(
				firstName,
				lastName,
				countryCode,
				city,
				streetAddress1,
				streetAddress2,
				postcode,
				state)
		)

		return send(builder, handler)
	}

	override fun requestAcquisition(acquisitionSource: String, handler: WebResponseHandler<*>): WebRequestInterface {

		val builder = WebRequest.Builder()
			.url(baseUrl + PATH_ACQUISITION)

		val request = AcquisitionRequest(acquisitionSource)

		builder.bodyJson(WebRequest.HttpMethod.Post, request)
		return send(builder, handler)
	}

	override fun requestStripeKey(handler: WebResponseHandler<*>): WebRequestInterface {

		val builder = WebRequest.Builder().url(baseUrl + PATH_STRIPE_KEY)
		return send(builder, handler)
	}
}