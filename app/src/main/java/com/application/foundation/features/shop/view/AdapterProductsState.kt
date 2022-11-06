package com.application.foundation.features.shop.view

import androidx.recyclerview.widget.RecyclerView
import com.application.foundation.features.common.view.utils.RecyclerViewUtils.ScrollPosition
import com.application.foundation.features.shop.model.Products
import com.application.foundation.features.shop.model.dto.Product

class AdapterProductsState {

	var firstVisibleItemPosition = 0
		private set

	var scrollOffset = 0
		private set

	fun updateScroll(scrollPosition: ScrollPosition) {
		firstVisibleItemPosition = scrollPosition.firstVisibleItemPosition
		scrollOffset = scrollPosition.scrollOffset
	}

	private var compoundCategoryId: String? = null
	private var sync = 0
	private var currentPage: Int? = null

	private var products: MutableList<Product>?

	private val productsRequired: MutableList<Product>
		get() = products ?: mutableListOf<Product>().also { products = it }


	constructor() {
		products = null
	}

	constructor(products: Products) : this(products.products, products.getCurrentPage(), products.sync)

	constructor(products: List<Product>?, currentPage: Int?, sync: Int) {
		this.products = products?.toMutableList()
		this.currentPage = currentPage
		this.sync = sync
	}

	fun clear() {
		firstVisibleItemPosition = 0
		scrollOffset = 0

		compoundCategoryId = null
		sync = 0
		currentPage = null

		products = null
	}

	fun isSyncEquals(sync: Int): Boolean {
		return this.sync == sync
	}

	fun isSyncAndCurrentPageEquals(sync: Int, currentPage: Int?): Boolean {
		return this.currentPage == currentPage &&
				this.sync == sync
	}

	fun isPossibleToAdd(sync: Int, currentPage: Int?): Boolean {
		return this.currentPage != null && currentPage != null && this.currentPage!! < currentPage &&
				this.sync == sync
	}

	@JvmOverloads
	fun update(compoundCategoryId: String?,
			   sync: Int, currentPage: Int?, productCountPerPage: Int = Products.PRODUCT_COUNT_PER_PAGE,
			   productsFromModel: List<Product>?,
			   adapter: RecyclerView.Adapter<*>?) {

		if (this.compoundCategoryId == compoundCategoryId &&
			isSyncAndCurrentPageEquals(sync, currentPage)) {

			return

		} else if (this.compoundCategoryId == compoundCategoryId &&
				   isPossibleToAdd(sync, currentPage)) {

			val oldCount = adapter?.itemCount ?: productsRequired.size

			val toAdd = productsFromModel!!.subList((this.currentPage!! + 1) * productCountPerPage, productsFromModel.size)
			productsRequired.addAll(toAdd)

			adapter?.notifyItemRangeInserted(oldCount, toAdd.size)

		} else {
			if (productsFromModel == null) {
				products = null
			} else {
				products?.clear()
				productsRequired.addAll(productsFromModel)
			}

			adapter?.notifyDataSetChanged()
		}

		this.compoundCategoryId = compoundCategoryId
		this.sync = sync
		this.currentPage = currentPage
	}

	@JvmOverloads
	fun update(sync: Int, currentPage: Int?, productCountPerPage: Int = Products.PRODUCT_COUNT_PER_PAGE,
			   productsFromModel: List<Product>?,
			   adapter: RecyclerView.Adapter<*>?) {
		update(null, sync, currentPage, productCountPerPage, productsFromModel, adapter)
	}

	fun update(productsInStore: Products, adapter: RecyclerView.Adapter<*>?) {
		update(null, productsInStore.sync, productsInStore.getCurrentPage(), Products.PRODUCT_COUNT_PER_PAGE, productsInStore.products, adapter)
	}


	val productsCount
		get() = products?.size ?: 0

	val isProductsEmptyOrNull
		get() = products?.isEmpty() ?: true

	val isProductsNull
		get() = products == null


	fun getProduct(pos: Int): Product {
		return products!![pos]
	}
}