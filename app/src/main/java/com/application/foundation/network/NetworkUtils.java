package com.application.foundation.network;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import static com.application.foundation.App.getInjector;

public class NetworkUtils {

	private static final ConnectivityManager connectivityManager =
			(ConnectivityManager) getInjector().getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);

	public static boolean isConnected() {
		NetworkInfo activeNetwork = connectivityManager.getActiveNetworkInfo();
		return activeNetwork != null && activeNetwork.isConnectedOrConnecting();
	}
}