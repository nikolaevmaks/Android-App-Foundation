package com.application.foundation.features.common.presenter

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import com.application.foundation.App
import com.application.foundation.features.common.model.Models
import com.application.foundation.features.common.view.ActivityInterface
import com.application.foundation.utils.Analytics

interface BasePresenterInterface : RegisterRequestListenerInterface, StartActivityInterface, ViewModelInterface {

	fun getActivity(): ActivityInterface

	override val context: Context

	val isContentViewHiddenInitial: Boolean
	val isContentVisible: Boolean
	fun updateContentAndProgressVisibility()
	fun updateContentVisibility()
	fun updateProgressVisibility()

	val isIndefiniteSnackbar: Boolean
	fun needDismissSnackbarOnTouchOutside(): Boolean

	fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?)
	fun onNewIntent(intent: Intent)

	val intent: Intent

	fun onCreateOptionsMenu(menu: Menu)
	fun onOptionsItemSelected(item: MenuItem): Boolean
	fun invalidateOptionsMenu()
	fun onBackPressed(): Boolean

	val isToolbarAsActionBarEnabled: Boolean
	val isToolbarHomeAsUpEnabled: Boolean


	fun onStart()
	fun onResume()
	fun onPause()
	fun onSaveInstanceState(outState: Bundle)
	fun onStop()
	fun onDestroy()

	fun onRequestPermissionsResultSafe(requestCode: Int, permissions: Array<String>, grantResults: IntArray)

	fun onFinishingAfterBackPress(): Boolean
	fun onFinishing()

	fun onStartActivity()
	fun onDialogShown()

	var isActivityStartedForResult: Boolean

	fun checkClick(): Boolean
	fun checkClickForDialog(): Boolean

	val application: App
	val analytics: Analytics
	val models: Models
}