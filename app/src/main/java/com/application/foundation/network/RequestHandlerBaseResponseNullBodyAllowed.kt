package com.application.foundation.network

import com.application.foundation.features.common.model.RequestBase
import com.application.foundation.features.common.model.dto.BaseResponse
import com.application.foundation.features.common.model.utils.RequestError
import com.application.foundation.network.RequestHandlerBaseResponse.Companion.RESPONSE_ERROR_IS_REQURED_ERROR
import java.lang.reflect.Type

abstract class RequestHandlerBaseResponseNullBodyAllowed<T : BaseResponse>(type: Type,
																		   request: RequestBase<*, *>,
																		   private val responseCodeHandler: ResponseCodeHandler? = null) : ObjectHandler<T>(type, request) {


	override fun onReceiveResult(body: T?, error: BaseResponse.Error?, responseCode: Int, request: WebRequest) {

		if (responseCodeHandler != null && responseCodeHandler.handleResponseCode(responseCode)) {

		} else if (isSuccessfulResponse(responseCode)) {
			onReceiveValidResult(body, responseCode)
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


	protected open fun onReceiveValidResult(body: T?, responseCode: Int) {
		onReceiveValidResult(body)
	}

	protected open fun onReceiveValidResult(body: T?) {
		onReceiveValidResult()
	}
}