package com.application.foundation.network

import androidx.annotation.VisibleForTesting
import kotlinx.coroutines.launch
import com.application.foundation.App.Companion.injector
import com.application.foundation.features.common.model.RequestBase
import com.application.foundation.features.common.model.RequestBase.OnAbortListener
import com.application.foundation.features.common.presenter.Router
import com.application.foundation.utils.CommonUtils
import com.application.foundation.utils.LogUtils
import okhttp3.*
import java.io.IOException

class WebRequest (

	override var request: Request,
	private val isAccessTokenRequired: Boolean,
	@get:VisibleForTesting val webResponseHandler: WebResponseHandler<*>?,
	val client: OkHttpClient

): WebRequestInterface {

	companion object {
		private var refreshedTokenTime: Long = 0
	}


	private val token = injector.token

	private var call: Call? = null


	class Builder : RequestBuilder() {

		private var isAccessTokenRequired = false

		override fun url(url: String): Builder {
			super.url(url)
			return this
		}

		override fun postForm(builder: FormBody.Builder): Builder {
			super.postForm(builder)
			return this
		}

		override fun postMultipart(builder: MultipartBody.Builder): Builder {
			super.postMultipart(builder)
			return this
		}

		override fun post(): Builder {
			super.post()
			return this
		}

		override fun delete(): Builder {
			super.delete()
			return this
		}

		override fun <T> bodyJson(method: HttpMethod, body: T): Builder {
			super.bodyJson(method, body)
			return this
		}

		override fun body(method: HttpMethod, contentType: String, body: String): Builder {
			super.body(method, contentType, body)
			return this
		}

		override fun addHeader(name: String, value: String): Builder {
			if (WebClient.ACCESS_TOKEN_HEADER_NAME == name) {
				isAccessTokenRequired = true
			}
			super.addHeader(name, value)
			return this
		}

		override fun cacheControl(cacheControl: CacheControl): Builder {
			super.cacheControl(cacheControl)
			return this
		}

		fun build(handler: WebResponseHandler<*>?, client: OkHttpClient): WebRequest {

			return WebRequest(request = build(),
					isAccessTokenRequired = isAccessTokenRequired,
					webResponseHandler = handler,
					client = client)
		}
	}

	fun send(isRefreshTokenRequest: Boolean) {

		if (!isRefreshTokenRequest && isAccessTokenRequired && token.isUpdating) {
			token.addListener(tokenListener, tokenAbortListener)

		} else {
			if (NetworkUtils.isConnected()) {
				call = client.newCall(request)
				call!!.enqueue(object : Callback {

					override fun onFailure(call: Call, e: IOException) {
						webResponseHandler?.onFailure(this@WebRequest, e)
					}

					@Throws(IOException::class)
					override fun onResponse(call: Call, response: Response) {

						if (response.code == 401 && !isRefreshTokenRequest) {
							// 401 - unauthorized. token has been expired

							injector.applicationScope.launch {

								if (!isAborted) {
									if (token.isTokenExists) {
										if (CommonUtils.currentTimeMillisForDuration - refreshedTokenTime > 5000) {
											token.addListener(tokenListener, tokenAbortListener)
											token.requestRefreshToken()

											LogUtils.log("refresh_token")
										} else {
											launchRequest()
										}

										response.close()
									} else {
										webResponseHandler?.onResponse(this@WebRequest, response)
									}
								} else {
									response.close()
								}
							}
						} else {
							webResponseHandler?.onResponse(this@WebRequest, response)
						}
					}
				})
			} else {
				onNetworkError()
			}
		}
	}

	private fun onNetworkError() {
		webResponseHandler?.onFailure(this, null)
	}

	private fun launchRequest() {
		if (isAccessTokenRequired) {
			// need to update request's access token
			val builder = request.newBuilder()
			builder.header(WebClient.ACCESS_TOKEN_HEADER_NAME, token.accessToken!!)
			request = builder.build()
		}
		send(false)
	}

	private val tokenAbortListener = OnAbortListener { abort() }

	private val tokenListener = object : RequestBase.Listener {

		override fun onStateChanged() {
			if (!token.isUpdating) {
				token.removeListener(this, tokenAbortListener, false)

				if (!isAborted) {
					if (token.error == null) {
						refreshedTokenTime = CommonUtils.currentTimeMillisForDuration
						launchRequest()

					} else if (token.error!!.isNetworkError) {
						onNetworkError()

					} else {
						LogUtils.logError("bad_token")

						injector.application.logout(false)
						Router.goToMainActivityClearTask(injector.applicationContext)
					}
				}
			}
		}
	}


	@Volatile
	override var isAborted: Boolean = false

	override fun abort() {
		isAborted = true

		call?.cancel()

		webResponseHandler?.job?.let {
			it.cancel()
			webResponseHandler.job = null
		}

		token.removeListener(tokenListener, tokenAbortListener, false)
	}

	enum class HttpMethod {
		Get, Post, Put, Patch, Delete
	}
}