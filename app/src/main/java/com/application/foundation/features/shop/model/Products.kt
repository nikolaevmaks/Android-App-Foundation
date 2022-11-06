package com.application.foundation.features.shop.model

import android.annotation.SuppressLint
import com.application.foundation.App.Companion.injector
import com.application.foundation.features.common.model.RequestBase
import com.application.foundation.features.shop.model.dto.Product
import com.application.foundation.features.shop.model.dto.ProductsResponse
import com.application.foundation.network.RequestHandlerBaseResponse
import com.application.foundation.utils.mapCapacity
import java.util.*

class Products @JvmOverloads constructor(sync: Int = 0) : RequestBase<Any, Any>() {

	companion object {
		const val PRODUCT_COUNT_PER_PAGE = 50
	}

	private var categoryId: Long? = null
	private var productsList: MutableList<Product>? = null
	private var productIds: MutableSet<Long>? = null

	var isLastPage = false
		private set


	private var currentPage = -1

	var sync: Int = sync
		private set


	@SuppressLint("MissingSuperCall")
	override fun clear() {
		categoryId = null
		clearProducts()
	}

	fun clearProducts() {
		super.clear()

		clearFromMemoryOnDuplicates()
		sync++
	}

	private fun clearFromMemoryOnDuplicates() {
		productsList = null
		productIds = null
		currentPage = -1
		isLastPage = false
	}

	fun startIfRequired(categoryId: Long) {
		if (this.categoryId != categoryId ||
			productsList == null && !isUpdating) {

			abortRequest()
			clear()

			this.categoryId = categoryId

			requestNextPage()
		}
	}

	fun requestNextPageIfPossible() {
		if (!isUpdating) {
			if (categoryId != null && (productsList == null || !isLastPage)) {
				requestNextPage()
			}
		}
	}

	private val handler: RequestHandlerBaseResponse<*> = object : RequestHandlerBaseResponse<ProductsResponse>(ProductsResponse::class.java, this@Products) {

		override fun onReceiveValidResult(body: ProductsResponse) {

			if (productsList == null) {
				productsList = ArrayList(body.products.size)
				productIds = HashSet(mapCapacity(body.products.size))
			}

			isLastPage = body.isLastPage

			productsList!!.addAll(body.products)


			var duplicatesExists = false
			for (product in body.products) {
				if (productIds!!.contains(product.id)) {
					duplicatesExists = true
					break
				} else {
					productIds!!.add(product.id)
				}
			}

			if (duplicatesExists) {
				clearFromMemoryOnDuplicates()

				this@Products.sync++

				requestNextPage()
			} else {
				currentPage++
				stopRequest()
			}
		}
	}

	private fun requestNextPage() {
		continueRequest(injector.webClient.requestProducts(categoryId!!, currentPage + 1, PRODUCT_COUNT_PER_PAGE, handler))
	}

	fun getCurrentPage(): Int? {
		return if (currentPage == -1) null else currentPage
	}

	val products: List<Product>?
		get() = productsList
}