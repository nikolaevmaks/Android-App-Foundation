package com.application.foundation.features.common.view

import android.animation.Animator
import android.os.Bundle
import com.application.foundation.features.common.presenter.PresenterInterface
import com.application.foundation.features.common.presenter.FragmentPresenterInterface
import androidx.annotation.CallSuper
import com.application.foundation.features.common.view.BaseActivity.ActivityLifecycleState
import com.application.foundation.features.common.view.utils.CleverOnPreDrawListener
import android.util.SparseArray
import android.os.Parcelable
import android.view.ViewGroup.MarginLayoutParams
import butterknife.ButterKnife
import com.application.foundation.features.common.view.utils.UpdateListener
import android.animation.ValueAnimator
import com.application.foundation.features.common.model.utils.RequestError
import kotlin.jvm.JvmOverloads
import android.annotation.SuppressLint
import android.content.Context
import android.content.res.Resources
import android.text.Spanned
import android.view.*
import android.view.ViewTreeObserver.OnGlobalLayoutListener
import android.widget.ProgressBar
import androidx.annotation.StringRes
import androidx.appcompat.widget.Toolbar
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.view.isVisible
import butterknife.Unbinder
import com.google.android.material.appbar.AppBarLayout
import com.google.android.material.behavior.SwipeDismissBehavior
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.Job
import com.application.foundation.R
import com.application.foundation.features.common.view.utils.inflate
import com.application.foundation.utils.CommonUtils
import com.application.foundation.utils.LogUtils
import java.lang.Exception
import java.lang.RuntimeException
import java.util.*
import kotlin.properties.Delegates

abstract class BaseFragment {

	private var containerViewId: Int by Delegates.notNull()

	lateinit var tag: String
		private set

	lateinit var parentView: ViewGroup
		private set

	private lateinit var backStack: Deque<BaseFragment>

	private var appBarLayoutNullable: AppBarLayout? = null
	val appBarLayout: AppBarLayout
		get() = appBarLayoutNullable!!



	var toolbarNullable: Toolbar? = null
	val toolbar: Toolbar
		get() = toolbarNullable!!


	private var viewNullable: ViewGroup? = null
	val view: ViewGroup
		get() = viewNullable!!


	private var viewContentNullable: View? = null
	val viewContent: View
		get() = viewContentNullable!!



	var scrollNullable: ViewGroup? = null
	open val scroll: ViewGroup
		get() = scrollNullable!!


	private var unbinder: Unbinder? = null


	private var state: Bundle? = null

	var isDetached = true
		private set

	var isRemoving = false
		private set

	var isMarkedAsDetached = false
		private set
	var isMarkedAsRemoved = false
		private set

	var arguments: Bundle? = null

	private var focusedViewId = View.NO_ID

	private var isViewStateSaveEnabled = true


	private lateinit var activityPresenter: PresenterInterface
	private lateinit var presenter: FragmentPresenterInterface


	private var viewProgressInitialNullable: View? = null
	private val viewProgressInitial: View
		get() = viewProgressInitialNullable!!

	private var progressInitialNullable: ProgressBar? = null
	private val progressInitial: View
		get() = progressInitialNullable!!

	private var onCreateViewTimeMillis: Long = 0

	private var viewContentAnimator: ViewPropertyAnimator? = null
	private var viewProgressInitialAnimator: ViewPropertyAnimator? = null
	private var isProgressInitialAnimatingToVisible = false
	private var isContentAnimatingToVisible = false

	protected var snackbar: Snackbar? = null
		private set

	private var isSnackbarShowing = false
	private var snackbarDismissJob: Job? = null

	private var onEnterListener: OnEnterListener? = null
	val isEnterListenerSet: Boolean
		get() = onEnterListener != null

	private var viewNavigationBarDummyNullable: View? = null
	val viewNavigationBarDummy: View
		get() = viewNavigationBarDummyNullable!!



	// Fragment lifecycle:
	// ---------------------
	// onCreate

	// onCreateView - only view
	// onViewBound(savedInstanceState) - only view
	// onViewCreationFinished(savedInstanceState) only presenter

	// onViewAttached - user starts to see view

	// updateWindowInsets() - only view

	// onRestoreInstanceState(savedInstanceState) - only view

	// onStart
	// onResume
	// onPause
	// onStop

	// onViewDetached

	// onDestroyView

	// onDestroy()



	fun init(activityPresenter: PresenterInterface,
			 tag: String,
			 containerViewId: Int,
			 viewContent: View,
			 backStack: Deque<BaseFragment>) {

		this.activityPresenter = activityPresenter
		this.tag = tag
		this.containerViewId = containerViewId
		this.backStack = backStack

		parentView = (if (containerViewId == 0) viewContent else viewContent.findViewById(containerViewId)) as ViewGroup
	}

	@CallSuper
	protected open fun onCreate() {
		setPresenter(createPresenter())

		presenter.onCreate(state?.getBundle(STATE_FRAGMENT))
	}

	protected abstract fun createPresenter(): FragmentPresenterInterface

	open fun getPresenter(): FragmentPresenterInterface {
		return presenter
	}

	fun isPresenterSet() = ::presenter.isInitialized

	private fun setPresenter(presenter: FragmentPresenterInterface) {
		this.presenter = presenter

		presenter.setFragment(this)
		presenter.setActivityPresenter(activityPresenter)
	}

	@CallSuper
	fun onStart() {
		lifecycleState = ActivityLifecycleState.Started
		presenter.onStart()
	}

	@CallSuper
	fun onResume() {
		lifecycleState = ActivityLifecycleState.Resumed
		presenter.onResume()

		state = null
	}

	@CallSuper
	fun onPause() {
		lifecycleState = ActivityLifecycleState.Paused
		presenter.onPause()
	}

	@CallSuper
	fun onStop() {
		lifecycleState = ActivityLifecycleState.Stopped
		presenter.onStop()
	}

	val activityLifecycleState: ActivityLifecycleState
		get() = activity.getLifecycleState()


	fun setOnEnterListener(onEnterListener: OnEnterListener?) {
		this.onEnterListener = onEnterListener
	}

	private var onPreDrawListener: CleverOnPreDrawListener? = null

	private val onPreDrawRunnable = Runnable {
		onEnterListener?.onEnter()
	}

	private fun attachView() {

		isDetached = false
		isMarkedAsDetached = false

		// all fragments should already be in back stack
		var pos = 0
		for (fragment in backStack) {
			if (fragment === this) {
				break
			}
			if (!fragment.isDetached) {
				pos++
			}
		}

		parentView.addView(view, pos)

		if (onEnterListener != null) {
			onPreDrawListener!!.startIfNotStarted()
		}
		if (toolbarNullable != null && presenter.isToolbarAsActionBarEnabled) {
			activity.setSupportActionBar(toolbar)
		}
	}

	fun navigate() {
		if (!isPresenterSet()) {
			onCreate()
		}

		if (isDetached || viewNullable == null) {

			val viewIsNull = viewNullable == null
			if (viewIsNull) {
				onCreateViewStaff()
			} else {
				attachView()
			}

			onViewAttached()

			if (activity.windowInsetTop != null || activity.windowInsetBottom != null) {
				updateWindowInsets()
			}

			if (viewIsNull) {
				restoreViewStateIfRequired()
			}

			if (focusedViewId != View.NO_ID) {
				val focusedView = view.findViewById<View>(focusedViewId)
				focusedView?.requestFocus()
				focusedViewId = View.NO_ID
			}

			launchLifecycleAfterNavigate()
		}
	}

	private fun restoreViewStateIfRequired() {
		state?.let {
			val uiState = it.getSparseParcelableArray<Parcelable>(STATE_UI_STATE)
			if (uiState != null) {
				view.restoreHierarchyState(uiState)
				it.remove(STATE_UI_STATE)
			}
			onRestoreInstanceState(it.getBundle(STATE_FRAGMENT))
		}
	}

	@CallSuper
	protected open fun onRestoreInstanceState(savedInstanceState: Bundle?) {
	}

	@CallSuper
	protected open fun onViewAttached() {
		presenter.onViewAttached()
	}


	@CallSuper
	protected open fun onViewBeforeDetached() {
	}

	@CallSuper
	protected open fun onViewDetached() {
		presenter.onViewDetached()
	}

	private fun onCreateViewStaff() {

		val inflater = LayoutInflater.from(parentView.context)

		viewNullable = onCreateView(inflater, parentView)

		view.isSaveEnabled = false
		view.isSaveFromParentEnabled = false

		viewContentNullable = if (getViewContentId() == 0) view else view.findViewById(getViewContentId())

		viewProgressInitialNullable = view.inflate(R.layout.progress_in_fragment)

		appBarLayoutNullable = view.findViewById(R.id.appbar)

		if (appBarLayoutNullable == null && view.findViewById<View>(R.id.view_appbar) == null &&
			!enableAppBarLayoutTopMargin) {

			val lp = viewProgressInitial.layoutParams as MarginLayoutParams
			lp.topMargin = 0
		}

		progressInitialNullable = viewProgressInitial.findViewById(R.id.progress_initial)
		view.addView(viewProgressInitial, 0)

		scrollNullable = view.findViewById(R.id.scroll)

		viewNavigationBarDummyNullable = view.findViewById(R.id.view_navigation_bar_dummy)

		setToolbar()


		onPreDrawListener = CleverOnPreDrawListener(view, onPreDrawRunnable)
		attachView()

		unbinder = ButterKnife.bind(this, view)

		val fragmentState = state?.getBundle(STATE_FRAGMENT)

		onViewBound(fragmentState)

		onCreateViewTimeMillis = CommonUtils.currentTimeMillisForDuration
		setInitialVisibility()

		presenter.onViewCreationFinished(fragmentState)
	}

	protected open val enableAppBarLayoutTopMargin
		get() = false

	fun setToolbar() {
		toolbarNullable = view.findViewById(R.id.toolbar)
	}

	protected abstract fun getViewContentId(): Int

	fun setToolbarTitle(title: CharSequence) {
		toolbar.title = title
	}

	protected abstract fun onCreateView(inflater: LayoutInflater, container: ViewGroup): ViewGroup

	protected open fun onViewBound(savedInstanceState: Bundle?) {}


	private fun setInitialVisibility() {

		viewProgressInitial.isVisible = false
		progressInitial.alpha = 0f

		if (presenter.isContentViewHiddenInitial) {
			viewContent.isVisible = false
			viewContent.alpha = 0f
		}
		onContentVisibilityChanged(viewContent.isVisible, viewContent.alpha)
	}

	fun setInitialProgressVisibility(visible: Boolean) {
		if (viewProgressInitialNullable == null) {
			// called after onDestroyView. example StoreCheckoutPresenter create order's updateProgressVisibility
			return
		}

		if (visible != (if (viewProgressInitialAnimator == null) viewProgressInitial.isVisible else isProgressInitialAnimatingToVisible)) {

			viewProgressInitialAnimator?.cancel()

			viewProgressInitial.isVisible = true

			isProgressInitialAnimatingToVisible = visible

			viewProgressInitialAnimator = progressInitial.animate().alpha(if (visible) 1f else 0f).setListener(object : UpdateListener() {
				override fun onAnimationEndWithoutCancel(animation: Animator) {
					if (!visible) {
						if (!isRemovedOrDetached) {
							viewProgressInitial.isVisible = false
						}
					}
					viewProgressInitialAnimator = null
				}
			}).withLayer()

			viewProgressInitialAnimator!!.start()
		}
	}

	fun setContentVisibility(visible: Boolean) {
		if (viewContentNullable == null) {
			// called after onDestroyView. example StoreCheckoutPresenter create order's updateProgressVisibility
			return
		}


		if (visible != (if (viewContentAnimator == null) viewContent.isVisible else isContentAnimatingToVisible)) {

			val afterOnCreateViewDuration = CommonUtils.currentTimeMillisForDuration - onCreateViewTimeMillis

			LogUtils.logD(TAG, "setContentVisibility $visible")
			LogUtils.logD(TAG, "afterOnCreateViewDuration $afterOnCreateViewDuration")

			viewContentAnimator?.let {
				it.cancel()
				viewContentAnimator = null
			}

			if (afterOnCreateViewDuration < 100) {
				viewContent.isVisible = visible
				viewContent.alpha = if (visible) 1f else 0f
			} else {
				viewContent.isVisible = true

				isContentAnimatingToVisible = visible

				viewContentAnimator = viewContent.animate().alpha(if (visible) 1f else 0f)
						.setUpdateListener(object : UpdateListener() {
							override fun onAnimationUpdate(animation: ValueAnimator) {
								if (!isRemovedOrDetached) {
									onContentVisibilityChanged(visible, viewContent.alpha)
								}
							}
						})
						.setListener(object : UpdateListener() {
							override fun onAnimationEndWithoutCancel(animation: Animator) {
								if (!visible) {
									viewContent.isVisible = false
								}
								viewContentAnimator = null
							}
						}).withLayer()
				viewContentAnimator!!.start()
			}
			onContentVisibilityChanged(visible, viewContent.alpha)
		}
	}

	protected open fun onContentVisibilityChanged(visible: Boolean, alpha: Float) {}

	open fun onAnimatingScreenMoveEnd() {}


	var lifecycleState = ActivityLifecycleState.Created
		private set


	private var callAfterLifecycleLaunchedRunnable: Runnable? = null
	private var lifecycleLaunched = false

	// navigate, detach, remove not allowed to be called from launchLifecycle (for example, during addListeners in onStart)
	// not possible to change fragments in launchLifecycleAfterNavigate
	// because i always call callAfterLifecycleLaunched for any changes in fragments
	private fun launchLifecycleAfterNavigate() {

		lifecycleLaunched = false

		val activityState = activityLifecycleState

		if (activityState == ActivityLifecycleState.Started) {
			onStart()
		} else if (activityState == ActivityLifecycleState.Resumed) {
			onStart()
			onResume()
		} else if (activityState == ActivityLifecycleState.Paused) {
			onStart()
			onResume()
			onPause()
		} else if (activityState == ActivityLifecycleState.Stopped) {
//			onStart();
//			onResume();
//			onPause();
//			onStop();
		} else if (activityState == ActivityLifecycleState.Destroyed) {
			onDestroyActivity()
		}

		setLifecycleLaunched()
	}

	private fun setLifecycleLaunched() {

		lifecycleLaunched = true

		callAfterLifecycleLaunchedRunnable?.let {
			// need to set null before runnable.run(), can remove fragment in runnable
			callAfterLifecycleLaunchedRunnable = null
			it.run()
		}
	}

	fun callAfterLifecycleLaunched(runnable: Runnable) {
		if (lifecycleLaunched) {
			runnable.run()
		} else {
			callAfterLifecycleLaunchedRunnable = runnable
		}
	}

	fun onDestroyActivity() {
		if (!isPresenterSet()) {
			return
		}

		if (viewNullable != null) {
			if (!isDetached) {
				detachView()
			}
			onDestroyView()
		}
		onDestroy()
	}

	val isRemovedOrDetached: Boolean
		get() = isRemoving || isDetached

	fun remove() {
		if (!isPresenterSet()) {
			return
		}

		if (!isRemoving) {
			isRemoving = true

			lifecycleLaunched = false


			if (lifecycleState == ActivityLifecycleState.Started) {
				onStop()
			} else if (lifecycleState == ActivityLifecycleState.Resumed) {
				onPause()
				onStop()
			} else if (lifecycleState == ActivityLifecycleState.Paused) {
				onStop()
			}

			if (lifecycleState != ActivityLifecycleState.Destroyed) {
				onDestroyActivity()
			}

			setLifecycleLaunched()
		}
	}



	fun markAsDetached() {
		isMarkedAsDetached = true
	}

	fun markAsRemoved() {
		isMarkedAsRemoved = true
	}


	fun detach() {
		if (!isPresenterSet()) {
			return
		}

		if (!isDetached) {
			lifecycleLaunched = false

			if (lifecycleState == ActivityLifecycleState.Started) {
				onStop()
			} else if (lifecycleState == ActivityLifecycleState.Resumed) {
				onPause()
				onStop()
			} else if (lifecycleState == ActivityLifecycleState.Paused) {
				onStop()
			}
			if (lifecycleState != ActivityLifecycleState.Destroyed) {
				detachView()
			}

			setLifecycleLaunched()
		}
	}

	private fun detachView() {
		isDetached = true

		val focusedView = view.findFocus()
		focusedViewId = focusedView?.id ?: View.NO_ID

		CommonUtils.hideKeyboard(activity.activity)

		onPreDrawListener!!.cancel()


		onViewBeforeDetached()

		parentView.removeView(view)

		onViewDetached()
	}

	@CallSuper
	protected open fun onDestroyView() {
		viewNullable = null

		appBarLayoutNullable = null
		toolbarNullable = null
		viewContentNullable = null
		scrollNullable = null

		viewProgressInitialNullable = null
		progressInitialNullable = null

		snackbar = null

		viewNavigationBarDummyNullable = null

		unbinder!!.unbind()
		unbinder = null


		presenter.onDestroyView()
	}

	fun onSaveInstanceStateStaff(): Bundle {

		return (state ?: Bundle().also { state = it }).apply {
			putString(STATE_FRAGMENT_CLASS_NAME, this@BaseFragment.javaClass.name)
			putInt(STATE_CONTAINER_VIEW_ID, containerViewId)
			putString(STATE_TAG, tag)

			putBundle(STATE_ARGUMENTS, arguments)

			putBoolean(STATE_IS_DETACHED, isDetached || isMarkedAsDetached)

			if (viewNullable != null && (!isDetached || !getBoolean(STATE_IS_STATE_SAVED))) {

				if (isViewStateSaveEnabled) {
					val uiState = SparseArray<Parcelable>()

					view.isSaveEnabled = true
					view.saveHierarchyState(uiState)
					view.isSaveEnabled = false

					putSparseParcelableArray(STATE_UI_STATE, uiState)
					putInt(STATE_FOCUSED_VIEW_ID, focusedViewId)
				}

				val additional = Bundle()
				onSaveInstanceState(additional)
				putBundle(STATE_FRAGMENT, additional)

				putBoolean(STATE_IS_STATE_SAVED, true)
			}
		}
	}

	protected fun setViewStateSaveEnabled(enabled: Boolean) {
		isViewStateSaveEnabled = enabled
	}

	@CallSuper
	protected open fun onSaveInstanceState(outState: Bundle?) {
		presenter.onSaveInstanceState(outState!!)
	}

	@CallSuper
	protected fun onDestroy() {
		lifecycleState = ActivityLifecycleState.Destroyed
		presenter.onDestroy()
	}

	val activity: ActivityInterface
		get() = activityPresenter.getActivity()

	val context: Context
		get() = activity.context

	val resources: Resources
		get() = context.resources


	// snackbar
	fun showErrorSnackBarWithRetryOnNetworkError(error: RequestError<*>, onRetryClickListener: View.OnClickListener) {
		showErrorSnackBarWithRetryOnNetworkError(error, presenter.isIndefiniteSnackbar, onRetryClickListener)
	}

	fun showErrorSnackBarWithRetryOnNetworkError(error: RequestError<*>, indefinite: Boolean, onRetryClickListener: View.OnClickListener) {
		showErrorSnackBarWithRetry(error, indefinite, if (error.isNetworkError) onRetryClickListener else null)
	}

	private fun showErrorSnackBar(error: RequestError<*>, indefinite: Boolean) {
		showErrorSnackBarWithRetry(error, indefinite, null)
	}

	private fun showErrorSnackBarWithRetry(error: RequestError<*>, indefinite: Boolean, onRetryClickListener: View.OnClickListener?) {
		showSnackBarWithRetry(error.toPrintableString(), indefinite, onRetryClickListener)
	}

	private fun showSnackBarWithRetry(message: Int, indefinite: Boolean, onRetryClickListener: View.OnClickListener?) {
		showSnackBarWithRetry(context.getString(message), indefinite, onRetryClickListener)
	}

	private fun showSnackBarWithRetry(message: String, indefinite: Boolean, onRetryClickListener: View.OnClickListener?) {
		showSnackBar(message, context.getString(R.string.common_retry), indefinite, onRetryClickListener)
	}

	@JvmOverloads
	fun showSnackBar(message: Int, indefinite: Boolean = presenter.isIndefiniteSnackbar) {
		showSnackBar(getString(message), null, indefinite, null)
	}

	@JvmOverloads
	fun showSnackBar(message: String, indefinite: Boolean = presenter.isIndefiniteSnackbar) {
		showSnackBar(message, null, indefinite, null)
	}

	private fun showSnackBar(message: String, action: String?,
							 indefinite: Boolean, onActionClickListener: View.OnClickListener?) {

		// it doesn't show before layout
		CommonUtils.launchDelayed(1000) {
			if (isSnackbarShowing || viewNullable == null || view.parent == null) {
				// fragment detached or removed
				return@launchDelayed
			}

			// workaround bug: can't update to 1.1.0. bug with Lint https://github.com/material-components/material-components-android/issues/504
			@SuppressLint("WrongConstant")
			val snackbar = Snackbar.make(if (viewContent is CoordinatorLayout && viewContent.isVisible) viewContent else view,
					message,
					if (indefinite) Snackbar.LENGTH_INDEFINITE else Snackbar.LENGTH_LONG)


			// required for Nexus 5X
			viewNavigationBarDummyNullable?.let {
				snackbar.setAnchorView(it)
			}

			val layout = snackbar.view as Snackbar.SnackbarLayout
			layout.viewTreeObserver.addOnGlobalLayoutListener(object : OnGlobalLayoutListener {
				@SuppressLint("RestrictedApi")
				override fun onGlobalLayout() {
					val lp = layout.layoutParams
					if (lp is CoordinatorLayout.LayoutParams) {
						lp.behavior = DisableSwipeBehavior()
						layout.layoutParams = lp
					}
					layout.viewTreeObserver.removeOnGlobalLayoutListener(this)
				}
			})

			snackbar.addCallback(snackbarCallback)

			if (onActionClickListener != null) {
				val wrappedListener = View.OnClickListener { v ->
					onActionClickListener.onClick(v)
					onSnackbarDismissionStarted()
					isSnackbarShowing = false
				}

				snackbar.setAction(action, wrappedListener)
			}
			snackbar.show()

			isSnackbarShowing = true

			abortSnackbarDismiss()
		}
	}

	fun dispatchTouchEvent(ev: MotionEvent) {

		if (presenter.needDismissSnackbarOnTouchOutside() && snackbar != null && snackbarDismissJob == null) {
			val pos = IntArray(2)
			snackbar!!.view.getLocationInWindow(pos)
			val snackbarLeft = pos[0]
			val snackbarTop = pos[1]

			if (ev.y < snackbarTop || ev.y > snackbarTop + snackbar!!.view.height ||
					ev.x < snackbarLeft || ev.x > snackbarLeft + snackbar!!.view.width) {
				//position not in snackbar
				isSnackbarShowing = false

				snackbarDismissJob = CommonUtils.launchDelayed(2000) {
					snackbar?.let {
						onSnackbarDismissionStarted()
						it.dismiss()
					}
					snackbarDismissJob = null
				}
			}
		}
	}

	private fun abortSnackbarDismiss() {
		snackbarDismissJob?.let {
			it.cancel()
			snackbarDismissJob = null
		}
	}

	// need only in CoordinatorLayout
	private class DisableSwipeBehavior : SwipeDismissBehavior<Snackbar.SnackbarLayout>() {
		override fun canSwipeDismissView(view: View): Boolean {
			return false
		}
	}

	private val snackbarCallback: Snackbar.Callback = object : Snackbar.Callback() {

		override fun onDismissed(snackbar: Snackbar, event: Int) {
			super.onDismissed(snackbar, event)

			this@BaseFragment.snackbar = null

			onSnackbarDismissed(snackbar, event)

			isSnackbarShowing = false
		}

		override fun onShown(snackbar: Snackbar) {
			super.onShown(snackbar)

			this@BaseFragment.snackbar = snackbar

			onSnackbarShown(snackbar)
		}
	}

	fun dismissSnackbar() {
		abortSnackbarDismiss()

		snackbar?.let {
			onSnackbarDismissionStarted()
			it.dismiss()
		}
	}

	// called only if snackbar duration indefinite is set
	protected open fun onSnackbarDismissionStarted() {
		scrollNullable?.let {
			it.layoutParams.height = ViewGroup.LayoutParams.MATCH_PARENT
			it.requestLayout()
		}
	}

	protected open fun onSnackbarDismissed(snackbar: Snackbar, event: Int) {
		updateScrollHeight()
	}

	protected open fun onSnackbarShown(snackbar: Snackbar) {
		updateScrollHeight()
	}

	private fun updateScrollHeight() {
		if (isSnackbarScrollHeightChangeEnabled) {
			scrollNullable?.let {
				it.layoutParams.height = if (snackbar == null) ViewGroup.LayoutParams.MATCH_PARENT else snackbar!!.view.y.toInt() - it.top
				it.requestLayout()
			}
		}
	}

	open val isSnackbarScrollHeightChangeEnabled: Boolean
		get() = true

	fun getString(@StringRes resId: Int): String {
		return context.getString(resId)
	}

	open fun checkClick(): Boolean {
		return presenter.checkClick()
	}

	open fun checkClickForDialog(): Boolean {
		return presenter.checkClickForDialog()
	}

	open fun updateWindowInsets() {}

	fun interface OnEnterListener {
		fun onEnter()
	}

	companion object {
		val TAG = BaseFragment::class.java.simpleName

		private const val STATE_FRAGMENT_CLASS_NAME = "fragmentClassName"
		private const val STATE_CONTAINER_VIEW_ID = "fragmentContainerViewId"
		private const val STATE_TAG = "fragmentTag"
		private const val STATE_ARGUMENTS = "fragmentArguments"
		private const val STATE_IS_DETACHED = "fragmentIsDetached"
		private const val STATE_IS_STATE_SAVED = "fragmentIsStateSaved"
		private const val STATE_UI_STATE = "fragmentUiState"
		private const val STATE_FOCUSED_VIEW_ID = "fragmentFocusedViewId"
		private const val STATE_FRAGMENT = "fragmentState"


		fun restoreFragment(stateFragment: Bundle,
							presenter: PresenterInterface,
							viewContent: View,
							backStack: Deque<BaseFragment>): BaseFragment {

			val fragmentClassName = stateFragment.getString(STATE_FRAGMENT_CLASS_NAME)!!
			stateFragment.remove(STATE_FRAGMENT_CLASS_NAME)

			val fragment: BaseFragment
			fragment = try {
				val clazz = Class.forName(fragmentClassName)
				val constructor = clazz.getConstructor()
				constructor.newInstance() as BaseFragment
			} catch (e: Exception) {
				throw RuntimeException("BaseFragment unable to construct fragment", e)
			}

			fragment.state = stateFragment

			fragment.init(presenter,
					stateFragment.getString(STATE_TAG)!!,
					stateFragment.getInt(STATE_CONTAINER_VIEW_ID),
					viewContent,
					backStack)

			stateFragment.remove(STATE_CONTAINER_VIEW_ID)
			stateFragment.remove(STATE_TAG)

			fragment.arguments = stateFragment.getBundle(STATE_ARGUMENTS)
			stateFragment.remove(STATE_ARGUMENTS)

			fragment.isDetached = stateFragment.getBoolean(STATE_IS_DETACHED)
			stateFragment.remove(STATE_IS_DETACHED)

			fragment.focusedViewId = stateFragment.getInt(STATE_FOCUSED_VIEW_ID, View.NO_ID)
			stateFragment.remove(STATE_FOCUSED_VIEW_ID)

			return fragment
		}
	}
}