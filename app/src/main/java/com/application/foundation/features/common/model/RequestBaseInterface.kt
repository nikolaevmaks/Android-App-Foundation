package com.application.foundation.features.common.model

import com.application.foundation.features.common.model.utils.Abortable
import com.application.foundation.features.common.model.utils.RequestError
import com.application.foundation.features.common.model.RequestBase.Listener
import com.application.foundation.features.common.model.RequestBase.OnAbortListener
import com.application.foundation.features.common.model.RequestBase.Observer

interface RequestBaseInterface<ErrorType, StateType> {

	val isUpdating: Boolean

	val error: RequestError<ErrorType>?

	val state: StateType?

	val request: Abortable?


	fun clear()

	fun addListenerToCacheIfAbsent(listener: Listener, abortListener: OnAbortListener? = null)
	fun addListener(listener: Listener, abortListener: OnAbortListener? = null)

	fun isListenerCached(listener: Listener): Boolean

	fun removeCachedListeners(listenerClass: Class<*>)
	fun removeListener(listener: Listener, temporarily: Boolean)
	fun removeListener(listener: Listener, abortListener: OnAbortListener? = null, temporarily: Boolean)

	fun notifyListeners()

	fun startRequest(request: Abortable?)
	fun startRequest(state: StateType? = null, request: Abortable? = null)
	fun continueRequest(request: Abortable): Abortable

	fun stopRequest()

	fun abortRequest()

	fun fireNetworkError()
	fun fireUnknownError()
	fun fireError(error: ErrorType)
	fun fireError(error: RequestError<*>)

	fun resetError()


	fun setObserver(observer: Observer?)

	// for debugging reasons
	val listenersCount: Int
}