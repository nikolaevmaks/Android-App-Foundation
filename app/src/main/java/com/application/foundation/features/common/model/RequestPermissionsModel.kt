package com.application.foundation.features.common.model

import kotlin.properties.Delegates

class RequestPermissionsModel : RequestBase<Any, Any>() {

	var requestCode: Int by Delegates.notNull()
		private set

	lateinit var permissions: Array<String>
		private set

	lateinit var grantResults: IntArray
		private set


	fun onRequestPermissionsResult(
		requestCode: Int,
		permissions: Array<String>, grantResults: IntArray
	) {
		this.requestCode = requestCode
		this.permissions = permissions
		this.grantResults = grantResults

		notifyListeners()
	}
}