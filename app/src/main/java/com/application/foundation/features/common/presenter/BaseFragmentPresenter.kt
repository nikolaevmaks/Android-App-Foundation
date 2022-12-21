package com.application.foundation.features.common.presenter

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.annotation.CallSuper
import kotlinx.coroutines.Job
import com.application.foundation.App
import com.application.foundation.App.Companion.injector
import com.application.foundation.R
import com.application.foundation.features.common.model.Models
import com.application.foundation.features.common.model.RequestBase
import com.application.foundation.features.common.model.RequestBase.OnAbortListener
import com.application.foundation.features.common.model.utils.Creator
import com.application.foundation.features.common.view.ActivityInterface
import com.application.foundation.features.common.view.BaseFragment
import com.application.foundation.utils.Analytics
import com.application.foundation.utils.CommonUtils

abstract class BaseFragmentPresenter : FragmentPresenterInterface, RequestsListenersFeature.Listener {

	private lateinit var fragment: BaseFragment
	private lateinit var activityPresenter: PresenterInterface


	private lateinit var activity: ActivityInterface


	private val requestsListenersFeature = RequestsListenersFeature(this)
	private val viewModelFeature = ViewModelFeature { this@BaseFragmentPresenter }

	private var initialProgressShowJob: Job? = null

	protected var isSavedInstanceStateExists = false
		private set

	final override var isActivityStartedForResult = false


	final override fun getFragment(): BaseFragment = fragment

	final override fun setFragment(fragment: BaseFragment) {
		this.fragment = fragment
	}


	final override fun getActivityPresenter(): PresenterInterface = activityPresenter

	final override fun setActivityPresenter(presenter: PresenterInterface) {
		activityPresenter = presenter
		activity = presenter.getActivity()
	}



	override val argumentsNullable: Bundle?
		get() = fragment.arguments
	override val arguments: Bundle
		get() = fragment.arguments!!

	@CallSuper
	override fun onCreate(savedInstanceState: Bundle?) {

		isSavedInstanceStateExists = savedInstanceState != null
		isActivityStartedForResult = savedInstanceState != null && savedInstanceState.getBoolean(BasePresenter.STATE_IS_ACTIVITY_STARTED_FOR_RESULT)
	}

	@CallSuper
	override fun onViewCreationFinished(savedInstanceState: Bundle?) {
		if (isContentViewHiddenInitial) {
			initialProgressShowJob = CommonUtils.launchDelayed(2000) {
				initialProgressShowJob = null
				updateProgressVisibility()
			}
		}
	}

	private fun abortInitialProgressShow() {
		initialProgressShowJob?.let {
			it.cancel()
			initialProgressShowJob = null
		}
	}

	protected open val analyticsScreenAttributes: Map<String, String?>?
		get() = null

	protected fun setTitle(title: CharSequence) {
		activity.setTitle(title)
		fragment.setToolbarTitle(title)
	}

	override val isContentViewHiddenInitial: Boolean
		get() = true

	override val isContentVisible: Boolean
		get() = !isContentViewHiddenInitial

	override val isIndefiniteSnackbar: Boolean
		get() = !isContentVisible

	override fun needDismissSnackbarOnTouchOutside(): Boolean {
		return isContentVisible
	}

	override fun updateContentAndProgressVisibility() {
		updateContentVisibility()
		updateProgressVisibility()
	}

	@CallSuper
	override fun updateContentVisibility() {
		fragment.setContentVisibility(isContentVisible)
	}


	override val isToolbarAsActionBarEnabled: Boolean
		get() = true

	override val isToolbarHomeAsUpEnabled: Boolean
		get() = false

	override val hasOptionsMenu: Boolean
		get() = false

	@CallSuper
	override fun onCreateOptionsMenu(menu: Menu) {
		updateHomeAsUpEnabled()
	}

	@CallSuper
	override fun onOptionsItemSelected(item: MenuItem): Boolean {
		when (item.itemId) {
			android.R.id.home -> {
				if (onOptionsItemUpSelected()) {
					onFinishingAfterBackPress()
					activity.popFragmentFromStack()
				}
			}
		}
		return true
	}

	// return true - pop fragment
	open fun onOptionsItemUpSelected(): Boolean {
		return true
	}

	override fun invalidateOptionsMenu() {
		activity.invalidateOptionsMenu()
	}

	// return true - finish activity
	override fun onBackPressed(): Boolean {
		onFinishingAfterBackPress()
		activity.popFragmentFromStack()
		return false
	}

	private fun updateHomeAsUpEnabled() {
		if (fragment.toolbarNullable != null && isToolbarAsActionBarEnabled) {
			val enabled = isToolbarHomeAsUpEnabled
			activity.supportActionBarRequired.setDisplayHomeAsUpEnabled(enabled)
			if (enabled) {
				activity.supportActionBarRequired.setHomeAsUpIndicator(R.drawable.ic_back)
			}
		}
	}

	@CallSuper
	override fun onViewAttached() {
		updateContentVisibility()
	}

	@CallSuper
	override fun onViewDetached() {
		abortInitialProgressShow()
	}

	@CallSuper
	override fun onAnimatingScreenMoveEnd() {
		fragment.onAnimatingScreenMoveEnd()
	}

	@CallSuper
	override fun onDestroyView() {
	}

	@CallSuper
	override fun onNewIntent(intent: Intent) {
	}

	@CallSuper
	override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
		isActivityStartedForResult = false
	}

	// Listeners
	final override fun isListenerRegistered(request: RequestBase<*, *>, listenerClass: Class<*>): Boolean {
		return requestsListenersFeature.isListenerRegistered(request, listenerClass)
	}

	final override fun isListenerRegistered(request: RequestBase<*, *>, listener: RequestBase.Listener): Boolean {
		return requestsListenersFeature.isListenerRegistered(request, listener)
	}

	final override fun registerRequestListener(request: RequestBase<*, *>, listener: RequestBase.Listener) {
		requestsListenersFeature.registerRequestListener(request, listener)
	}

	final override fun registerRequestListener(request: RequestBase<*, *>, listener: RequestBase.Listener, isManyEqualListenerClassesAllowed: Boolean) {
		requestsListenersFeature.registerRequestListener(request, listener, isManyEqualListenerClassesAllowed)
	}

	final override fun registerRequestListener(request: RequestBase<*, *>,
										 listener: RequestBase.Listener,
										 abortListener: OnAbortListener?,
										 isManyEqualListenerClassesAllowed: Boolean) {
		requestsListenersFeature.registerRequestListener(request, listener, abortListener, isManyEqualListenerClassesAllowed)
	}

	final override fun unregisterRequestListener(request: RequestBase<*, *>, listener: RequestBase.Listener) {
		requestsListenersFeature.unregisterRequestListener(request, listener)
	}

	final override fun unregisterRequestListener(request: RequestBase<*, *>,
										   listener: RequestBase.Listener,
										   abortListener: OnAbortListener?) {
		requestsListenersFeature.unregisterRequestListener(request, listener, abortListener)
	}


	private fun addRequestsListeners() {
		requestsListenersFeature.addRequestsListeners()
	}

	override fun isRequestDied(request: RequestBase<*, *>): Boolean {
		return false
	}


	final override fun removeRequestsListeners(temporarily: Boolean) {
		requestsListenersFeature.removeRequestsListeners(temporarily)
	}

	final override fun removeRequestsListeners(temporarily: Boolean, vararg requests: RequestBase<*, *>) {
		requestsListenersFeature.removeRequestsListeners(temporarily, *requests)
	}



	final override fun <ViewModel : Any> restoreOrCreateViewModel(viewModelTag: String, id: Long, viewModelCreator: Creator<ViewModel>): ViewModel {
		return viewModelFeature.restoreOrCreateViewModel(viewModelTag, id, viewModelCreator)
	}

	final override fun <ViewModel : Any> restoreOrCreateViewModel(viewModelTag: String, id: String, viewModelCreator: Creator<ViewModel>): ViewModel {
		return viewModelFeature.restoreOrCreateViewModel(viewModelTag, id, viewModelCreator)
	}

	final override fun <ViewModel : Any> restoreOrCreateViewModel(viewModelTag: String?, viewModelCreator: Creator<ViewModel>): ViewModel {
		return viewModelFeature.restoreOrCreateViewModel(viewModelTag, viewModelCreator)
	}

	final override fun <ViewModel : Any> restoreOrCreateViewModel(viewModelCreator: Creator<ViewModel>): ViewModel {
		return viewModelFeature.restoreOrCreateViewModel(viewModelCreator)
	}

	final override fun removeViewModels() {
		viewModelFeature.removeViewModels()
	}

	///
	// may be called only between onStart - onStop
	@CallSuper
	override fun onRequestPermissionsResultSafe(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
	}

	override fun updateProgressVisibility() {}

	protected fun setInitialProgressVisibility(visible: Boolean) {
		if (initialProgressShowJob == null) {
			fragment.setInitialProgressVisibility(visible)
		}
	}


	val isRemoving: Boolean
		get() = fragment.isRemoving

	val isFinishing: Boolean
		get() = isRemoving || activity.isFinishing()


	@CallSuper
	override fun onStart() {

		if (activityPresenter.isAlive) {
			addRequestsListeners()
		}
	}

	@CallSuper
	override fun onResume() {

		if (activityPresenter.isAlive) {
			addRequestsListeners()
		}

		updateContentAndProgressVisibility()

		if (isAnalyticsScreenOpenTrackingEnabled) {
			injector.analytics.screenOpened(fragment, analyticsScreenAttributes)
		}
	}

	protected open val isAnalyticsScreenOpenTrackingEnabled: Boolean
		get() = true

	@CallSuper
	override fun onPause() {

		if (isRemoving && !activity.isFinishing()) {
			onFinishing()
		}
	}

	@CallSuper
	override fun onStop() {

		if (activityPresenter.isAlive) {
			requestsListenersFeature.onStop()
			removeRequestsListeners(!isFinishing)
		}
	}

	// If called, this method will occur after onStop for applications targeting platforms starting with Build.VERSION_CODES.P.
	// For applications targeting earlier platform versions this method will occur before onStop and there are no guarantees about whether it will occur before or after onPause.
	@CallSuper
	override fun onSaveInstanceState(outState: Bundle) {
		outState.putBoolean(BasePresenter.STATE_IS_ACTIVITY_STARTED_FOR_RESULT, isActivityStartedForResult)
	}

	override fun onFinishingAfterBackPress(): Boolean {
		return true // no sense now
	}

	override fun onFinishing() {}

	@CallSuper
	override fun onDestroy() {

		if (activityPresenter.isAlive) {
			// additional safety if listener has been added after onStop
			removeRequestsListeners(!isFinishing)

			if (isFinishing) {
				removeViewModels()
			}
		}

		abortInitialProgressShow()
	}

	override fun onStartActivity() {}

	override fun onDialogShown() {}


	override fun startActivity(intent: Intent) {
		activityPresenter.startActivity(intent)
	}

	override fun startActivityForResult(intent: Intent, requestCode: Int) {

		activityPresenter.startActivityForResult(intent, requestCode)

		isActivityStartedForResult = true
		activityPresenter.isActivityStartedForResult = false
	}


	override fun checkClick(): Boolean {
		return activityPresenter.checkClick() || fragment.isMarkedAsDetached || fragment.isMarkedAsRemoved
	}

	override fun checkClickForDialog(): Boolean {
		return activityPresenter.checkClickForDialog() || fragment.isMarkedAsDetached || fragment.isMarkedAsRemoved
	}


	final override val application: App
		get() = injector.application

	final override fun getActivity(): ActivityInterface = activity


	final override val analytics: Analytics
		get() = injector.analytics

	final override val context: Context
		get() = activity.context

	final override val intent: Intent
		get() = activity.getIntent()

	final override val models: Models
		get() = injector.models
}