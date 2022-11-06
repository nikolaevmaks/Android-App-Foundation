package com.application.foundation.features.checkout.model

import com.stripe.android.Stripe
import com.application.foundation.App.Companion.injector
import com.application.foundation.features.checkout.model.dto.StripeKeyResponse
import com.application.foundation.features.common.model.RequestBase
import com.application.foundation.features.common.model.RequestWaitPerformer
import com.application.foundation.features.common.model.utils.AbortableResult
import com.application.foundation.network.RequestHandlerBaseResponse
import com.stripe.android.PaymentConfiguration

open class Stripe : RequestBase<Any, Any>() {
	// getStripeApi is allowed to call only after requestStripeKeyIfRequired
	var stripeApi: Stripe? = null
		private set

	private var data: StripeKeyResponse? = null

	override fun clear() {
		super.clear()

		stripeApi = null
		data = null
	}

	fun requestStripeKeyIfRequired(request: RequestBase<*, *>,
								   onCompleted: AbortableResult) {
		if (data == null) {
			val stripeWaitPerformer = RequestWaitPerformer(this, request, onCompleted)
					.useContinueRequest()

			request.continueRequest(stripeWaitPerformer.addListener())

			if (!isUpdating) {
				startRequest(injector.webClient.requestStripeKey(
						object : RequestHandlerBaseResponse<StripeKeyResponse>(StripeKeyResponse::class.java, this@Stripe) {

							override fun onReceiveValidResult(body: StripeKeyResponse) {
								handleStripeKeyResponse(body)
							}
						}))
			}
		} else {
			request.continueRequest(onCompleted.run())
		}
	}

	fun requestStripeKeyIfRequired(): Boolean {
		return if (data == null) {
			if (!isUpdating) {
				startRequest(injector.webClient.requestStripeKey(
						object : RequestHandlerBaseResponse<StripeKeyResponse>(StripeKeyResponse::class.java, this@Stripe) {

							override fun onReceiveValidResult(body: StripeKeyResponse) {
								handleStripeKeyResponse(body)
							}
						}))
			}
			true
		} else {
			false
		}
	}

	private fun handleStripeKeyResponse(body: StripeKeyResponse) {
		data = body

		initStripeApi() // need to init PaymentConfiguration.init(stripeKey) for GooglePayConfig().getTokenizationSpecification()

		stopRequest()
	}

	private fun initStripeApi() {
		PaymentConfiguration.init(injector.applicationContext, data!!.publishableKey)
		stripeApi = Stripe(injector.applicationContext, data!!.publishableKey)
	}
}