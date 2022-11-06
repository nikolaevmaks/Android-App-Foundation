package com.application.foundation.features.profile.model.dto

import com.application.foundation.features.common.model.dto.BaseResponse
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
class TokenType (

	@Json(name = "access_token")
	val accessToken: String,

	@Json(name = "expired")
	val expires: String,

	@Json(name = "refresh_token")
	val refreshToken: String
): BaseResponse()