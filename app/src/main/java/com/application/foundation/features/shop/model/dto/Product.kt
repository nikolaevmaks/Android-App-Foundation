package com.application.foundation.features.shop.model.dto

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import java.math.BigDecimal

@JsonClass(generateAdapter = true)
class Product (

	val id: Long,

	val name: String,

	val price: BigDecimal,

	@Json(name = "image_url")
	val imageUrl: String
)