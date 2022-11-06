package com.application.foundation.features.common.presenter

import com.application.foundation.App.Companion.injector
import android.os.Bundle
import com.application.foundation.R
import android.content.Intent
import com.application.foundation.features.common.view.SplashActivity
import com.application.foundation.utils.CommonUtils

// you can request config on splash screen
class SplashPresenter : BasePresenter(), SplashPresenterInterface {

	lateinit var activity: SplashActivity

	override fun onViewCreationStarted(savedInstanceState: Bundle?) {
		super.onViewCreationStarted(savedInstanceState)
		activity = getActivity() as SplashActivity

		injector.models.onSplashStarted()


		// load config
		CommonUtils.launchDelayed(2000) {
			startActivity()
		}
	}

	private fun startActivity() {

		startActivity(Intent(context,  Router.mainActivityClass)
			.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK))

		activity.finish()
		activity.overridePendingTransition(R.anim.activity_fade_in, R.anim.activity_fade_out)
	}
}