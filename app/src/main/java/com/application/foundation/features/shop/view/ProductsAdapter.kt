package com.application.foundation.features.shop.view

import android.view.ViewGroup
import android.view.View
import butterknife.BindView
import android.widget.TextView
import butterknife.OnClick
import butterknife.ButterKnife
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.application.foundation.features.common.view.utils.inflate
import com.application.foundation.features.shop.model.dto.Product

class ProductsAdapter(
	private val list: RecyclerView,
	private val listener: Listener

) : RecyclerView.Adapter<ProductsAdapter.ProductViewHolder>() {

	private val state = AdapterProductsState()

	init {
		list.setHasFixedSize(true)

		val linearLayoutManager = LinearLayoutManager(list.context)
		list.layoutManager = linearLayoutManager


		list.addOnScrollListener(object : RecyclerView.OnScrollListener() {

			override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
				super.onScrolled(recyclerView, dx, dy)

				val firstVisibleItemPosition = linearLayoutManager.findFirstVisibleItemPosition()
				val lastVisibleItemPosition = linearLayoutManager.findLastVisibleItemPosition()

				if (dy >= 0 && firstVisibleItemPosition != RecyclerView.NO_POSITION && lastVisibleItemPosition != RecyclerView.NO_POSITION) {

					val totalItemCount = linearLayoutManager.itemCount
					if (lastVisibleItemPosition >= totalItemCount - (lastVisibleItemPosition - firstVisibleItemPosition)) {
						listener.onScrolledToBottom()
					}
				}
			}
		})
	}


	fun update(sync: Int, currentPage: Int?, productCountPerPage: Int, productsFromModel: List<Product>?) {
		state.update(sync, currentPage, productCountPerPage, productsFromModel, this)
	}

	override fun getItemCount(): Int {
		return state.productsCount
	}

	override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProductViewHolder {
		return ProductViewHolder(parent.inflate(android.R.layout.simple_list_item_1))
	}

	override fun onBindViewHolder(holder: ProductViewHolder, position: Int) {
		holder.populate(position)
	}

	inner class ProductViewHolder(view: View) : RecyclerView.ViewHolder(view) {

		@BindView(android.R.id.text1) lateinit var textName: TextView

		private lateinit var product: Product

		init {
			ButterKnife.bind(this, view)
		}

		fun populate(position: Int) {

			product = state.getProduct(position)

			textName.text = product.name
		}

		@OnClick(android.R.id.text1)
		fun onProductClicked() {
			listener.onProductClicked(product)
		}
	}

	interface Listener {
		fun onProductClicked(product: Product)
		fun onScrolledToBottom()
	}
}