package com.application.foundation.features.common.model.dto

import android.os.Parcelable
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import kotlinx.parcelize.Parcelize

@Parcelize
@JsonClass(generateAdapter = true)
class Currency (

	val code: String,
	val name: String,

	val symbol: String?,
	val exp: Int = 2,

	@Json(name = "suffix")
	val isSuffix: Boolean = false

) : Parcelable {

	companion object {
		val USD = Currency("USD", "United States dollar", "$")
	}
}