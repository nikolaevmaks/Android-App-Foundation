package com.application.foundation.features.common.model.dto

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
class AcquisitionRequest (

	@Json(name = "acquisition_source")
	val acquisitionSource: String
)