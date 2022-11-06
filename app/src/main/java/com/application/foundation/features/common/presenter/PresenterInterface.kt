package com.application.foundation.features.common.presenter

import android.os.Bundle
import android.view.KeyEvent
import com.application.foundation.features.common.view.ActivityInterface

interface PresenterInterface : BasePresenterInterface {

	fun setActivity(activity: ActivityInterface)

	fun onViewCreationStarted(savedInstanceState: Bundle?)

	fun onViewCreationFinished(savedInstanceState: Bundle?)
	fun onPostCreate(savedInstanceState: Bundle?)

	fun onWindowFocusChanged(hasFocus: Boolean)
	fun onKeyDown(keyCode: Int, event: KeyEvent): Boolean

	val isAlive: Boolean

	fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray)

	val isLastPresenterOrEmpty: Boolean

	val isTopPresenter: Boolean

	fun checkClickStartActivity(): Boolean

	fun setWaitForActivityResult(flag: Boolean)

	var isAnimatingScreenMove: Boolean

	fun onApplyWindowInsetsFinished()
}