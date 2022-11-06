package com.application.foundation.features.profile.model.dto

import com.application.foundation.features.common.model.dto.BaseResponse
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
open class ProfileType (

	val id: Long,

	val email: String?,

	@Json(name = "first_name")
	val firstName: String?,

	@Json(name = "last_name")
	val lastName: String?,

	@Json(name = "facebook_id")
	val facebookId: String?

) : BaseResponse() {

	enum class Store {
		UK, US, WW
	}
}