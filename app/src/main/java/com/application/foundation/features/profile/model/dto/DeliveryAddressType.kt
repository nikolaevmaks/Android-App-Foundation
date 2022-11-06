package com.application.foundation.features.profile.model.dto

import com.application.foundation.features.common.model.dto.BaseResponse
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
class DeliveryAddressType (

	@Json(name = "first_name")
	var firstName: String?,

	@Json(name = "last_name")
	var lastName: String?,

	@Json(name = "country")
	val countryCode: String?,

	val city: String?,

	@Json(name = "street_address1")
	val streetAddress1: String?,

	@Json(name = "street_address2")
	val streetAddress2: String?,

	@Json(name = "postcode")
	val postcode: String?,

	val state: String?

) : BaseResponse()