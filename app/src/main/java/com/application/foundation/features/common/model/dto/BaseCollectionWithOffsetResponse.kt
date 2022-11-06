package com.application.foundation.features.common.model.dto

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
open class BaseCollectionWithOffsetResponse (

	val offset: Int,

	val limit: Int,

	val next: Boolean

) : BaseResponse() {

	val isLastPage: Boolean
		get() = !next
}