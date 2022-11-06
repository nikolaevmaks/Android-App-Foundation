package com.application.foundation.network

import com.application.foundation.features.common.model.utils.Abortable
import okhttp3.Request

interface WebRequestInterface : Abortable {
	val request: Request
	val isAborted: Boolean
}