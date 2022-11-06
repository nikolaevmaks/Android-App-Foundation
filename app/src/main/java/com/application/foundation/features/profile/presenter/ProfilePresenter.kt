package com.application.foundation.features.profile.presenter

import android.os.Bundle
import com.application.foundation.features.common.presenter.BaseFragmentPresenter
import com.application.foundation.features.profile.view.ProfileFragment

class ProfilePresenter : BaseFragmentPresenter(), ProfilePresenterInterface {

	companion object {
		const val FRAGMENT_TAG = "profile"
	}

	private lateinit var fragment: ProfileFragment


	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		fragment = getFragment() as ProfileFragment
	}

	override val isContentViewHiddenInitial: Boolean
		get() = false


	override fun onBackPressed(): Boolean {
		return true
	}
}