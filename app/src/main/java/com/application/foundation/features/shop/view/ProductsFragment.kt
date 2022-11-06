package com.application.foundation.features.shop.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewGroup.MarginLayoutParams
import android.view.ViewTreeObserver
import androidx.recyclerview.widget.RecyclerView
import butterknife.BindView
import com.application.foundation.R
import com.application.foundation.features.common.view.BaseFragment
import com.application.foundation.features.common.view.MainActivity
import com.application.foundation.features.shop.model.dto.Product
import com.application.foundation.features.shop.presenter.ProductsPresenter
import com.application.foundation.features.shop.presenter.ProductsPresenterInterface

class ProductsFragment : BaseFragment(), ProductsAdapter.Listener {

	private lateinit var presenter: ProductsPresenterInterface


	@BindView(R.id.list) lateinit var list: RecyclerView
	private lateinit var viewBottomNavigationBar: View

	private lateinit var adapter: ProductsAdapter


	override fun createPresenter(): ProductsPresenterInterface {
		return ProductsPresenter().also { presenter = it }
	}

	override fun onCreateView(inflater: LayoutInflater, container: ViewGroup): ViewGroup {
		return inflater.inflate(R.layout.products, container, false) as ViewGroup
	}

	override fun getViewContentId(): Int {
		return R.id.list
	}

	override fun onViewBound(savedInstanceState: Bundle?) {
		viewBottomNavigationBar = (activity as MainActivity).viewBottomNavigationBar

		adapter = ProductsAdapter(list, this)
		list.adapter = adapter
	}


	override fun updateWindowInsets() {

		val lp = viewContent.layoutParams as MarginLayoutParams
		if (lp.topMargin != activity.windowInsetTop) {
			lp.topMargin = activity.windowInsetTop!!
			viewContent.requestLayout()
		}
	}


	private val onLayoutChangeListener = ViewTreeObserver.OnGlobalLayoutListener { updateViewMargins() }

	private fun updateViewMargins() {
		val lp = view.layoutParams as MarginLayoutParams
		if (lp.bottomMargin != viewBottomNavigationBar.height) {
			lp.bottomMargin = viewBottomNavigationBar.height
			view.requestLayout()
		}
	}

	override fun onViewAttached() {
		super.onViewAttached()
		view.viewTreeObserver.addOnGlobalLayoutListener(onLayoutChangeListener)
	}

	override fun onViewBeforeDetached() {
		super.onViewBeforeDetached()
		view.viewTreeObserver.removeOnGlobalLayoutListener(onLayoutChangeListener)
	}


	fun updateProducts(sync: Int, currentPage: Int?, productCountPerPage: Int, productsFromModel: List<Product>?) {
		adapter.update(sync, currentPage, productCountPerPage, productsFromModel)
	}


	override fun onProductClicked(product: Product) {
		if (checkClick()) {
			return
		}
		presenter.onProductClicked(product)
	}

	override fun onScrolledToBottom() {
		presenter.onScrolledToBottom()
	}


	override val enableAppBarLayoutTopMargin: Boolean
		get() = true
}