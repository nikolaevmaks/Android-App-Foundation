package com.application.foundation.utils;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import com.application.foundation.Environment;
import static com.application.foundation.App.getInjector;

public class AppUpdatedBroadcastReceiver extends BroadcastReceiver {

	@SuppressLint("UnsafeProtectedBroadcastReceiver")
	@Override
	public void onReceive(Context context, Intent intent) {

		if (Environment.IS_DEVELOPMENT) {
			LogUtils.logD("AppUpdatedBroadcastReceiver", "ACTION_PACKAGE_REPLACED");
		}

		getInjector().getAnalytics().appUpdated(CommonUtils.getVersionCode(getInjector().getApplicationContext()));
	}
}