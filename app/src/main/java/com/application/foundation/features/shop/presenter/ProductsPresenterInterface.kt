package com.application.foundation.features.shop.presenter

import com.application.foundation.features.common.presenter.FragmentPresenterInterface
import com.application.foundation.features.shop.model.dto.Product

interface ProductsPresenterInterface : FragmentPresenterInterface {

	fun onProductClicked(product: Product)
	fun onScrolledToBottom()
}