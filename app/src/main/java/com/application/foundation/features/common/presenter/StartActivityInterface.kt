package com.application.foundation.features.common.presenter

import android.content.Context
import android.content.Intent

interface StartActivityInterface {

	fun startActivity(intent: Intent)
	fun startActivityForResult(intent: Intent, requestCode: Int)
	val context: Context
}