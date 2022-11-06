package com.application.foundation.features.common.view

import android.animation.Animator
import android.annotation.SuppressLint
import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.transition.Fade
import android.transition.Transition
import android.util.Pair
import android.util.SparseArray
import android.view.*
import android.view.ViewTreeObserver.OnGlobalLayoutListener
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import androidx.annotation.CallSuper
import androidx.annotation.IdRes
import androidx.annotation.LayoutRes
import androidx.annotation.StringRes
import androidx.appcompat.app.ActionBar
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.cardview.widget.CardView
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.view.isVisible
import butterknife.ButterKnife
import com.application.foundation.App
import com.application.foundation.App.Companion.injector
import com.application.foundation.R
import com.application.foundation.features.common.model.utils.RequestError
import com.application.foundation.features.common.presenter.PresenterInterface
import com.application.foundation.features.common.view.utils.TransitionUtils
import com.application.foundation.features.common.view.utils.UpdateListener
import com.application.foundation.features.common.view.utils.inflate
import com.application.foundation.utils.Analytics
import com.application.foundation.utils.CommonUtils
import com.application.foundation.utils.LogUtils
import com.google.android.material.appbar.AppBarLayout
import com.google.android.material.behavior.SwipeDismissBehavior
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.Job
import java.util.*

abstract class BaseActivity : AppCompatActivity(), ActivityInterface {

	companion object {
		val TAG = BaseActivity::class.java.simpleName

		private const val STATE_FRAGMENTS = "fragments"
		private const val STATE_FRAGMENTS_BACK_STACK = "fragmentsBackStack"
		private const val STATE_CURRENT_FRAGMENT_STACK_TAG = "fragmentsCurrentFragmentStackTag"
	}


	// can be not only ScrollView, but NestedScrollView
	private var scrollNullable: ViewGroup? = null
	val scroll: ViewGroup
		get() = scrollNullable!!


	// ~2x performance than RelativeLayout
	final override lateinit var contentParentView: RootLayout
		private set

	private var appBarLayoutNullable: AppBarLayout? = null
	final override val appBarLayout: AppBarLayout
		get() = appBarLayoutNullable!!


	private var toolbarNullable: Toolbar? = null
	val toolbar: Toolbar
		get() = toolbarNullable!!


	private lateinit var viewContent: ViewGroup
	private lateinit var progressInitial: ProgressBar
	private lateinit var viewProgressInitial: View

	private val dialogs: MutableList<Dialog> = ArrayList()

	private lateinit var presenter: PresenterInterface

	private var onCreateViewTimeMillis: Long = 0
	private var viewContentAnimator: ViewPropertyAnimator? = null
	private var viewProgressInitialAnimator: ViewPropertyAnimator? = null
	private var isProgressInitialAnimatingToVisible = false
	private var isContentAnimatingToVisible = false

	private var progressDialogFull: Dialog? = null
	private var progressDialogSmall: Dialog? = null

	protected var snackbar: Snackbar? = null
		private set

	private var isSnackbarShowing = false
	private var snackbarDismissJob: Job? = null

	private var savedInstanceState: Bundle? = null

	final override var windowInsetTop: Int? = null
	final override var windowInsetBottom: Int? = null
	final override var displayCutoutTop: Int? = null

	final override val activity: Activity
		get() = this



	private val isAppUpdatingAndNotSplash: Boolean
		get() = injector.models.isAppUpdating &&
				this !is SplashActivity

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)

		// case: we have application in recents, then update application, then navigate to our app in recents -
		// last activity will be created
		// splash will be launched only if you click app icon
		if (isAppUpdatingAndNotSplash) {
			finish()
			startActivity(Intent(this, SplashActivity::class.java))
			return
		}

		val presenterName = intent.getStringExtra("presenter")

		val presenter: PresenterInterface
		presenter = if (presenterName == null) {
			createPresenter()
		} else {
			try {
				val clazz = Class.forName(presenterName)
				val constructor = clazz.getConstructor()
				constructor.newInstance() as PresenterInterface
			} catch (e: Exception) {
				throw RuntimeException("BaseActivity unable to construct presenter", e)
			}
		}
		setPresenter(presenter)
		presenter.onViewCreationStarted(savedInstanceState)

		title = title

		onCreateViewTimeMillis = CommonUtils.currentTimeMillisForDuration

		this.savedInstanceState = savedInstanceState
	}

	final override fun getPresenter(): PresenterInterface = presenter

	private fun setPresenter(presenter: PresenterInterface) {
		this.presenter = presenter
		presenter.setActivity(this)
	}


	final override val context: Context
		get() = activity

	protected abstract fun createPresenter(): PresenterInterface

	fun setContentViewWithAppbar(@LayoutRes layoutResId: Int) {
		setContentViewWithAppbar(layoutResId, R.layout.appbar)
	}

	fun setContentViewWithAppbar(@LayoutRes layoutResId: Int, @LayoutRes appBarLayoutResId: Int) {
		super.setContentView(R.layout.empty_layout)

		contentParentView = findViewByIdRequired(R.id.view_content_staff)

		if (isSetLightStatusBarBackground) {
			setLightStatusBarBackground(true)
		}

		appBarLayoutNullable = contentParentView.inflate(appBarLayoutResId) as AppBarLayout
		contentParentView.addView(appBarLayout)

		viewProgressInitial = contentParentView.inflate(R.layout.progress_in_activity)
		progressInitial = viewProgressInitial.findViewById(R.id.progress_initial)
		contentParentView.addView(viewProgressInitial)

		viewContent = contentParentView.inflate(layoutResId) as ViewGroup
		contentParentView.addView(viewContent)

		contentParentView.init(appBarLayout, viewProgressInitial, viewContent)

		setInitialVisibility()

		ButterKnife.bind(this)

		scrollNullable = findViewById(R.id.scroll)

		setToolbar()
	}

	override fun setContentView(layoutResId: Int) {
		super.setContentView(R.layout.empty_layout)

		contentParentView = findViewByIdRequired(R.id.view_content_staff)

		if (isSetLightStatusBarBackground) {
			setLightStatusBarBackground(true)
		}

		viewProgressInitial = contentParentView.inflate(R.layout.progress_in_activity)
		progressInitial = viewProgressInitial.findViewById(R.id.progress_initial)
		contentParentView.addView(viewProgressInitial)

		viewContent = contentParentView.inflate(layoutResId) as ViewGroup
		contentParentView.addView(viewContent)

		appBarLayoutNullable = findViewById(R.id.appbar)

		contentParentView.init(null, viewProgressInitial, viewContent)

		setInitialVisibility()

		ButterKnife.bind(this)

		scrollNullable = findViewById(R.id.scroll)

		setToolbar()
	}

	private fun setToolbar() {

		toolbarNullable = findViewById(R.id.toolbar)
		// presenter can be not initialized if isAppUpdatingAndNotSplash in onCreate called
		if (toolbarNullable != null && ::presenter.isInitialized && presenter.isToolbarAsActionBarEnabled) {
			setSupportActionBar(toolbar)

			val enabled = presenter.isToolbarHomeAsUpEnabled
			supportActionBarRequired.setDisplayHomeAsUpEnabled(enabled)
			if (enabled) {
				supportActionBarRequired.setHomeAsUpIndicator(R.drawable.ic_back)
			}
		}
	}

	final override val supportActionBarRequired: ActionBar
		get() = supportActionBar!!

	fun setLightStatusBarBackground(light: Boolean) {
		CommonUtils.setLightStatusBarBackground(contentParentView, light)
	}

	override val contentView: View
		get() = viewContent

	override val isNoContentView: Boolean
		get() = !::viewContent.isInitialized

	protected fun setViewStateSaveEnabled(enabled: Boolean) {
		contentParentView.isSaveFromParentEnabled = enabled
	}

	@CallSuper
	fun onViewCreationFinished() {
		if (isAppUpdatingAndNotSplash) {
			return
		}

		savedInstanceState?.let {
			// moved from restoreFragments because need currentFragmentStackTag for onViewCreationFinished. see MainPresenter current tab
			currentFragmentStackTag = it.getString(STATE_CURRENT_FRAGMENT_STACK_TAG)
		}

		presenter.onViewCreationFinished(savedInstanceState)

		savedInstanceState?.let {
			restoreFragments(it)
		}

		presenter.updateContentVisibility()

		savedInstanceState = null
	}

	protected open val isSetLightStatusBarBackground: Boolean
		get() = true


	// Fragments
	enum class ActivityLifecycleState {
		Created, Started, Resumed, Paused, Stopped, Destroyed
	}



	private var lifecycleState: ActivityLifecycleState = ActivityLifecycleState.Created

	final override fun getLifecycleState(): ActivityLifecycleState = lifecycleState


	final override var currentFragmentStackTag: String? = null
		private set

	// containerViewId<tag, fragment stack>
	private val fragments = SparseArray<MutableMap<String?, Deque<BaseFragment>>>(1)


	private fun restoreFragments(savedInstanceState: Bundle) {

		val stateFragments = savedInstanceState.getBundle(STATE_FRAGMENTS)
		for (containerViewId in stateFragments!!.keySet()) {
			val stacksBundle = stateFragments.getBundle(containerViewId)

			val stacks: MutableMap<String?, Deque<BaseFragment>> = HashMap()
			fragments.put(containerViewId.toInt(), stacks)

			for (stackTag in stacksBundle!!.keySet()) {
				val stackBundle = stacksBundle.getBundle(stackTag)

				val tags: List<String> = stackBundle!!.getStringArrayList(STATE_FRAGMENTS_BACK_STACK)!!

				val stack: Deque<BaseFragment> = ArrayDeque(tags.size)
				stacks[stackTag] = stack

				for (tag in tags) {
					val stateFragment = stackBundle.getBundle(tag)

					val fragment = BaseFragment.restoreFragment(stateFragment!!, presenter, viewContent, stack)
					stack.add(fragment)
				}
			}
		}

		for (fragment in attachedFragments) {
			if (!fragment.isRemovedOrDetached) {
				fragment.navigate()
			}
		}
	}

	private fun saveFragments(outState: Bundle) {

		outState.putString(STATE_CURRENT_FRAGMENT_STACK_TAG, currentFragmentStackTag)

		val stateFragments = Bundle()

		for (i in 0 until fragments.size()) {
			val stacks: Map<String?, Deque<BaseFragment>> = fragments.valueAt(i)

			val stacksBundle = Bundle()

			for (stackTag in stacks.keys) {
				val stack = stacks[stackTag]!!

				val stackBundle = Bundle()

				val tags = ArrayList<String>(stack.size)

				for (fragment in stack) {
					if (!fragment.isMarkedAsRemoved) {
						tags.add(fragment.tag)
						stackBundle.putBundle(fragment.tag, fragment.onSaveInstanceStateStaff())
					}
				}

				stackBundle.putStringArrayList(STATE_FRAGMENTS_BACK_STACK, tags)

				stacksBundle.putBundle(stackTag, stackBundle)
			}

			stateFragments.putBundle(fragments.keyAt(i).toString(), stacksBundle)
		}

		outState.putBundle(STATE_FRAGMENTS, stateFragments)
	}

	private fun prepareStack(containerViewId: Int, stackTag: String?) {

		var stacks = fragments[containerViewId]
		if (stacks == null) {
			stacks = HashMap()
			fragments.put(containerViewId, stacks)
		}
		var stack = stacks[stackTag]
		if (stack == null) {
			stack = ArrayDeque(1)
			stacks[stackTag] = stack
		}
	}

	private fun detachFragmentsFromStack(stack: Deque<BaseFragment>) {
		for (fragment in stack) {
			if (!fragment.isDetached) {
				fragment.detach()
			}
		}
	}

	private fun detachTopFragmentFromStack(stack: Deque<BaseFragment>) {
		val prevFragment = stack.peekLast()
		if (prevFragment != null && !prevFragment.isDetached) {
			prevFragment.detach()
		}
	}

	override fun replaceFragments(fragments: Deque<Pair<BaseFragment, String>>, containerViewId: Int) {

		prepareStack(containerViewId, currentFragmentStackTag)

		val stack = this.fragments[containerViewId][currentFragmentStackTag]!!

		val prevFragment = stack.peekLast()
		val isDetached = prevFragment != null && prevFragment.isDetached

		detachFragmentsFromStack(stack)

		for (pair in fragments) {
			val fragment = pair.first
			val tag = pair.second

			stack.add(fragment)

			fragment.init(presenter, tag, containerViewId, viewContent, stack)
		}

		val fragment = stack.peekLast()
		fragment.navigate()

		invalidateOptionsMenuIfRequired(fragment, if (prevFragment == null || isDetached) null else prevFragment)
	}

	override fun replaceFragment(fragment: BaseFragment, tag: String, stackTag: String?, containerViewId: Int, animate: Boolean) {

		if (animate &&
			TransitionUtils.isTransitionApiAvailable &&
			stackTag == currentFragmentStackTag) {

			val transition: Transition = Fade(Fade.IN)
			transition.duration = 250

			TransitionUtils.beginDelayedTransitionIfPossible(viewContent.findViewById(containerViewId), transition)
		}

		prepareStack(containerViewId, stackTag)

		val stack = fragments[containerViewId][stackTag]!!

		val prevFragment = stack.peekLast()
		val isDetached = prevFragment != null && prevFragment.isDetached

		detachTopFragmentFromStack(stack)

		stack.add(fragment)

		fragment.init(presenter, tag, containerViewId, viewContent, stack)

		if (stackTag == currentFragmentStackTag) {
			fragment.navigate()
			invalidateOptionsMenuIfRequired(fragment, if (prevFragment == null || isDetached) null else prevFragment)
		}
	}

	override fun addFragment(fragment: BaseFragment, tag: String, stackTag: String?, containerViewId: Int, animate: Boolean) {

		if (animate &&
			TransitionUtils.isTransitionApiAvailable &&
			stackTag == currentFragmentStackTag) {

			val transition: Transition = Fade(Fade.IN)
			transition.duration = 250

			TransitionUtils.beginDelayedTransitionIfPossible(viewContent.findViewById(containerViewId), transition)
		}

		prepareStack(containerViewId, stackTag)

		val stack = fragments[containerViewId][stackTag]!!

		stack.add(fragment)

		fragment.init(presenter, tag, containerViewId, viewContent, stack)

		if (stackTag == currentFragmentStackTag) {
			fragment.navigate()
			invalidateOptionsMenuIfRequired(fragment, null)
		}
	}

	override fun setFragmentStack(stackTag: String) {

		if (stackTag == currentFragmentStackTag) {
			return
		}

		var prevFragment: BaseFragment? = null
		var currentFragment: BaseFragment? = null
		var isDetached = false

		var backStack = currentFragmentStack
		if (backStack != null) {
			prevFragment = backStack.peekLast()
			isDetached = prevFragment.isDetached

			detachFragmentsFromStack(backStack)
		}

		currentFragmentStackTag = stackTag

		backStack = currentFragmentStack
		if (backStack != null) {
			currentFragment = backStack.peekLast()
			currentFragment?.navigate()
		}

		invalidateOptionsMenuIfRequired(currentFragment,
				if (prevFragment == null || isDetached) null else prevFragment)
	}



	override fun popFragmentFromStack(animate: Boolean) {

		val backStack = currentFragmentStack
		if (backStack != null && !backStack.isEmpty()) {
			val prevFragment = backStack.pollLast()

			if (animate && TransitionUtils.isTransitionApiAvailable) {
				val transition: Transition = Fade(Fade.OUT)
				transition.duration = 250

				transition.addTarget(prevFragment.view)
				TransitionUtils.beginDelayedTransitionIfPossible(viewContent.findViewById(R.id.view_content_fragments), transition)
			}

			val isDetached = prevFragment.isDetached
			prevFragment.remove()

			val currentFragment = backStack.peekLast()
			currentFragment?.navigate()

			invalidateOptionsMenuIfRequired(currentFragment, if (isDetached) null else prevFragment)
		}
	}

	override fun removeFragment(fragment: BaseFragment) {

		for (i in 0 until fragments.size()) {
			val stacks: Map<String?, Deque<BaseFragment>> = fragments.valueAt(i)

			for (stackTag in stacks.keys) {
				val stack = stacks[stackTag]!!

				if (stack.removeFirstOccurrence(fragment)) {

					val isDetached = fragment.isDetached
					fragment.remove()

					if (stackTag == currentFragmentStackTag && !isDetached) {
						invalidateOptionsMenuIfRequired(null, fragment)
					}
					return
				}
			}
		}
	}

	override fun attachFragment(fragment: BaseFragment) {
		if (fragment.isDetached) {
			fragment.navigate()
			invalidateOptionsMenuIfRequired(fragment, null)
		}
	}

	override fun detachFragment(fragment: BaseFragment) {
		if (!fragment.isDetached) {
			fragment.detach()
			invalidateOptionsMenuIfRequired(null, fragment)
		}
	}

	private fun invalidateOptionsMenuIfRequired(currentFragment: BaseFragment?, prevFragment: BaseFragment?) {
		if (currentFragment != null && currentFragment.getPresenter().hasOptionsMenu ||
			prevFragment != null && prevFragment.getPresenter().hasOptionsMenu) {
			invalidateOptionsMenu()
		}
	}

	override val currentFragmentStack: Deque<BaseFragment>?
		get() = fragments[R.id.view_content_fragments]?.get(currentFragmentStackTag)

	final override val topFragmentOnCurrentStack: BaseFragment?
		get() {
			val stack = currentFragmentStack
			return if (stack == null || stack.isEmpty()) {
				null
			} else {
				stack.peekLast()
			}
		}

	final override val isCurrentFragmentStackEmpty: Boolean
		get() = currentFragmentStack?.isEmpty() ?: true

	final override val attachedFragments: List<BaseFragment>
		get() {
			val attached: MutableList<BaseFragment> = ArrayList(1)
			for (i in 0 until fragments.size()) {
				val stacks: Map<String?, Deque<BaseFragment>> = fragments.valueAt(i)

				for (stack in stacks.values) {
					for (fragment in stack) {
						if (!fragment.isDetached) {
							attached.add(fragment)
						}
					}
				}
			}
			return attached
		}

	final override val allFragments: List<BaseFragment>
		get() {
			val all: MutableList<BaseFragment> = ArrayList()
			for (i in 0 until fragments.size()) {
				val stacks: Map<String?, Deque<BaseFragment>> = fragments.valueAt(i)
				for ((_, value) in stacks) {
					all.addAll(value)
				}
			}
			return all
		}

	private fun findBackStackForFragment(fragment: BaseFragment): Deque<BaseFragment> {
		for (i in 0 until fragments.size()) {
			val stacks: Map<String?, Deque<BaseFragment>> = fragments.valueAt(i)

			for (stack in stacks.values) {
				if (stack.contains(fragment)) {
					return stack
				}
			}
		}
		throw RuntimeException("findBackStackForFragment: fragment not found!")
	}

	final override fun <T : BaseFragment> findFragmentByTag(tag: String): T? {
		for (fragment in allFragments) {
			if (tag == fragment.tag) {
				return fragment as T
			}
		}
		return null
	}

	final override fun <T : BaseFragment> findFragmentByTagInCurrentStack(tag: String): T {
		return findFragmentByTagInCurrentStackNullable(tag)!!
	}

	final override fun <T : BaseFragment> findFragmentByTagInCurrentStackNullable(tag: String): T? {
		val fragments = currentFragmentStack ?: return null
		for (fragment in fragments) {
			if (tag == fragment.tag) {
				return fragment as T
			}
		}
		return null
	}

	final override fun <T : BaseFragment> findFragmentsByTag(tag: String): List<T> {
		val fragments: MutableList<T> = ArrayList(1)
		for (fragment in allFragments) {
			if (tag == fragment.tag) {
				fragments.add(fragment as T)
			}
		}
		return fragments
	}

	final override fun findFragmentsCountStartedWithTag(tag: String): Int {

		var count = 0
		for (fragment in allFragments) {
			if (fragment.tag.startsWith(tag)) {
				count++
			}
		}
		return count
	}

	/////////////
	private fun setInitialVisibility() {
		viewProgressInitial.isVisible = false
		progressInitial.alpha = 0f

		if (isAppUpdatingAndNotSplash || presenter.isContentViewHiddenInitial) {
			viewContent.isVisible = false
			viewContent.alpha = 0f
		}
	}

	// needClearFocusInOnCreate false doesn't focus edit field automatically on samsung s8+, need to call requestFocus
	protected open fun needClearFocusInOnCreate(): Boolean {
		return true
	}

	// This method is called between onStart and onPostCreate
	override fun onRestoreInstanceState(savedInstanceState: Bundle) {

		if (needClearFocusInOnCreate()) {
			contentParentView.isFocusable = true
			contentParentView.isFocusableInTouchMode = true
			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
				contentParentView.isFocusedByDefault = true
			}
		}
		super.onRestoreInstanceState(savedInstanceState)
	}

	// Called when activity start-up is complete (after onStart and onRestoreInstanceState have been called)
	override fun onPostCreate(savedInstanceState: Bundle?) {
		super.onPostCreate(savedInstanceState)

		if (needClearFocusInOnCreate() && savedInstanceState == null) {

			contentParentView.isFocusable = true
			contentParentView.isFocusableInTouchMode = true
			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
				contentParentView.isFocusedByDefault = true
			} else {
				contentParentView.requestFocus()
			}
		}

		presenter.onPostCreate(savedInstanceState)
	}

	override fun onNewIntent(intent: Intent) {
		super.onNewIntent(intent)
		presenter.onNewIntent(intent)
	}

	// you will receive this call immediately before onResume() when your activity is re-starting
	@CallSuper
	override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
		super.onActivityResult(requestCode, resultCode, data)

		if (isAppUpdatingAndNotSplash) {
			return
		}
		presenter.onActivityResult(requestCode, resultCode, data)
	}

	@CallSuper
	override fun onWindowFocusChanged(hasFocus: Boolean) {
		super.onWindowFocusChanged(hasFocus)
		presenter.onWindowFocusChanged(hasFocus)
	}

	override fun setInitialProgressVisibility(visible: Boolean) {

		if (visible != (if (viewProgressInitialAnimator == null) viewProgressInitial.isVisible else isProgressInitialAnimatingToVisible)) {

			viewProgressInitialAnimator?.cancel()

			viewProgressInitial.isVisible = true

			isProgressInitialAnimatingToVisible = visible

			viewProgressInitialAnimator = progressInitial.animate().alpha(if (visible) 1f else 0f).setListener(object : UpdateListener() {
				override fun onAnimationEndWithoutCancel(animation: Animator) {
					if (!visible) {
						viewProgressInitial.isVisible = false
					}
					viewProgressInitialAnimator = null
				}
			}).withLayer()

			viewProgressInitialAnimator!!.start()
		}
	}

	override fun setContentVisibility(visible: Boolean) {

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

				viewContentAnimator = viewContent.animate().alpha(if (visible) 1f else 0f).setListener(object : UpdateListener() {
					override fun onAnimationEndWithoutCancel(animation: Animator) {
						if (!visible) {
							viewContent.isVisible = false
						}
						viewContentAnimator = null
					}
				}).withLayer()

				viewContentAnimator!!.start()
			}
		}
	}

	override fun onKeyDown(keyCode: Int, event: KeyEvent): Boolean {
		return presenter.onKeyDown(keyCode, event) || super.onKeyDown(keyCode, event)
	}

	override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
		super.onRequestPermissionsResult(requestCode, permissions, grantResults)
		presenter.onRequestPermissionsResult(requestCode, permissions, grantResults)
	}

	override fun onCreateOptionsMenu(menu: Menu): Boolean {
		presenter.onCreateOptionsMenu(menu)
		return true
	}

	override fun onOptionsItemSelected(item: MenuItem): Boolean {
		if (checkClick()) {
			return false
		}
		val fragments = currentFragmentStack
		return if (fragments != null && !fragments.isEmpty() && fragments.peekLast().checkClick()) {
			false
		} else presenter.onOptionsItemSelected(item)
	}

	@CallSuper
	override fun onBackPressed() {
		if (checkClickFromBackPressed()) {
			return
		}
		val fragments = currentFragmentStack
		if (fragments != null && !fragments.isEmpty() && fragments.peekLast().checkClick()) {
			return
		}
		if (presenter.onBackPressed()) {
			super.onBackPressed()
		}
	}

	override fun onStart() {
		super.onStart()

		lifecycleState = ActivityLifecycleState.Started

		presenter.onStart()

		for (fragment in attachedFragments) {
			if (!fragment.isRemovedOrDetached && fragment.lifecycleState != ActivityLifecycleState.Started) {
				// i can detach or remove fragment during this cycle or
				// navigate to fragment in presenter.onStart(), no need to call fragment.onStart() twice
				fragment.onStart()
			}
		}
	}

	// Called after onRestoreInstanceState(Bundle), onRestart(), or onPause()
	override fun onResume() {
		super.onResume()

		lifecycleState = ActivityLifecycleState.Resumed

		presenter.onResume()

		for (fragment in attachedFragments) {
			if (!fragment.isRemovedOrDetached && fragment.lifecycleState != ActivityLifecycleState.Resumed) {
				fragment.onResume()
			}
		}
		enableTouches(true)
	}

	override fun onPause() {
		super.onPause()

		lifecycleState = ActivityLifecycleState.Paused

		presenter.onPause()

		for (fragment in attachedFragments) {
			if (!fragment.isRemovedOrDetached && fragment.lifecycleState != ActivityLifecycleState.Paused) {
				fragment.onPause()
			}
		}
	}

	override fun onStop() {
		super.onStop()

		lifecycleState = ActivityLifecycleState.Stopped

		presenter.onStop()
		for (fragment in attachedFragments) {
			if (!fragment.isRemovedOrDetached && fragment.lifecycleState != ActivityLifecycleState.Stopped) {
				fragment.onStop()
			}
		}
	}

	/** If called, this method will occur after onStop for applications targeting platforms starting with Build.VERSION_CODES.P.
	 * For applications targeting earlier platform versions this method will occur before onStop and there are
	 * no guarantees about whether it will occur before or after onPause.  */
	public override fun onSaveInstanceState(outState: Bundle) {
		super.onSaveInstanceState(outState)

		presenter.onSaveInstanceState(outState)

		saveFragments(outState)
	}

	override fun onDestroy() {
		super.onDestroy()

		lifecycleState = ActivityLifecycleState.Destroyed

		// see isAppUpdatingAndNotSplash
		// isAppUpdating can be false in this method. app update on splash can be very quick
		if (!::presenter.isInitialized) {
			return
		}

		// need to destroy fragments before activity because of fragment presenter's isAlive() check
		for (fragment in allFragments) {
			fragment.onDestroyActivity()
		}

		presenter.onDestroy()

		// for preventing leak window exception, last chance to hide dialog
		for (dialog in dialogs) {
			if (dialog is BottomSheetDialogApp) {
				dialog.dismissImmediately()
			} else {
				dialog.dismiss()
			}
		}
		dialogs.clear()
	}


	// snackbar
	override fun showErrorSnackBarWithRetryOnNetworkError(error: RequestError<*>, onRetryClickListener: View.OnClickListener) {
		showErrorSnackBarWithRetryOnNetworkError(error, presenter.isIndefiniteSnackbar, onRetryClickListener)
	}

	override fun showErrorSnackBarWithRetryOnNetworkError(error: RequestError<*>, indefinite: Boolean, onRetryClickListener: View.OnClickListener) {
		showErrorSnackBarWithRetry(error, indefinite, if (error.isNetworkError) onRetryClickListener else null)
	}

	private fun showErrorSnackBar(error: RequestError<*>, indefinite: Boolean) {
		showErrorSnackBarWithRetry(error, indefinite, null)
	}

	private fun showErrorSnackBarWithRetry(error: RequestError<*>, indefinite: Boolean, onRetryClickListener: View.OnClickListener?) {
		showSnackBarWithRetry(error.toPrintableString(), indefinite, onRetryClickListener)
	}

	private fun showSnackBarWithRetry(message: Int, indefinite: Boolean, onRetryClickListener: View.OnClickListener?) {
		showSnackBarWithRetry(getString(message), indefinite, onRetryClickListener)
	}

	private fun showSnackBarWithRetry(message: String, indefinite: Boolean, onRetryClickListener: View.OnClickListener?) {
		showSnackBar(message, getString(R.string.common_retry), indefinite, onRetryClickListener)
	}

	override fun showSnackBar(message: Int) {
		showSnackBar(message, presenter.isIndefiniteSnackbar)
	}

	override fun showSnackBar(message: String) {
		showSnackBar(message, presenter.isIndefiniteSnackbar)
	}

	override fun showSnackBar(message: Int, indefinite: Boolean) {
		showSnackBar(getString(message), null, indefinite, null)
	}

	override fun showSnackBar(message: String, indefinite: Boolean) {
		showSnackBar(message, null, indefinite, null)
	}

	private fun showSnackBar(message: String, action: String?,
							 indefinite: Boolean, onActionClickListener: View.OnClickListener?) {
		// it doesn't show before layout
		CommonUtils.launchDelayed(1000) {

			if (isSnackbarShowing) {
				return@launchDelayed
			}
			val coordinatorLayout = findViewById<CoordinatorLayout>(R.id.coordinator)
			// workaround bug: can't update to 1.1.0. bug with Lint https://github.com/material-components/material-components-android/issues/504
			@SuppressLint("WrongConstant")
			val snackbar = Snackbar.make((if (coordinatorLayout == null || !viewContent.isVisible) contentParentView else coordinatorLayout),
					message,
					if (indefinite) Snackbar.LENGTH_INDEFINITE else Snackbar.LENGTH_LONG)

			if (coordinatorLayout != null) {
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
			}

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

	private var isTouchEventsEnabled = true

	final override fun enableTouches(enabled: Boolean) {
		isTouchEventsEnabled = enabled
	}

	override fun dispatchTouchEvent(ev: MotionEvent): Boolean {

		if (!isTouchEventsEnabled) {
			return true
		}

		if (presenter.needDismissSnackbarOnTouchOutside() && snackbar != null && snackbarDismissJob == null) {
			val pos = IntArray(2)
			snackbar!!.view.getLocationInWindow(pos)
			val snackbarLeft = pos[0]
			val snackbarTop = pos[1]

			if (ev.y < snackbarTop || ev.y > snackbarTop + snackbar!!.view.height || ev.x < snackbarLeft || ev.x > snackbarLeft + snackbar!!.view.width) {
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

		for (fragment in attachedFragments) {
			fragment.dispatchTouchEvent(ev)
		}

		return super.dispatchTouchEvent(ev)
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

			this@BaseActivity.snackbar = null

			onSnackbarDismissed(snackbar, event)

			isSnackbarShowing = false
		}

		override fun onShown(snackbar: Snackbar) {
			super.onShown(snackbar)

			this@BaseActivity.snackbar = snackbar

			onSnackbarShown(snackbar)
		}
	}

	override fun dismissSnackbar() {
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
		scrollNullable?.let {
			it.layoutParams.height = if (snackbar == null) ViewGroup.LayoutParams.MATCH_PARENT else snackbar!!.view.y.toInt() - it.top
			it.requestLayout()
		}
	}


	// dialog

	private fun onDialogShown() {
		presenter.onDialogShown()
	}


	override fun showDialog(@LayoutRes layoutRes: Int, wrapContentWidth: Boolean, onDismissListener: DialogInterface.OnDismissListener): Dialog {
		return showDialog(layoutRes, wrapContentWidth, true, onDismissListener)
	}

	override fun showDialog(@LayoutRes layoutRes: Int, wrapContentWidth: Boolean, cancelable: Boolean,
							onDismissListener: DialogInterface.OnDismissListener?): Dialog {

		return showDialog(if (wrapContentWidth) R.layout.dialog_wrap_content_width else R.layout.dialog_match_parent_width,
				layoutRes, cancelable, onDismissListener)
	}

	@SuppressLint("ClickableViewAccessibility")
	override fun showDialog(@LayoutRes dialogLayoutRes: Int, @LayoutRes layoutRes: Int, cancelable: Boolean,
							onDismissListener: DialogInterface.OnDismissListener?): Dialog {

		val dialog = Dialog(this, R.style.DialogApp)

		val wrappedOnDismissListener = DialogInterface.OnDismissListener { dialog1: DialogInterface ->
			dialogs.remove(dialog1)
			onDismissListener?.onDismiss(dialog1)
		}

		dialog.setOnDismissListener(wrappedOnDismissListener)

		dialogs.add(dialog)

		val viewDialog = LayoutInflater.from(this).inflate(dialogLayoutRes, null) as ViewGroup

		val viewContent: CardView = viewDialog.findViewById(R.id.view_dialog_card)

		if (layoutRes != 0) {
			val view = viewContent.inflate(layoutRes)
			viewContent.addView(view)
		}

		dialog.setContentView(viewDialog)

		dialog.setCancelable(cancelable)

		viewDialog.setOnTouchListener { v: View, event: MotionEvent ->
			if (cancelable) {
				val x = event.x
				val y = event.y

				val left = viewContent.left + viewContent.paddingLeft
				val top = viewContent.top + viewContent.paddingTop
				val right = viewContent.right - viewContent.paddingRight
				val bottom = viewContent.bottom - viewContent.paddingBottom

				if (x < left || x > right || y < top || y > bottom) {
					dialog.dismiss()
				}
			}
			false
		}

		dialog.show()

		onDialogShown()

		return dialog
	}

	override fun showBottomSheetDialog(@LayoutRes layoutRes: Int, onDismissListener: DialogInterface.OnDismissListener?): Dialog {

		val dialog: Dialog = BottomSheetDialogApp(this)

		val wrappedOnDismissListener = DialogInterface.OnDismissListener { dialog1: DialogInterface ->
			dialogs.remove(dialog1)
			onDismissListener?.onDismiss(dialog1)
		}

		dialog.setOnDismissListener(wrappedOnDismissListener)

		dialogs.add(dialog)

		dialog.setContentView(layoutRes)
		dialog.show()

		onDialogShown()

		return dialog
	}


	// progress dialog

	override fun setProgressDialogFullVisibility(visible: Boolean) {
		setProgressDialogFullVisibility(visible, R.string.common_loading)
	}

	override fun setProgressDialogFullVisibility(visible: Boolean, @StringRes messageId: Int?) {
		setProgressDialogFullVisibility(visible, if (visible) getString(messageId!!) else null)
	}

	override fun setProgressDialogFullVisibility(visible: Boolean, message: String?) {
		if (visible) {
			showProgressDialogFull(message!!)
		} else {
			hideProgressDialog()
		}
	}

	override fun showProgressDialogFull(messageId: Int) {
		showProgressDialogFull(messageId, null)
	}

	override fun showProgressDialogFull(message: String) {
		showProgressDialogFull(message, null)
	}

	override fun showProgressDialogFull(messageId: Int, cancelListener: DialogInterface.OnCancelListener?) {
		showProgressDialogFull(getString(messageId), cancelListener)
	}

	override fun showProgressDialogFull(message: String, cancelListener: DialogInterface.OnCancelListener?) {

		hideProgressDialogSmall()

		var dialog = progressDialogFull

		if (dialog == null) {
			dialog = showDialog(R.layout.progress_dialog, true, cancelListener != null) { dialog1: DialogInterface ->
				if (progressDialogFull === dialog1) {
					progressDialogFull = null
				}
			}.also { progressDialogFull = it }
		}
		dialog.setCancelable(cancelListener != null)
		dialog.setOnCancelListener(cancelListener)

		val text = dialog.findViewById<TextView>(R.id.text_progress)
		text.text = message
	}


	override fun setProgressDialogSmallVisibility(visible: Boolean) {
		setProgressDialogSmallVisibility(visible, null)
	}

	override fun setProgressDialogSmallVisibility(visible: Boolean, cancelListener: DialogInterface.OnCancelListener?) {
		if (visible) {
			showProgressDialogSmall(cancelListener)
		} else {
			hideProgressDialog()
		}
	}

	override fun showProgressDialogSmall(cancelListener: DialogInterface.OnCancelListener?) {

		hideProgressDialogFull()

		var dialog = progressDialogSmall

		if (dialog == null) {
			dialog = Dialog(this, R.style.DialogTransparentApp).also { progressDialogSmall = it }

			dialog.setOnDismissListener { dialog1: DialogInterface ->
				dialogs.remove(dialog1)
				if (progressDialogSmall === dialog1) {
					progressDialogSmall = null
				}
			}
			dialogs.add(dialog)

			val viewDialog = LayoutInflater.from(this).inflate(R.layout.progress, null)

			dialog.setContentView(viewDialog)

			dialog.show()

			onDialogShown()
		}

		dialog.setCancelable(cancelListener != null)
		dialog.setOnCancelListener(cancelListener)
	}

	final override val isProgressDialogShowing: Boolean
		get() = progressDialogFull != null || progressDialogSmall != null

	final override fun hideProgressDialog() {
		hideProgressDialogFull()
		hideProgressDialogSmall()
	}

	final override fun hideProgressDialogFull() {
		progressDialogFull?.let {
			it.dismiss()
			progressDialogFull = null
		}
	}

	final override fun hideProgressDialogSmall() {
		progressDialogSmall?.let {
			it.dismiss()
			progressDialogSmall = null
		}
	}

	@SuppressLint("ClickableViewAccessibility")
	override fun showHintsDialog(@LayoutRes layoutRes: Int, onDismissListener: DialogInterface.OnDismissListener?): Dialog {

		val dialog = Dialog(this, R.style.DialogHintsApp)

		dialog.setOnDismissListener { dialog1: DialogInterface ->
			dialogs.remove(dialog1)
			onDismissListener?.onDismiss(dialog1)
		}

		dialogs.add(dialog)

		val viewDialog = LayoutInflater.from(this).inflate(layoutRes, null)

		dialog.setContentView(viewDialog)

		dialog.setCancelable(true)

		viewDialog.setOnClickListener { dialog.dismiss() }

		val buttonClose = dialog.findViewById<Button>(R.id.button_close)
		buttonClose.setOnClickListener { dialog.dismiss() }

		dialog.show()

		onDialogShown()

		return dialog
	}

	final override val isDialogShowing: Boolean
		get() = !dialogs.isEmpty()

	final override val dialogCount: Int
		get() = dialogs.size


	protected open fun checkClick(): Boolean {
		return presenter.checkClick()
	}

	protected open fun checkClickFromBackPressed(): Boolean {
		return presenter.checkClick()
	}

	protected open fun checkClickForDialog(): Boolean {
		return presenter.checkClickForDialog()
	}


	final override fun unpressButtons() {
		unpressButtons(contentParentView)
	}

	private fun unpressButtons(parent: ViewGroup) {
		for (i in 0 until parent.childCount) {
			val view = parent.getChildAt(i)

			// setPressed(false) propagates to all children
			if (view.isPressed) {
				view.isPressed = false

			} else if (view is ViewGroup) {
				unpressButtons(view)
			}
		}
	}


	final override fun <T : View> findViewById(@IdRes id: Int): T? {
		return super.findViewById(id)
	}

	final override fun <T : View> findViewByIdRequired(@IdRes id: Int): T {
		return findViewById(id)!!
	}

	final override val isInMultiWindowModeSafe: Boolean
		get() = Build.VERSION.SDK_INT >= 24 && !isInMultiWindowMode


	val analytics: Analytics
		get() = injector.analytics

	val app: App
		get() = application as App
}