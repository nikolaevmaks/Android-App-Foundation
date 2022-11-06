package com.application.foundation.features.profile.model

import com.application.foundation.features.common.model.RequestBase
import com.application.foundation.features.profile.model.dto.DeliveryAddressType
import com.application.foundation.App.Companion.injector
import com.application.foundation.network.RequestHandlerBaseResponse

class DeliveryAddressSaveRequest internal constructor(private val deliveryAddress: DeliveryAddress) : RequestBase<Any, Any>() {

	init {
		setObserver(deliveryAddress.requestsObserver.createObserver(this))
	}

	fun requestDeliveryAddressSave(firstName: String?,
								   lastName: String?,
								   countryCode: String?,
								   city: String?,
								   streetAddress1: String?,
								   streetAddress2: String?,
								   postcode: String?,
								   state: String?) {
		if (!isUpdating) {
			val isDeliveryAddressUpdating = deliveryAddress.deliveryAddress.isUpdating

			val address = DeliveryAddressType(
					firstName,
					lastName,
					countryCode,
					city,
					streetAddress1,
					streetAddress2,
					postcode,
					state)

			deliveryAddress.setDeliveryAddress(address)

			deliveryAddress.requestsObserver.perform(this) {
				injector.webClient.requestDeliveryAddressAdd(
						firstName,
						lastName,
						countryCode,
						city,
						streetAddress1,
						streetAddress2,
						postcode,
						state,
						object : RequestHandlerBaseResponse<DeliveryAddressType>(DeliveryAddressType::class.java, this@DeliveryAddressSaveRequest) {

							override fun onReceiveValidResult(body: DeliveryAddressType) {
								deliveryAddress.setDeliveryAddress(body)

								if (isDeliveryAddressUpdating) {
									deliveryAddress.deliveryAddress.abort()
									deliveryAddress.deliveryAddress.requestDeliveryAddress(deliveryAddress.deliveryAddress.store)
								}
								stopRequest()
							}
						})
			}
		}
	}
}