package com.application.foundation.features.shop.presenter

import android.os.Bundle
import com.application.foundation.features.common.model.RequestBase
import com.application.foundation.features.common.presenter.BaseFragmentPresenter
import com.application.foundation.features.shop.model.Products
import com.application.foundation.features.shop.model.dto.Product
import com.application.foundation.features.shop.view.ProductsFragment
import kotlin.properties.Delegates

class ProductsPresenter : BaseFragmentPresenter(), ProductsPresenterInterface {

	companion object {
		const val FRAGMENT_TAG = "products"

		const val EXTRA_CATEGORY_ID = "categoryId"
	}

	private lateinit var fragment: ProductsFragment

	private var categoryId: Long by Delegates.notNull()
	private lateinit var products: Products

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		fragment = getFragment() as ProductsFragment

		categoryId = arguments.getLong(EXTRA_CATEGORY_ID)

		// you can get model from injector or create it via restoreOrCreateViewModel
		// for example:
		// deliveryAddress = injector.deliveryAddress.deliveryAddress
		// deliveryAddressSaveRequest = restoreOrCreateViewModel { injector.deliveryAddress.createDeliveryAddressSaveRequest() }

		products = restoreOrCreateViewModel { Products() }
		registerRequestListener(products, productsListener)
	}

	override fun onViewAttached() {
		super.onViewAttached()

		products.startIfRequired(categoryId)
		updateView()
	}

	override val isContentVisible: Boolean
		get() = products.products != null


	override fun updateProgressVisibility() {
		setInitialProgressVisibility(products.products == null && products.isUpdating)

		// also you can call activity.setProgressDialogFullVisibility / activity.setProgressDialogSmallVisibility there
	}


	private fun updateView() {
		fragment.updateProducts(products.sync,
				products.getCurrentPage(),
				Products.PRODUCT_COUNT_PER_PAGE,
				products.products)
	}


	private val productsListener: RequestBase.Listener = RequestBase.Listener {

		if (!products.isUpdating) {
			updateView()

			if (products.error != null && !isContentVisible) {
				fragment.showErrorSnackBarWithRetryOnNetworkError(products.error!!) { products.requestNextPageIfPossible() }
			}
		}
		updateContentAndProgressVisibility()
	}


	override fun onScrolledToBottom() {
		products.requestNextPageIfPossible()
	}

	override fun onProductClicked(product: Product) {
	}
}