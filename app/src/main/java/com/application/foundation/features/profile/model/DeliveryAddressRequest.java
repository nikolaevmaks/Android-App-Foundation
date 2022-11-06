package com.application.foundation.features.profile.model;

import androidx.annotation.Nullable;
import com.application.foundation.features.common.model.DetectCountry;
import com.application.foundation.features.common.model.RequestBase;
import com.application.foundation.features.profile.model.dto.DeliveryAddressType;
import com.application.foundation.features.profile.model.dto.ProfileType;
import com.application.foundation.features.profile.model.dto.ProfileType.Store;
import com.application.foundation.network.RequestHandlerBaseResponse;
import com.application.foundation.utils.CommonUtils;
import java.util.EnumMap;
import java.util.Map;
import static com.application.foundation.App.getInjector;

public class DeliveryAddressRequest extends RequestBase<Void, Void> {

	private final DeliveryAddress deliveryAddressModel;

	private final Profile profile;

	private final Map<Store, DeliveryAddressType> deliveryAddresses = new EnumMap<>(Store.class);
	private DeliveryAddressType lastDeliveryAddress;

	private Store store;

	DeliveryAddressRequest(DeliveryAddress deliveryAddress) {
		deliveryAddressModel = deliveryAddress;
		profile = getInjector().getProfile();
	}

	@Override
	public void clear() {
		super.clear();

		deliveryAddresses.clear();
		lastDeliveryAddress = null;
		store = null;
	}

	public void requestDeliveryAddress(Store store) {

		if (this.store != null && store != this.store || !isUpdating()) {

			if (store != this.store) {
				abortRequest();
			}
			this.store = store;

			deliveryAddressModel.requestsObserver.perform(this, () ->

					getInjector().getWebClient().requestDeliveryAddress(store,
							new RequestHandlerBaseResponse<DeliveryAddressType>(DeliveryAddressType.class, this,
									responseCode -> {
										if (responseCode == 404) {
											generateDeliveryAddress();

											stopRequest();
											return true;
										} else {
											return false;
										}
									}) {
								@Override
								public void onReceiveValidResult(DeliveryAddressType body) {

									setDeliveryAddressWithoutNotify(body);
									stopRequest();
								}
							}));
		}
	}

	@Nullable Store getStore() {
		return store;
	}

	private void setDeliveryAddressWithoutNotify(DeliveryAddressType deliveryAddress) {

		Store store;
		if (DetectCountry.UK_COUNTRY_CODE.equals(deliveryAddress.getCountryCode())) {
			store = Store.UK;
		} else if (DetectCountry.US_COUNTRY_CODE.equals(deliveryAddress.getCountryCode())) {
			store = Store.US;
		} else {
			store = Store.WW;
		}

		deliveryAddresses.put(store, deliveryAddress);

		lastDeliveryAddress = deliveryAddress;
	}


	public @Nullable DeliveryAddressType getDeliveryAddress(Store store) {
		return deliveryAddresses.get(store);
	}

	public @Nullable DeliveryAddressType getLastDeliveryAddress() {
		return lastDeliveryAddress;
	}

	public boolean isAnyDeliveryAddressExists() {
		return lastDeliveryAddress != null;
	}




	public void setDeliveryAddress(DeliveryAddressType deliveryAddress) {
		abort();
		resetError();

		setDeliveryAddressWithoutNotify(deliveryAddress);

		notifyListeners();
	}

	private void generateDeliveryAddress() {

		ProfileType profile = this.profile.getData();

		DeliveryAddressType deliveryAddress = new DeliveryAddressType(
				profile == null ? null : profile.getFirstName(),
				profile == null ? null : profile.getLastName(),
				null,
				null,
				null,
				null,
				null,
				null);

		setDeliveryAddressWithoutNotify(deliveryAddress);
	}



	void updateFromProfileIfEmpty() {

		ProfileType profile = this.profile.getData();
		if (profile != null) {
			for (DeliveryAddressType deliveryAddress : deliveryAddresses.values()) {

				if (CommonUtils.isStringEmpty(deliveryAddress.getFirstName()) &&
					CommonUtils.isStringEmpty(deliveryAddress.getLastName())) {

					deliveryAddress.setFirstName(profile.getFirstName());
					deliveryAddress.setLastName(profile.getLastName());
				}
			}
		}
	}
}