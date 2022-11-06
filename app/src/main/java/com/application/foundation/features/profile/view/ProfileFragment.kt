package com.application.foundation.features.profile.view

import android.view.LayoutInflater
import android.view.ViewGroup
import com.application.foundation.R
import com.application.foundation.features.common.view.BaseFragment
import com.application.foundation.features.profile.presenter.ProfilePresenter
import com.application.foundation.features.profile.presenter.ProfilePresenterInterface

class ProfileFragment : BaseFragment() {

	private lateinit var presenter: ProfilePresenterInterface


	override fun createPresenter(): ProfilePresenterInterface {
		return ProfilePresenter().also { presenter = it }
	}

	override fun onCreateView(inflater: LayoutInflater, container: ViewGroup): ViewGroup {
		return inflater.inflate(R.layout.profile, container, false) as ViewGroup
	}

	override fun getViewContentId(): Int {
		return 0
	}


	override fun updateWindowInsets() {

		// it is possible to use OnGlobalLayoutListener for viewBottomNavigationBar.height like in ProductsFragment, but it is simpler
		val bottomNavigationBarHeight = activity.windowInsetBottom!! + resources.getDimensionPixelSize(R.dimen.bottom_navigation_bar_height)

		val lp = view.layoutParams as ViewGroup.MarginLayoutParams
		if (lp.topMargin != activity.windowInsetTop || lp.bottomMargin != bottomNavigationBarHeight) {
			lp.topMargin = activity.windowInsetTop!!
			lp.bottomMargin = bottomNavigationBarHeight
			view.requestLayout()
		}
	}
}