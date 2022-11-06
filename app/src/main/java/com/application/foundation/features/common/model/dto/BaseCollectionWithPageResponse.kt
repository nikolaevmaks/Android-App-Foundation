package com.application.foundation.features.common.model.dto

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
open class BaseCollectionWithPageResponse (

	val page: Int,

	@Json(name = "per_page")
	val perPage: Int,

	@Json(name = "is_last_page")
	val isLastPage: Boolean

) : BaseResponse()