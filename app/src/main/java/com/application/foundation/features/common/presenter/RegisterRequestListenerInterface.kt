package com.application.foundation.features.common.presenter

import com.application.foundation.features.common.model.RequestBase
import com.application.foundation.features.common.model.RequestBase.OnAbortListener

interface RegisterRequestListenerInterface {

	fun isListenerRegistered(request: RequestBase<*, *>, listenerClass: Class<*>): Boolean
	fun isListenerRegistered(request: RequestBase<*, *>, listener: RequestBase.Listener): Boolean

	fun registerRequestListener(request: RequestBase<*, *>, listener: RequestBase.Listener)
	fun registerRequestListener(request: RequestBase<*, *>, listener: RequestBase.Listener, isManyEqualListenerClassesAllowed: Boolean)
	fun registerRequestListener(request: RequestBase<*, *>,
								listener: RequestBase.Listener,
								abortListener: OnAbortListener?,
								isManyEqualListenerClassesAllowed: Boolean)

	fun unregisterRequestListener(request: RequestBase<*, *>, listener: RequestBase.Listener)
	fun unregisterRequestListener(request: RequestBase<*, *>,
								  listener: RequestBase.Listener,
								  abortListener: OnAbortListener?)

	fun removeRequestsListeners(temporarily: Boolean)
	fun removeRequestsListeners(temporarily: Boolean, vararg requests: RequestBase<*, *>)
}