package com.application.foundation.features.profile.model.dto

import com.application.foundation.features.common.model.dto.BaseResponse
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
class ProfileWithTokenType (

	val token: TokenType,

	id: Long,

	email: String?,

	@Json(name = "first_name")
	firstName: String?,

	@Json(name = "last_name")
	lastName: String?,

	@Json(name = "facebook_id")
	facebookId: String?

) : ProfileType(
	id = id,
	email = email,
	firstName = firstName,
	lastName = lastName,
	facebookId = facebookId
)