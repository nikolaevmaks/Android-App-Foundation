package com.application.foundation.features.profile.model.dto

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
class RefreshTokenRequest (

	@Json(name = "refresh_token")
	val refreshToken: String
)