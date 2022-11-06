package com.application.foundation.features.common.model

import androidx.annotation.CallSuper
import com.application.foundation.features.common.model.utils.Abortable
import com.application.foundation.features.common.model.utils.RequestError
import com.application.foundation.utils.CommonUtils

abstract class RequestBase<ErrorType, StateType> : Abortable, RequestBaseInterface<ErrorType, StateType> {

	final override var isUpdating = false
		private set

	final override var error: RequestError<ErrorType>? = null
		private set

	final override var state: StateType? = null
		private set

	private val onStateChangedListeners: MutableSet<Listener> = linkedSetOf()
	private val onAbortListeners: MutableSet<OnAbortListener> = linkedSetOf()

	private val onStateChangedListenersCache: MutableMap<String, Boolean> = mutableMapOf()
	private val onAbortListenersCache: MutableMap<String, Boolean> = mutableMapOf()

	final override var request: Abortable? = null
		private set

	private var observer: Observer? = null

	private val listeners: List<Listener>
		get() = onStateChangedListeners.toList()

	private fun getOnAbortListeners(): List<OnAbortListener> {
		return onAbortListeners.toList()
	}

	@CallSuper
	override fun clear() {
		error = null
	}

	final override fun addListenerToCacheIfAbsent(listener: Listener, abortListener: OnAbortListener?) {

		val listenerName = getListenerClassName(listener)
		if (!onStateChangedListenersCache.containsKey(listenerName)) {
			onStateChangedListenersCache[listenerName] = false
		}

		if (abortListener != null) {
			val abortListenerName = CommonUtils.getClassNameFull(abortListener)
			if (!onAbortListenersCache.containsKey(abortListenerName)) {
				onAbortListenersCache[abortListenerName] = false
			}
		}
	}

	final override fun isListenerCached(listener: Listener): Boolean {
		return java.lang.Boolean.TRUE == onStateChangedListenersCache[getListenerClassName(listener)]
	}


	final override fun addListener(listener: Listener, abortListener: OnAbortListener?) {

		onStateChangedListeners.add(listener)

		var stateChanged = onStateChangedListenersCache.remove(getListenerClassName(listener))
		if (java.lang.Boolean.TRUE == stateChanged) {
			listener.onStateChanged()
		}

		if (abortListener != null) {
			onAbortListeners.add(abortListener)

			stateChanged = onAbortListenersCache.remove(CommonUtils.getClassNameFull(abortListener))
			if (java.lang.Boolean.TRUE == stateChanged) {
				abortListener.onAbort()
			}
		}
	}

	final override fun removeCachedListeners(listenerClass: Class<*>) {
		onStateChangedListenersCache.remove(listenerClass.name)
	}

	final override fun removeListener(listener: Listener, temporarily: Boolean) {
		removeListener(listener, null, temporarily)
	}

	final override fun removeListener(listener: Listener, abortListener: OnAbortListener?, temporarily: Boolean) {
		val listenerName = getListenerClassName(listener)

		var exists = onStateChangedListeners.remove(listener)
		if (temporarily) {
			if (exists) {
				onStateChangedListenersCache[listenerName] = false
			}
		} else {
			onStateChangedListenersCache.remove(listenerName)
		}

		if (abortListener != null) {
			val abortListenerName = CommonUtils.getClassNameFull(abortListener)
			exists = onAbortListeners.remove(abortListener)

			if (temporarily) {
				if (exists) {
					onAbortListenersCache[abortListenerName] = false
				}
			} else {
				onAbortListenersCache.remove(abortListenerName)
			}
		}
	}

	final override fun notifyListeners() {
		for (listener in listeners) {
			listener.onStateChanged()
		}
		for (name in onStateChangedListenersCache.keys) {
			onStateChangedListenersCache[name] = true
		}
	}

	private fun notifyAbortListeners() {
		for (listener in getOnAbortListeners()) {
			listener.onAbort()
		}
		for (name in onAbortListenersCache.keys) {
			onAbortListenersCache[name] = true
		}
	}


	final override fun startRequest(request: Abortable?) {
		startRequest(null, request)
	}


	final override fun startRequest(state: StateType?, request: Abortable?) {
		error = null
		isUpdating = true
		this.state = state
		this.request = request

		observer?.onStart()

		notifyListeners()
	}

	final override fun continueRequest(request: Abortable): Abortable {
		if (isUpdating) {
			this.request = request
		} else {
			startRequest(request)
		}

		observer?.onContinue()

		return request
	}

	final override fun stopRequest() {
		stopRequestWithoutResetError()
	}

	private fun stopRequestWithoutResetError() {
		isUpdating = false
		request = null

		observer?.onStop()

		notifyListeners()
	}

	final override fun abortRequest() {
		request?.let {
			it.abort()
			request = null
		}
	}

	@CallSuper
	override fun abort() {
		abortRequest()
		isUpdating = false

		observer?.onAbort()

		notifyAbortListeners()
	}

	final override fun fireNetworkError() {
		error = RequestError.networkError()
		stopRequestWithoutResetError()
	}

	final override fun fireUnknownError() {
		error = RequestError.unknownError()
		stopRequestWithoutResetError()
	}

	final override fun fireError(error: ErrorType) {
		this.error = RequestError(error)
		stopRequestWithoutResetError()
	}

	final override fun fireError(error: RequestError<*>) {
		this.error = error as RequestError<ErrorType>
		stopRequestWithoutResetError()
	}

	final override fun resetError() {
		error = null
	}

	// for debugging reasons
	final override val listenersCount: Int
		get() = onStateChangedListeners.size + onStateChangedListenersCache.size

	protected val isListenersEmpty: Boolean
		get() = onStateChangedListeners.isEmpty()

	protected val isListenersCacheEmpty: Boolean
		get() = onStateChangedListenersCache.isEmpty()


	fun clearListeners() {
		onStateChangedListeners.clear()
		onStateChangedListenersCache.clear()
	}

	interface ListenerExtra : Listener {
		val listenerClassNamePrefix: String
	}

	fun interface Listener {
		fun onStateChanged()
	}

	fun interface OnAbortListener {
		fun onAbort()
	}

	final override fun setObserver(observer: Observer?) {
		this.observer = observer
	}

	open class Observer {
		open fun onStart() {}
		open fun onContinue() {}
		open fun onStop() {}
		open fun onAbort() {}
	}


	companion object {

		@JvmStatic
		fun getListenerClassName(listener: Listener): String {
			return if (listener is ListenerExtra) {
				listener.listenerClassNamePrefix + CommonUtils.getClassNameFull(listener)
			} else {
				CommonUtils.getClassNameFull(listener)
			}
		}
	}
}