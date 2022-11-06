package com.application.foundation.network

import com.application.foundation.features.common.model.RequestBase
import com.application.foundation.features.common.model.dto.BaseResponse

abstract class RequestHandlerBaseResponseNullBody @JvmOverloads constructor(request: RequestBase<*, *>,
												  responseCodeHandler: ResponseCodeHandler? = null) :

		RequestHandlerBaseResponseNullBodyAllowed<BaseResponse>(BaseResponse::class.java, request, responseCodeHandler) {
}