package com.application.foundation.features.common.view.utils;

import android.view.View;
import android.view.ViewTreeObserver;
import com.application.foundation.utils.LogUtils;

public class CleverOnPreDrawListener implements ViewTreeObserver.OnPreDrawListener, View.OnAttachStateChangeListener {

	private static final String TAG = CleverOnPreDrawListener.class.getSimpleName();

	private final View view;
	private final Runnable runnable;
	private boolean isStarted;

	public CleverOnPreDrawListener(View view, Runnable runnable) {
		this.view = view;
		this.runnable = runnable;
	}

	public void startIfNotStarted() {
		if (!isStarted) {
			isStarted = true;

			view.addOnAttachStateChangeListener(this);

			if (view.getWindowToken() != null) {
				view.getViewTreeObserver().addOnPreDrawListener(this);
			}
		}
	}

	@Override
	public void onViewAttachedToWindow(View view) {
		view.getViewTreeObserver().addOnPreDrawListener(this);
		LogUtils.logD(TAG, "onViewAttachedToWindow: height " + view.getHeight() + "  visible " + view.getVisibility());
	}

	@Override
	public void onViewDetachedFromWindow(View view) {
		view.getViewTreeObserver().removeOnPreDrawListener(this);
		LogUtils.logD(TAG, "onViewDetachedFromWindow: height " + view.getHeight() + "  visible " + view.getVisibility());
	}

	@Override
	public boolean onPreDraw() {

		LogUtils.logD(TAG, "OnPreDrawListener height " + view.getHeight() + "  visible " + view.getVisibility() +
				" isLaidOut " + view.isLaidOut() + " isLayoutRequested " + view.isLayoutRequested());

		// see view isLayoutValid
		if (!view.isLaidOut() || view.isLayoutRequested()) {
			return true;
		}

		cancel();

		runnable.run();

		return true;
	}

	public void cancel() {
		if (isStarted) {
			view.removeOnAttachStateChangeListener(this);
			view.getViewTreeObserver().removeOnPreDrawListener(this);

			isStarted = false;
		}
	}
}