package com.application.foundation.features.profile.model

import com.application.foundation.features.common.model.RequestsObserver
import com.application.foundation.features.profile.model.dto.DeliveryAddressType

class DeliveryAddress {
	@JvmField
	val requestsObserver = RequestsObserver()

	val deliveryAddress = DeliveryAddressRequest(this)

	fun abort() {
		requestsObserver.abort()
		deliveryAddress.abort()
	}

	fun clear() {
		requestsObserver.clear()
		deliveryAddress.clear()
	}

	fun createDeliveryAddressSaveRequest(): DeliveryAddressSaveRequest {
		return DeliveryAddressSaveRequest(this)
	}

	fun setDeliveryAddress(deliveryAddress: DeliveryAddressType) {
		this.deliveryAddress.setDeliveryAddress(deliveryAddress)
	}

	fun updateFromProfileIfEmpty() {
		deliveryAddress.updateFromProfileIfEmpty()
	}
}