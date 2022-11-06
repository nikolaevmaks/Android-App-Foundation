package com.application.foundation.features.common.presenter

import android.os.Bundle
import android.util.Pair
import com.application.foundation.features.common.view.BaseFragment
import com.application.foundation.features.common.view.MainActivity
import com.application.foundation.features.profile.presenter.ProfilePresenter
import com.application.foundation.features.profile.view.ProfileFragment
import com.application.foundation.features.shop.presenter.ProductsPresenter
import com.application.foundation.features.shop.view.ProductsFragment
import java.util.*
import java.util.ArrayDeque
import com.application.foundation.features.common.view.MainActivity.Tab

class MainPresenter : BasePresenter(), MainPresenterInterface {

	companion object {
		const val ACTIVITY_TAG = "main"

		const val EXTRA_FRAGMENTS = "fragments"
		const val EXTRA_FRAGMENT_TAG = "fragmentTag"

		private const val PRODUCTS_FRAGMENT_STACK = "products"
		private const val PROFILE_FRAGMENT_STACK = "profile"
	}

	private lateinit var activity: MainActivity


	override fun onViewCreationStarted(savedInstanceState: Bundle?) {
		super.onViewCreationStarted(savedInstanceState)
		activity = getActivity() as MainActivity


		// you can registerRequestListener there for all models like in ProductsPresenter
		// and call request some data for registered models
	}

	override fun onViewCreationFinished(savedInstanceState: Bundle?) {
		super.onViewCreationFinished(savedInstanceState)

		if (savedInstanceState == null) {

			@Suppress("DEPRECATION")
			val fragments = intent.getParcelableArrayListExtra<Bundle>(EXTRA_FRAGMENTS)

			if (fragments == null) {
				activity.setFragmentStack(PRODUCTS_FRAGMENT_STACK)

				val productsFragment = ProductsFragment().apply {
					arguments = Bundle().apply {
						putLong(ProductsPresenter.EXTRA_CATEGORY_ID, 1000)
					}
				}
				activity.replaceFragment(productsFragment, ProductsPresenter.FRAGMENT_TAG)

				activity.selectTab(Tab.Products)
			} else {
				val fragmentStack: Deque<Pair<BaseFragment, String>> = ArrayDeque()

				for (i in fragments.indices) {
					val bundle = fragments[i]

					val fragmentTag = bundle.getString(EXTRA_FRAGMENT_TAG)

					var fragment: BaseFragment? = null

					when (fragmentTag) {
						ProductsPresenter.FRAGMENT_TAG -> fragment = ProductsFragment()
						ProfilePresenter.FRAGMENT_TAG -> fragment = ProfileFragment()
					}

					if (i == 0) {
						when (fragmentTag) {
							ProductsPresenter.FRAGMENT_TAG -> {
								activity.setFragmentStack(PRODUCTS_FRAGMENT_STACK)
								activity.selectTab(Tab.Products)
							}
							else -> {
								activity.setFragmentStack(PROFILE_FRAGMENT_STACK)
								activity.selectTab(Tab.Profile)
							}
						}
					}
					fragment!!.arguments = bundle
					fragmentStack.add(Pair(fragment, fragmentTag))
				}
				activity.replaceFragments(fragmentStack)
			}

		} else {
			when (activity.currentFragmentStackTag) {
				PRODUCTS_FRAGMENT_STACK -> activity.selectTab(Tab.Products)
				PROFILE_FRAGMENT_STACK -> activity.selectTab(Tab.Profile)
			}
		}
	}

	override val isContentViewHiddenInitial: Boolean
		get() = false



	override fun onProductsClicked() {

		activity.setFragmentStack(PRODUCTS_FRAGMENT_STACK)

		if (activity.isCurrentFragmentStackEmpty) {
			val productsFragment = ProductsFragment().apply {
				arguments = Bundle().apply {
					putLong(ProductsPresenter.EXTRA_CATEGORY_ID, 1000)
				}
			}
			activity.replaceFragment(productsFragment, ProductsPresenter.FRAGMENT_TAG)

		}

		activity.selectTab(Tab.Products)
	}

	override fun onProfileClicked() {

		activity.setFragmentStack(PROFILE_FRAGMENT_STACK)
		if (activity.isCurrentFragmentStackEmpty) {
			activity.replaceFragment(ProfileFragment(), ProfilePresenter.FRAGMENT_TAG)
		}

		activity.selectTab(Tab.Profile)
	}
}