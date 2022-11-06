package com.application.foundation.features.checkout.model.dto

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import com.application.foundation.features.common.model.dto.BaseResponse

@JsonClass(generateAdapter = true)
class StripeKeyResponse (

	@Json(name = "publishable_key")
	val publishableKey: String

) : BaseResponse()