package com.application.foundation.features.common.model

import com.application.foundation.features.common.model.RequestBase.OnAbortListener
import com.application.foundation.features.common.model.utils.Abortable
import com.application.foundation.features.common.model.utils.AbortableResult
import com.application.foundation.features.common.model.utils.RequestError

open class RequestWaitPerformer @JvmOverloads constructor(private val target: RequestBase<*, *>,
														  private val request: RequestBase<*, *>,
														  private val onCompleted: AbortableResult,
														  private val onError: Runnable? = null,
														  abortListener: OnAbortListener? = null,
														  private val fireRequestError: Boolean = true) : RequestBase.Listener {
	private val abortListener = OnAbortListener {
		removeListener()
		abortListener?.onAbort()
	}

	private var isUseContinueRequest = false


	@JvmOverloads
	constructor(target: RequestBase<*, *>,
				request: RequestBase<*, *>,
				onCompleted: Runnable,
				onError: Runnable? = null,
				abortListener: OnAbortListener? = null,
				fireRequestError: Boolean = true) :
			this(target, request, onCompleted = convertRunnable(onCompleted), onError, abortListener, fireRequestError)

	@JvmOverloads
	constructor(target: RequestBase<*, *>,
				request: RequestBase<*, *>,
				fireRequestError: Boolean,
				onCompleted: Runnable,
				onError: Runnable? = null,
				abortListener: OnAbortListener? = null) :
			this(target, request, onCompleted = convertRunnable(onCompleted), onError, abortListener, fireRequestError)


	@JvmOverloads
	constructor(target: RequestBase<*, *>,
				request: RequestBase<*, *>,
				fireRequestError: Boolean,
				onCompleted: AbortableResult,
				onError: Runnable? = null,
				abortListener: OnAbortListener? = null) :
			this(target, request, onCompleted, onError, abortListener, fireRequestError)


	open fun useContinueRequest(): RequestWaitPerformer {
		isUseContinueRequest = true
		return this
	}

	fun perform() {
		if (isUseContinueRequest) {
			request.continueRequest(if (target.isUpdating) addListener() else onCompleted.run())
		} else {
			if (target.isUpdating) {
				addListener()
			} else {
				onCompleted.run()
			}
		}
	}

	fun addListener(): Abortable {
		target.addListener(this, abortListener)

		return Abortable { removeListener() }
	}

	fun abort() {
		removeListener()
	}

	private fun removeListener() {
		target.removeListener(this, abortListener, false)
	}

	override fun onStateUpdated(isUpdating: Boolean, error: RequestError<*>?) {
		if (!isUpdating) {
			removeListener()

			if (error == null) {
				handleOnCompleted()

			} else if (fireRequestError) {
				onError?.run()
				request.fireError(error)

			} else {
				handleOnCompleted()
			}
		}
	}

	private fun handleOnCompleted() {
		if (isUseContinueRequest) {
			request.continueRequest(onCompleted.run())
		} else {
			onCompleted.run()
		}
	}

	companion object {
		private fun convertRunnable(onCompleted: Runnable): AbortableResult {
			return AbortableResult {
				onCompleted.run()
				Abortable {}
			}
		}
	}
}