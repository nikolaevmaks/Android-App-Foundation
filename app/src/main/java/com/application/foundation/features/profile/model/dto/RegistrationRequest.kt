package com.application.foundation.features.profile.model.dto

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
class RegistrationRequest (

	@Json(name = "first_name")
	val firstName: String,

	@Json(name = "last_name")
	val lastName: String,

	val email: String,
	val password: String
)