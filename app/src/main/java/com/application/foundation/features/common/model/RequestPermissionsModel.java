package com.application.foundation.features.common.model;

import androidx.annotation.NonNull;

public class RequestPermissionsModel extends RequestBase<Void, Void> {

	public static final String TAG = "RequestPermissionsModel";

	private int requestCode;
	private String[] permissions;
	private int[] grantResults;

	public void onRequestPermissionsResult(int requestCode,
										   @NonNull String[] permissions, @NonNull int[] grantResults) {
		this.requestCode = requestCode;
		this.permissions = permissions;
		this.grantResults = grantResults;
		notifyListeners();
	}

	public int getRequestCode() {
		return requestCode;
	}

	public String[] getPermissions() {
		return permissions;
	}

	public int[] getGrantResults() {
		return grantResults;
	}
}