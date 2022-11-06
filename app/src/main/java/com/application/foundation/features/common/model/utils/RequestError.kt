package com.application.foundation.features.common.model.utils

import com.application.foundation.App.Companion.injector
import com.application.foundation.R
import com.application.foundation.features.common.model.dto.BaseResponse

open class RequestError<ErrorType> {

	private var isNetworkError_: Boolean? = null

	var errorType: ErrorType? = null
		private set

	var errorMessage: String? = null
		private set

	var baseResponseError: BaseResponse.Error? = null
		private set


	private constructor(isNetworkError: Boolean) {
		this.isNetworkError_ = isNetworkError
	}

	constructor(error: RequestError<ErrorType>) {
		isNetworkError_ = error.isNetworkError_
		errorType = error.errorType
		errorMessage = error.errorMessage
		baseResponseError = error.baseResponseError
	}


	constructor(error: ErrorType, message: String? = null) {
		errorType = error
		errorMessage = message
	}

	constructor(message: String) {
		errorMessage = message
	}

	constructor(error: BaseResponse.Error) {
		baseResponseError = error
	}


	val isNetworkError: Boolean
		get() = isNetworkError_ ?: false

	val isUnknownError: Boolean
		get() = !(isNetworkError_ ?: true)


	fun toPrintableString(): String {
		if (isNetworkError) {
			return injector.applicationContext.getString(R.string.common_error_internet)

		} else if (isUnknownError) {
			return injector.applicationContext.getString(R.string.common_error_unknown)

		} else if (errorMessage != null) {
			return errorMessage!!
		}

		return baseResponseError?.printableMessage ?: injector.applicationContext.getString(R.string.common_error_unknown)
	}

	companion object {
		fun <ErrorType> networkError(): RequestError<ErrorType> {
			return RequestError(true)
		}

		fun <ErrorType> unknownError(): RequestError<ErrorType> {
			return RequestError(false)
		}
	}
}