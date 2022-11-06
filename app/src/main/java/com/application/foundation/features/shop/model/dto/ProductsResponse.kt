package com.application.foundation.features.shop.model.dto

import com.application.foundation.features.common.model.utils.CollectionWithoutNulls
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import com.application.foundation.features.common.model.dto.BaseCollectionWithPageResponse

@JsonClass(generateAdapter = true)
class ProductsResponse (

	@CollectionWithoutNulls
	@Json(name = "collection")
	val products: List<Product>,


	page: Int,

	@Json(name = "per_page")
	perPage: Int,

	@Json(name = "is_last_page")
	isLastPage: Boolean

) : BaseCollectionWithPageResponse(page, perPage, isLastPage)