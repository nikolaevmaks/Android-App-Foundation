package com.application.foundation.features.common.view;

import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import androidx.annotation.LayoutRes;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatDialog;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import com.application.foundation.utils.AbortableRunnable;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.application.foundation.R;
import com.application.foundation.utils.CommonUtils;
import com.application.foundation.utils.LogUtils;
import static com.google.android.material.bottomsheet.BottomSheetBehavior.STATE_EXPANDED;
import static com.google.android.material.bottomsheet.BottomSheetBehavior.STATE_HIDDEN;

// simplified version of BottomSheetDialog
public class BottomSheetDialogApp extends AppCompatDialog {

	private boolean cancelable = true;
	private final boolean canceledOnTouchOutside = true;

	private BottomSheetBehavior behaviour;
	private final ColorDrawable background = new ColorDrawable(ContextCompat.getColor(getContext(), R.color.black));

	private Runnable expandRunnable;


	public BottomSheetDialogApp(@NonNull Context context) {
		super(context, R.style.BottomSheetDialogApp);
	}


	@Override
	public void setContentView(@LayoutRes int layoutResId) {
		super.setContentView(wrapInBottomSheet(layoutResId, null, null));
	}

	@Override
	public void setContentView(View view) {
		super.setContentView(wrapInBottomSheet(0, view, null));
	}

	@Override
	public void setContentView(View view, ViewGroup.LayoutParams params) {
		super.setContentView(wrapInBottomSheet(0, view, params));
	}

	@Override
	public void setCancelable(boolean cancelable) {
		super.setCancelable(cancelable);
		if (this.cancelable != cancelable) {
			this.cancelable = cancelable;
			if (behaviour != null) {
				behaviour.setHideable(cancelable);
			}
		}
	}


	private View wrapInBottomSheet(int layoutResId, View view, ViewGroup.LayoutParams params) {

		FrameLayout container = (FrameLayout) View.inflate(getContext(), R.layout.bottom_sheet_dialog, null);

		CoordinatorLayout coordinator = container.findViewById(R.id.coordinator);

		ViewCompat.setOnApplyWindowInsetsListener(getWindow().getDecorView(), (v, windowInsets) -> {

			Insets insets = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars() | WindowInsetsCompat.Type.ime() | WindowInsetsCompat.Type.displayCutout());

			getWindow().getDecorView().setPadding(insets.left,
					0,
					insets.right,
					insets.bottom);

			ViewGroup.MarginLayoutParams lp = (ViewGroup.MarginLayoutParams) coordinator.getLayoutParams();
			if (lp.topMargin != insets.top) {
				lp.topMargin = insets.top;
				coordinator.requestLayout();
			}

			return WindowInsetsCompat.CONSUMED;
		});

		if (layoutResId != 0 && view == null) {
			view = getLayoutInflater().inflate(layoutResId, coordinator, false);
		}

		FrameLayout bottomSheet = coordinator.findViewById(R.id.design_bottom_sheet);

		behaviour = BottomSheetBehavior.from(bottomSheet);
		behaviour.setBottomSheetCallback(bottomSheetCallback);
		behaviour.setHideable(cancelable);
		if (params == null) {
			bottomSheet.addView(view);
		} else {
			bottomSheet.addView(view, params);
		}



		background.setAlpha(0);

		ViewCompat.setBackground(container, background);


		// We treat the CoordinatorLayout as outside the dialog though it is technically inside
		coordinator.findViewById(R.id.touch_outside).setOnClickListener(view1 -> {
			if (cancelable && isShowing() && canceledOnTouchOutside) {
				hideDialog();
			}
		});

		bottomSheet.setOnTouchListener((view1, event) -> {
			// Consume the event and prevent it from falling through
			return true;
		});

		behaviour.setState(STATE_HIDDEN);

		expandRunnable = () -> behaviour.setState(STATE_EXPANDED);
		CommonUtils.postToMainThread(expandRunnable, 100);


		return container;
	}

	private void hideDialog() {
		behaviour.setState(STATE_HIDDEN);
	}

	private void abortExpandRunnable() {
		if (expandRunnable != null) {
			CommonUtils.removeCallbacksOnMainThread(expandRunnable);
			expandRunnable = null;
		}
	}

	@Override
	public void dismiss() {

		abortExpandRunnable();

		if (behaviour.getState() == STATE_HIDDEN) {
			super.dismiss();
		} else {
			behaviour.setState(STATE_HIDDEN);
		}
	}

	// called from activity onDestroy
	public void dismissImmediately() {
		abortExpandRunnable();
		behaviour.setBottomSheetCallback(null);
		super.dismiss();
	}


	private final BottomSheetBehavior.BottomSheetCallback bottomSheetCallback = new BottomSheetBehavior.BottomSheetCallback() {
		@Override
		public void onStateChanged(@NonNull View bottomSheet, @BottomSheetBehavior.State int newState) {
			if (newState == BottomSheetBehavior.STATE_HIDDEN) {
				cancel();
			}
		}

		@Override
		public void onSlide(@NonNull View bottomSheet, float slideOffset) {

			// hidden ->  collapsed       collapsed -> expanded          expanded -> hidden
			// -1 -> 0                         0 -> 1                         1 -> -1
			// slideOffset can be NaN on Samsung S8 android 8.0
			if (!Float.isNaN(slideOffset)) {
				LogUtils.logD("BottomSheetDialogApp", slideOffset);
				background.setAlpha((int) (255 * 0.4f * (1 + Math.min(0, slideOffset))));
			}
		}
	};
}