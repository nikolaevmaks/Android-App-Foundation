package com.application.foundation.features.common.presenter

import com.application.foundation.features.common.model.RequestBase
import com.application.foundation.features.common.model.RequestBase.OnAbortListener
import java.util.*

class RequestsListenersFeature(private val listener: Listener) : RegisterRequestListenerInterface {

	private val requestListeners: MutableMap<RequestBase<*, *>, MutableList<ListenersPair>> = LinkedHashMap()

	private var isAfterRegistrationListenersWillBeAdded = true

	private class ListenersPair(val listener: RequestBase.Listener, val abortListener: OnAbortListener?)


	override fun isListenerRegistered(request: RequestBase<*, *>, listenerClass: Class<*>): Boolean {

		removeDiedRequests()

		val listeners = requestListeners[request]
		if (listeners != null) {
			for (pair in listeners) {
				if (pair.listener.javaClass == listenerClass) {
					return true
				}
			}
		}
		return false
	}

	override fun isListenerRegistered(request: RequestBase<*, *>, listener: RequestBase.Listener): Boolean {

		removeDiedRequests()

		val listeners = requestListeners[request]
		if (listeners != null) {
			for (pair in listeners) {
				if (RequestBase.getListenerClassName(pair.listener) == RequestBase.getListenerClassName(listener)) {
					return true
				}
			}
		}
		return false
	}

	override fun registerRequestListener(request: RequestBase<*, *>, listener: RequestBase.Listener) {
		registerRequestListener(request, listener, false)
	}

	override fun registerRequestListener(request: RequestBase<*, *>, listener: RequestBase.Listener, isManyEqualListenerClassesAllowed: Boolean) {
		registerRequestListener(request, listener, null, isManyEqualListenerClassesAllowed)
	}

	override fun registerRequestListener(request: RequestBase<*, *>,
										 listener: RequestBase.Listener,
										 abortListener: OnAbortListener?,
										 isManyEqualListenerClassesAllowed: Boolean) {
		removeDiedRequests()

		if (isManyEqualListenerClassesAllowed || !isListenerRegistered(request, listener)) {

			var listeners = requestListeners[request]
			if (listeners == null) {
				listeners = ArrayList(1)
				requestListeners[request] = listeners
			}

			var contains = false
			for (pair in listeners) {
				if (pair.listener === listener) {
					contains = true
					break
				}
			}

			if (!contains) {
				listeners.add(ListenersPair(listener, abortListener))
			}

			if (isAfterRegistrationListenersWillBeAdded) {
				request.addListenerToCacheIfAbsent(listener, abortListener)
			} else {
				request.addListener(listener, abortListener)
			}
		}
	}

	override fun unregisterRequestListener(request: RequestBase<*, *>, listener: RequestBase.Listener) {
		unregisterRequestListener(request, listener, null)
	}

	override fun unregisterRequestListener(request: RequestBase<*, *>,
										   listener: RequestBase.Listener,
										   abortListener: OnAbortListener?) {
		removeDiedRequests()

		val listeners = requestListeners[request]
		if (listeners != null) {
			for (i in listeners.indices) {
				if (listeners[i].listener === listener) {
					listeners.removeAt(i)
					break
				}
			}

			request.removeListener(listener, abortListener, false)

			if (listeners.isEmpty()) {
				requestListeners.remove(request)
			}
		}
	}

	fun onStop() {
		isAfterRegistrationListenersWillBeAdded = true
	}

	fun addRequestsListeners() {

		removeDiedRequests()

		isAfterRegistrationListenersWillBeAdded = false

		for ((request, value) in LinkedHashMap(requestListeners)) {
			for (pair in ArrayList(value)) {
				request.addListener(pair.listener, pair.abortListener)
			}
		}
	}

	override fun removeRequestsListeners(temporarily: Boolean) {

		removeDiedRequests()

		for ((request, value) in requestListeners) {
			for (pair in value) {
				request.removeListener(pair.listener, pair.abortListener, temporarily)
			}
		}
	}

	override fun removeRequestsListeners(temporarily: Boolean, vararg requests: RequestBase<*, *>) {

		removeDiedRequests()

		for (request in requests) {
			val listeners = requestListeners[request]
			if (listeners != null) {
				for (pair in listeners) {
					request.removeListener(pair.listener, pair.abortListener, temporarily)
				}
			}
		}
	}

	private fun removeDiedRequests() {

		val iterator = requestListeners.keys.iterator()
		while (iterator.hasNext()) {
			val request = iterator.next()

			if (listener.isRequestDied(request)) {
				iterator.remove()
			}
		}
	}

	interface Listener {
		fun isRequestDied(request: RequestBase<*, *>): Boolean
	}
}