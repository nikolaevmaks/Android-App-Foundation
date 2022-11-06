package com.application.foundation.network

import com.application.foundation.features.common.model.RequestBase
import com.application.foundation.features.common.model.dto.BaseResponse
import com.application.foundation.features.common.model.utils.RequestError
import java.lang.reflect.Type

abstract class RequestHandlerBaseResponse<T : BaseResponse>(type: Type,
															request: RequestBase<*, *>,
															private val responseCodeHandler: ResponseCodeHandler? = null) : ObjectHandler<T>(type, request) {

	override fun onReceiveResult(body: T?, error: BaseResponse.Error?, responseCode: Int, request: WebRequest) {

		if (responseCodeHandler != null && responseCodeHandler.handleResponseCode(responseCode)) {

		} else if (isSuccessfulResponse(responseCode)) {
			if (body == null) {
				onUnknownError()
				logWebResponseError(RESPONSE_BODY_IS_REQURED_ERROR, request, responseCode, body)
			} else {
				onReceiveValidResult(body, responseCode)
			}
		} else {
			if (error == null) {
				onUnknownError()
				logWebResponseError(RESPONSE_ERROR_IS_REQURED_ERROR, request, responseCode, body)
			} else {
				onError(RequestError<Any>(error))
				logWebResponseError(error.printableMessage, request, responseCode, body)
			}
		}
	}

	protected open fun onReceiveValidResult(body: T, responseCode: Int) {
		onReceiveValidResult(body)
	}

	protected open fun onReceiveValidResult(body: T) {
		onReceiveValidResult()
	}



	companion object {
		const val RESPONSE_BODY_IS_REQURED_ERROR = "Response body required, but is null"
		const val RESPONSE_ERROR_IS_REQURED_ERROR = "Response body \"error\" required, but is null"
	}
}