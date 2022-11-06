package com.application.foundation.features.common.model

import com.application.foundation.features.common.model.utils.Abortable
import com.application.foundation.features.common.model.utils.AbortableResult
import com.application.foundation.utils.LogUtils

class RequestsObserver {

	private val requests: MutableSet<RequestBase<*, *>> = linkedSetOf()
	private val requestsEmptyListeners: MutableList<RequestsEmptyListener> = mutableListOf()

	private var isNeedHandleOnAbort = true

	fun abort() {
		isNeedHandleOnAbort = false
		for (request in requests) {
			request.abort()
		}
		isNeedHandleOnAbort = true
	}

	fun abortAllowWaitRequests() {
		for (request in requests) {
			request.abort()
		}
	}

	fun clear() {
		requests.clear()
		requestsEmptyListeners.clear()
	}

	fun perform(request: RequestBase<*, *>, onCompleted: AbortableResult) {

		if (requests.isEmpty()) {
			request.continueRequest(onCompleted.run())
			LogUtils.logD(TAG, "requests.isEmpty(). requestsEmptyListeners: " + requestsEmptyListeners.size)

		} else {
			val listener: RequestsEmptyListener = object : RequestsEmptyListener {
				override fun onRequestsEmpty() {
					request.continueRequest(onCompleted.run())
					LogUtils.logD(TAG, "onRequestsEmpty(). requestsEmptyListeners: " + requestsEmptyListeners.size)
				}

				override fun abort() {
					requestsEmptyListeners.remove(this)
				}
			}
			requestsEmptyListeners.add(listener)
			request.continueRequest(listener)
			LogUtils.logD(TAG, "requests: " + requests.size + ". requestsEmptyListeners: " + requestsEmptyListeners.size)
		}
	}

	fun createObserver(request: RequestBase<*, *>): RequestBase.Observer {

		return object : RequestBase.Observer() {
			override fun onStart() {
				if (request.request !is RequestsEmptyListener) {
					requests.add(request)
				}
			}

			override fun onContinue() {
				if (request.request !is RequestsEmptyListener) {
					requests.add(request)
				}
			}

			override fun onStop() {
				requests.remove(request)
				checkRequestsEmpty()
			}

			override fun onAbort() {
				if (isNeedHandleOnAbort) {
					requests.remove(request)
					checkRequestsEmpty()
				}
			}
		}
	}

	private fun checkRequestsEmpty() {
		if (requests.isEmpty()) {
			val iterator = requestsEmptyListeners.iterator()
			while (iterator.hasNext()) {
				val listener = iterator.next()
				if (requests.isEmpty()) {
					iterator.remove()
					listener.onRequestsEmpty()
				}
			}
		}
	}

	private interface RequestsEmptyListener : Abortable {
		fun onRequestsEmpty()
	}

	companion object {
		private val TAG = RequestsObserver::class.java.simpleName
	}
}