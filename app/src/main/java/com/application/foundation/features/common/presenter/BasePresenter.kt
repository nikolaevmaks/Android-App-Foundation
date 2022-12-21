package com.application.foundation.features.common.presenter

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.KeyEvent
import android.view.Menu
import android.view.MenuItem
import androidx.annotation.CallSuper
import kotlinx.coroutines.Job
import com.application.foundation.App
import com.application.foundation.App.Companion.injector
import com.application.foundation.features.common.model.Models
import com.application.foundation.features.common.model.RequestBase
import com.application.foundation.features.common.model.RequestBase.OnAbortListener
import com.application.foundation.features.common.model.RequestPermissionsModel
import com.application.foundation.features.common.model.utils.Creator
import com.application.foundation.features.common.view.ActivityInterface
import com.application.foundation.utils.Analytics
import com.application.foundation.utils.CommonUtils
import java.util.*

abstract class BasePresenter : PresenterInterface, RequestsListenersFeature.Listener {

	companion object {
		const val STATE_FORM_DATA = "baseFormData"
		const val STATE_IS_ACTIVITY_STARTED_FOR_RESULT = "baseIsActivityStartedForResult"

		const val EXTRA_NEED_FINISH = "needFinish"
		const val EXTRA_STARTED_FROM_ACTIVITY_NAME = "baseStartedFromActivityName"
		const val EXTRA_BUNDLE = "bundle"

		private val presenters: MutableList<BasePresenter> = ArrayList()


		@JvmStatic
		fun isPresentersContains(clazz: Class<*>): Boolean {
			return presenters.any { it.javaClass == clazz }
		}
	}


	private lateinit var activity: ActivityInterface


	private val requestsListenersFeature = RequestsListenersFeature(this)
	private val viewModelFeature = ViewModelFeature { this@BasePresenter }

	private lateinit var requestPermissionsModel: RequestPermissionsModel

	private var initialProgressShowJob: Job? = null

	protected var isSavedInstanceStateExists = false
		private set

	final override var isActivityStartedForResult = false


	final override fun getActivity(): ActivityInterface = activity

	final override fun setActivity(activity: ActivityInterface) {
		this.activity = activity
	}


	final override val context: Context
		get() = activity.context

	final override val intent: Intent
		get() = activity.getIntent()

	@CallSuper
	override fun onViewCreationStarted(savedInstanceState: Bundle?) {

		isSavedInstanceStateExists = savedInstanceState != null
		isActivityStartedForResult = savedInstanceState != null && savedInstanceState.getBoolean(STATE_IS_ACTIVITY_STARTED_FOR_RESULT)

		presenters.add(this)
		checkAllFinishingActivities()

		requestPermissionsModel = restoreOrCreateViewModel(RequestPermissionsModel.TAG) { RequestPermissionsModel() }

		registerRequestListener(requestPermissionsModel) {
			onRequestPermissionsResultSafe(requestPermissionsModel.requestCode,
					requestPermissionsModel.permissions, requestPermissionsModel.grantResults)
		}
	}

	protected open val analyticsScreenAttributes: Map<String, String>?
		get() = null

	private fun checkAllFinishingActivities() {
		val iterator = presenters.iterator()
		while (iterator.hasNext()) {
			val presenter = iterator.next()
			if (presenter.activity.isFinishing()) {

				// case - activity is finishing, request listener called after onPause (other's activity onStart will be called before onStop)
				// need to remove listeners from finishing activity ASAP
				presenter.removeRequestsListeners(false)
				presenter.removeViewModels()

				for (fragment in presenter.activity.allFragments) {
					if (fragment.isPresenterSet()) {
						fragment.getPresenter().removeRequestsListeners(false)
						fragment.getPresenter().removeViewModels()
					}
				}
				iterator.remove()
			}
		}
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

	override fun onPostCreate(savedInstanceState: Bundle?) {}


	@CallSuper
	override fun onNewIntent(intent: Intent) {
		for (fragment in activity.attachedFragments) {
			fragment.getPresenter().onNewIntent(intent)
		}
	}

	override fun onWindowFocusChanged(hasFocus: Boolean) {}

	@CallSuper
	override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
		// can be called right after onPause in finishing activity
		checkAllFinishingActivities()

		// can start activity in onActivityResult (SmsInvitePresenter)
		// it is called before onResume()
		isStartActivityCalled = false
		if (!isActivityStartedForResult) {
			for (fragment in activity.attachedFragments) {
				if (fragment.getPresenter().isActivityStartedForResult) {
					fragment.getPresenter().onActivityResult(requestCode, resultCode, data)
					break
				}
			}
		}
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
		for (fragment in activity.attachedFragments) {
			fragment.getPresenter().onRequestPermissionsResultSafe(requestCode, permissions, grantResults)
		}
	}

	override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
		requestPermissionsModel.onRequestPermissionsResult(requestCode, permissions, grantResults)
	}

	@CallSuper
	override fun onKeyDown(keyCode: Int, event: KeyEvent): Boolean {
		return false
//		return if (keyCode == KeyEvent.KEYCODE_VOLUME_UP &&
//				this !is BaseSplashPresenter &&
//				this !is DevPresenter &&
//				isDevUnlockerApplicationExists(context)) {
//			startActivity(Intent(context, DevActivity::class.java)
//					.putExtra(EXTRA_STARTED_FROM_ACTIVITY_NAME, CommonUtils.getClassNameSimple(javaClass)))
//			true
//		} else {
//			false
//		}
	}

	@CallSuper
	override fun onCreateOptionsMenu(menu: Menu) {
		for (fragment in activity.attachedFragments) {
			if (fragment.getPresenter().hasOptionsMenu) {
				fragment.getPresenter().onCreateOptionsMenu(menu)
			}
		}
	}

	fun simulateBackPress() {
		onFinishingAfterBackPress()
		activity.finish()
	}

	// return false to allow normal menu processing to proceed
	@CallSuper
	override fun onOptionsItemSelected(item: MenuItem): Boolean {
		val fragments = activity.currentFragmentStack
		if (fragments != null && !fragments.isEmpty() && fragments.peekLast().getPresenter().onOptionsItemSelected(item)) {
			return false
		}
		when (item.itemId) {
			android.R.id.home -> if (needHandleOnHome()) {
				onFinishingAfterBackPress()
				activity.finish()
			}
		}
		return false
	}

	final override fun invalidateOptionsMenu() {
		activity.invalidateOptionsMenu()
	}

	// return true - finish activity
	@CallSuper
	override fun onBackPressed(): Boolean {
		val fragments = activity.currentFragmentStack
		return if (fragments != null && !fragments.isEmpty()) {
			if (fragments.peekLast().getPresenter().onBackPressed()) {
				onFinishingAfterBackPress()
			} else {
				false
			}
		} else {
			onFinishingAfterBackPress()
		}
	}

	protected open fun needHandleOnHome(): Boolean {
		return true
	}

	@CallSuper
	override fun onFinishingAfterBackPress(): Boolean {
		for (fragment in activity.allFragments) {
			if (fragment.isPresenterSet()) {
				fragment.getPresenter().onFinishingAfterBackPress()
			}
		}
		return true
	}

	override fun updateProgressVisibility() {}

	protected fun setInitialProgressVisibility(visible: Boolean) {
		if (initialProgressShowJob == null) {
			activity.setInitialProgressVisibility(visible)
		}
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

	override fun updateContentVisibility() {
		if (!activity.isNoContentView) {
			// KeyboardActivity doesn't have view
			activity.setContentVisibility(isContentVisible)
		}
	}

	override val isToolbarAsActionBarEnabled: Boolean
		get() = true

	override val isToolbarHomeAsUpEnabled: Boolean
		get() = true



	@CallSuper
	override fun onStart() {

		checkAllFinishingActivities()

		isStartActivityCalled = false

		if (isAlive) {
			addRequestsListeners()
		}
	}

	@CallSuper
	override fun onResume() {

		checkAllFinishingActivities()

		isStartActivityCalled = false

		// can finish activity in onActivityResult then onResume will be called
		if (isAlive) {
			// need to add listeners because removeRequestsListeners can be called in onStartActivity:
			// case: added product to cart, clicked Back from Cart instantly - onStop/onStart didn't called
			addRequestsListeners()
		}

		updateContentAndProgressVisibility()

		if (isAnalyticsScreenOpenTrackingEnabled) {
			injector.analytics.screenOpened(activity.activity, analyticsScreenAttributes)
		}
	}

	protected val isAnalyticsScreenOpenTrackingEnabled: Boolean = true


	@CallSuper
	override fun onPause() {

		if (activity.isFinishing()) {
			onFinishing()
		}
		isStartActivityCalledBeforeOnResume = false
	}

	final override val isAlive: Boolean
		get() = presenters.contains(this)

	@CallSuper
	override fun onStop() {

		if (isAlive) {
			requestsListenersFeature.onStop()
			removeRequestsListeners(!activity.isFinishing())
		}
	}

	// If called, this method will occur after onStop for applications targeting platforms starting with Build.VERSION_CODES.P.
	// For applications targeting earlier platform versions this method will occur before onStop and there are no guarantees about whether it will occur before or after onPause.
	@CallSuper
	override fun onSaveInstanceState(outState: Bundle) {
		outState.putBoolean(STATE_IS_ACTIVITY_STARTED_FOR_RESULT, isActivityStartedForResult)
	}

	// not allowed to call startActivity from onFinishing. click on notification will start activity in new task which will force all old activities to finish
	@CallSuper
	override fun onFinishing() {

		for (fragment in activity.allFragments) {
			if (fragment.isPresenterSet()) {
				fragment.getPresenter().onFinishing()
			}
		}
	}

	@CallSuper
	override fun onDestroy() {

		if (isAlive) {
			// additional safety if listener has been added after onStop
			removeRequestsListeners(!activity.isFinishing())
			if (activity.isFinishing()) {
				removeViewModels()
			}
			presenters.remove(this)
		}

		abortInitialProgressShow()
	}

	private var isStartActivityCalled = false

	var isStartActivityCalledBeforeOnResume = false
		private set


	override fun onStartActivity() {}

	override fun startActivity(intent: Intent) {

		if (isStartActivityCalled) {
			return
		}

		// can throw ActivityNotFoundException
		activity.startActivity(intent)

		isStartActivityCalled = true
		isStartActivityCalledBeforeOnResume = true

		onStartActivity()

		for (fragment in activity.attachedFragments) {
			fragment.getPresenter().onStartActivity()
		}
	}

	override fun startActivityForResult(intent: Intent, requestCode: Int) {

		if (isStartActivityCalled) {
			return
		}

		// can throw ActivityNotFoundException
		activity.startActivityForResult(intent, requestCode)

		isStartActivityCalled = true
		isStartActivityCalledBeforeOnResume = true

		onStartActivity()

		for (fragment in activity.attachedFragments) {
			fragment.getPresenter().onStartActivity()
		}

		isActivityStartedForResult = true
	}

	override fun checkClick(): Boolean {
		return activity.isFinishing() || activity.isDialogShowing || checkClickStartActivity() || isAnimatingScreenMove
	}

	override fun checkClickForDialog(): Boolean {
		return activity.isFinishing() || activity.dialogCount > 1 || checkClickStartActivity() || isAnimatingScreenMove
	}

	final override fun checkClickStartActivity(): Boolean {
		return isStartActivityCalled || isWaitForActivityResult
	}

	private var isWaitForActivityResult = false

	final override fun setWaitForActivityResult(flag: Boolean) {
		isWaitForActivityResult = flag
	}

	override var isAnimatingScreenMove = false

	@CallSuper
	override fun onDialogShown() {
		for (fragment in activity.attachedFragments) {
			fragment.getPresenter().onDialogShown()
		}
	}

	final override val application: App
		get() = injector.application

	final override val analytics: Analytics
		get() = injector.analytics

	final override val models: Models
		get() = injector.models




	final override val isLastPresenterOrEmpty: Boolean
		get() = presenters.isEmpty() || presenters.size == 1 && presenters[0] === this

	final override val isTopPresenter: Boolean
		get() = !presenters.isEmpty() && presenters[presenters.size - 1] === this


	final override fun onApplyWindowInsetsFinished() {
		for (fragment in activity.attachedFragments) {
			fragment.updateWindowInsets()
		}
	}
}