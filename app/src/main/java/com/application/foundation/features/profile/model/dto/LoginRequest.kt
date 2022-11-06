package com.application.foundation.features.profile.model.dto

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
class LoginRequest (

	val login: String,
	val password: String
)