package com.application.foundation.network

import com.application.foundation.features.common.model.RequestBase
import com.application.foundation.features.common.model.dto.BaseResponse
import com.application.foundation.network.RequestHandlerBaseResponse.Companion.RESPONSE_BODY_IS_REQURED_ERROR
import java.lang.reflect.Type

abstract class RequestHandler<T> constructor(type: Type,
											 request: RequestBase<*, *>) : ObjectHandler<T>(type, request) {

	// parameter error is always null
	override fun onReceiveResult(body: T?, error: BaseResponse.Error?, responseCode: Int, request: WebRequest) {
		if (isSuccessfulResponse(responseCode)) {
			if (body == null) {
				onUnknownError()
				logWebResponseError(RESPONSE_BODY_IS_REQURED_ERROR, request, responseCode, body)
			} else {
				onReceiveValidResult(body, responseCode)
			}
		} else {
			onUnknownError()
			logWebResponseError(null, request, responseCode, body)
		}
	}



	protected open fun onReceiveValidResult(body: T, responseCode: Int) {
		onReceiveValidResult(body)
	}

	protected open fun onReceiveValidResult(body: T) {
		onReceiveValidResult()
	}
}