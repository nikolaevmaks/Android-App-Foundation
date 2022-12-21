package com.application.foundation.features.common.view

import android.app.Activity
import android.app.Dialog
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.util.Pair
import android.view.View
import android.view.Window
import androidx.annotation.IdRes
import androidx.annotation.LayoutRes
import androidx.annotation.StringRes
import androidx.appcompat.app.ActionBar
import androidx.appcompat.widget.Toolbar
import com.application.foundation.R
import com.application.foundation.features.common.model.utils.RequestError
import com.application.foundation.features.common.presenter.PresenterInterface
import com.google.android.material.appbar.AppBarLayout
import com.application.foundation.features.common.view.BaseActivity.ActivityLifecycleState
import java.util.*

interface ActivityInterface {

	fun getPresenter(): PresenterInterface

	val activity: Activity
	val context: Context
	fun getIntent(): Intent

	fun setTitle(title: CharSequence)
	fun getWindow(): Window
	fun getString(@StringRes res: Int): String
	val contentParentView: RootLayout
	val contentView: View
	val isNoContentView: Boolean

	fun isFinishing(): Boolean

	fun replaceFragment(fragment: BaseFragment, tag: String, stackTag: String? = currentFragmentStackTag, containerViewId: Int = R.id.view_content_fragments, animate: Boolean = true)
	fun replaceFragments(fragments: Deque<Pair<BaseFragment, String>>, containerViewId: Int = R.id.view_content_fragments)

	fun addFragment(fragment: BaseFragment, tag: String, stackTag: String? = currentFragmentStackTag, containerViewId: Int = R.id.view_content_fragments, animate: Boolean = true)

	val currentFragmentStackTag: String?

	val isCurrentFragmentStackEmpty: Boolean

	fun setFragmentStack(stackTag: String, vararg topFragmentsTagsToAttach: String)

	val allFragments: List<BaseFragment>
	val attachedFragments: List<BaseFragment>
	val currentFragmentStack: Deque<BaseFragment>?
	val topFragmentOnCurrentStack: BaseFragment?

	fun popFragmentFromStack(animate: Boolean = true)
	fun attachFragment(fragment: BaseFragment)
	fun detachFragment(fragment: BaseFragment)
	fun <T : BaseFragment> findFragmentByTag(tag: String): T?
	fun <T : BaseFragment> findFragmentByTagInCurrentStack(tag: String): T
	fun <T : BaseFragment> findFragmentByTagInCurrentStackNullable(tag: String): T?
	fun <T : BaseFragment> findFragmentsByTag(tag: String): List<T>
	fun findFragmentsCountStartedWithTag(tag: String): Int
	fun setSupportActionBar(toolbar: Toolbar?)
	val appBarLayout: AppBarLayout
	fun getSupportActionBar(): ActionBar?

	@Throws(ActivityNotFoundException::class)
	fun startActivity(intent: Intent)
	@Throws(ActivityNotFoundException::class)
	fun startActivityForResult(intent: Intent, requestCode: Int)
	fun setResult(resultCode: Int)
	fun setResult(resultCode: Int, data: Intent)
	fun finish()

	fun overridePendingTransition(enterAnim: Int, exitAnim: Int)

	val supportActionBarRequired: ActionBar

	fun invalidateOptionsMenu()

	fun getLifecycleState(): ActivityLifecycleState


	fun removeFragment(fragment: BaseFragment)

	fun setContentVisibility(visible: Boolean)
	fun setInitialProgressVisibility(visible: Boolean)

	fun showErrorSnackBarWithRetryOnNetworkError(error: RequestError<*>, onRetryClickListener: View.OnClickListener)
	fun showErrorSnackBarWithRetryOnNetworkError(error: RequestError<*>, indefinite: Boolean, onRetryClickListener: View.OnClickListener)
	fun showSnackBar(message: Int)
	fun showSnackBar(message: String)
	fun showSnackBar(message: Int, indefinite: Boolean)
	fun showSnackBar(message: String, indefinite: Boolean)
	fun dismissSnackbar()

	fun showDialog(@LayoutRes layoutRes: Int, wrapContentWidth: Boolean, onDismissListener: DialogInterface.OnDismissListener): Dialog
	fun showDialog(@LayoutRes layoutRes: Int, wrapContentWidth: Boolean, cancelable: Boolean = true,
				   onDismissListener: DialogInterface.OnDismissListener? = null): Dialog
	fun showDialog(@LayoutRes dialogLayoutRes: Int, @LayoutRes layoutRes: Int, cancelable: Boolean = true,
				   onDismissListener: DialogInterface.OnDismissListener? = null): Dialog

	fun showBottomSheetDialog(@LayoutRes layoutRes: Int, onDismissListener: DialogInterface.OnDismissListener? = null): Dialog

	fun setProgressDialogFullVisibility(visible: Boolean)
	fun setProgressDialogFullVisibility(visible: Boolean, @StringRes messageId: Int?)
	fun setProgressDialogFullVisibility(visible: Boolean, message: String?)
	fun showProgressDialogFull(messageId: Int)
	fun showProgressDialogFull(message: String)
	fun showProgressDialogFull(messageId: Int, cancelListener: DialogInterface.OnCancelListener?)
	fun showProgressDialogFull(message: String, cancelListener: DialogInterface.OnCancelListener?)

	fun setProgressDialogSmallVisibility(visible: Boolean)
	fun setProgressDialogSmallVisibility(visible: Boolean, cancelListener: DialogInterface.OnCancelListener?)
	fun showProgressDialogSmall(cancelListener: DialogInterface.OnCancelListener?)

	val isProgressDialogShowing: Boolean
	fun hideProgressDialog()
	fun hideProgressDialogFull()
	fun hideProgressDialogSmall()

	fun showHintsDialog(@LayoutRes layoutRes: Int, onDismissListener: DialogInterface.OnDismissListener?): Dialog

	val isDialogShowing: Boolean
	val dialogCount: Int

	fun unpressButtons()
	fun enableTouches(enabled: Boolean)

	val windowInsetTop: Int?
	val windowInsetBottom: Int?
	val displayCutoutTop: Int?

	fun <T : View> findViewById(@IdRes id: Int): T?
	fun <T : View> findViewByIdRequired(@IdRes id: Int): T

	val isInMultiWindowModeSafe: Boolean
}