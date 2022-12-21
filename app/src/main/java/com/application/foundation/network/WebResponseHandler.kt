package com.application.foundation.network

import com.application.foundation.App.Companion.injector
import kotlinx.coroutines.*
import com.application.foundation.Environment
import com.application.foundation.features.common.model.dto.BaseResponse
import com.application.foundation.features.common.model.utils.RequestError
import com.application.foundation.utils.CommonUtils
import com.application.foundation.utils.LogUtils
import okhttp3.Headers
import okhttp3.Response
import okio.BufferedSource
import java.io.IOException

abstract class WebResponseHandler<T> {

	companion object {

		@JvmField
		val TAG = WebResponseHandler::class.java.simpleName

		private const val REQUEST_DELAY_DEVELOPMENT = 0L

		@JvmField
		val REQUEST_DELAY = if (Environment.IS_DEVELOPMENT) REQUEST_DELAY_DEVELOPMENT else 0L

		@JvmStatic
		fun isSuccessfulResponse(responseCode: Int): Boolean {
			return responseCode in 200..299
		}


		fun logJsonValidationError(message: String?, request: WebRequestInterface, responseCode: Int) {
			injector.applicationScope.launch(Dispatchers.IO) {
				WebResponseHandlerUtils.logJsonValidationError(message, request, responseCode)
			}
		}
	}


	private var headers: Headers? = null

	var job: Job? = null


	fun onFailure(request: WebRequestInterface, exception: IOException?) {
		headers = null
		sendNetworkError(request, null, exception)
	}

	fun onResponse(request: WebRequestInterface, response: Response) {
		headers = response.headers

		job = injector.applicationScope.launch(Dispatchers.Default) {
			val source = response.body!!.source()
			parse(request, source, response.code)
			response.close()
		}
	}


	fun sendNetworkError(request: WebRequestInterface, responseCode: Int?, exception: Exception?) {
		logWebResponseError(exception?.toString(), request, responseCode, null, true)

		send(request, null, null, networkError = true, unknownError = false, responseCode, exception)
	}

	fun sendUnknownError(request: WebRequestInterface, responseCode: Int, exception: Exception?) {
		send(request, null, null, networkError = false, unknownError = true, responseCode, exception)
	}

	fun send(request: WebRequestInterface, body: T?, error: BaseResponse.Error?, responseCode: Int?) {
		send(request, body, error, networkError = false, unknownError = false, responseCode, null)
	}


	fun sendTestResponse(request: WebRequestInterface, body: T?, responseCode: Int? = 200, delay: Long = REQUEST_DELAY) {
		send(request, body, error = null, networkError = false, unknownError = false, responseCode, exception = null, delay)
	}

	private fun send(request: WebRequestInterface, body: T?, error: BaseResponse.Error?, networkError: Boolean, unknownError: Boolean,
					 responseCode: Int?, exception: Exception?, delay: Long = REQUEST_DELAY) {

		job = CommonUtils.launchDelayed(delay) {

			job = null

			val url = request.request.url.toString()

			if (request.isAborted) {
				LogUtils.logD(TAG, "Request abort:    $url")
				return@launchDelayed
			}

			if (networkError) {
				LogUtils.logD(TAG, "Request network error " + getError(responseCode, exception, url))
				onNetworkError()

			} else if (unknownError) {
				LogUtils.logD(TAG, "Request unknown error " + getError(responseCode, exception, url))
				onUnknownError()

			} else {
				LogUtils.logD(TAG, "Request complete " + getError(responseCode, exception, url))
				onReceiveResult(body, error, responseCode!!, request)
			}
		}
	}

	private fun getError(responseCode: Int?, exception: Exception?, url: String): String {
		return if (responseCode == null && exception == null) {
			url
		} else if (responseCode != null) {
			"(responseCode: " + responseCode + (if (exception == null) "" else ", exception: $exception") + "):    " + url
		} else {
			"(exception: " + exception.toString() + "):    " + url
		}
	}

	protected abstract fun parse(request: WebRequestInterface, source: BufferedSource, responseCode: Int)

	protected abstract fun onReceiveResult(body: T?, error: BaseResponse.Error?, responseCode: Int, request: WebRequestInterface)


	fun onNetworkError() {
		onError(RequestError.networkError<Any>())
	}

	fun onUnknownError() {
		onError(RequestError.unknownError<Any>())
	}

	protected open fun onError(error: RequestError<*>) {
	}


	fun logWebResponseError(error: String?, request: WebRequestInterface?, responseCode: Int?, responseBody: T?, networkError: Boolean = false) {
		injector.applicationScope.launch(Dispatchers.IO) {
			WebResponseHandlerUtils.logWebResponseError(error, request, responseCode, responseBody, networkError)
		}
	}
}