package com.application.foundation.features.common.view

import android.os.Bundle
import com.application.foundation.R
import com.application.foundation.features.common.presenter.SplashPresenter
import com.application.foundation.features.common.presenter.SplashPresenterInterface

class SplashActivity : BaseActivity() {

	private lateinit var presenter: SplashPresenterInterface

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)

		setContentView(R.layout.splash)
		onViewCreationFinished()
	}

	override fun createPresenter(): SplashPresenter {
		return SplashPresenter().also { presenter = it }
	}

	override val isSetLightStatusBarBackground: Boolean
		get() = false
}