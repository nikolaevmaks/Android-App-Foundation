package com.application.foundation.features.common.view

import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import butterknife.BindView
import butterknife.OnClick
import com.application.foundation.R
import com.application.foundation.features.common.presenter.MainPresenter
import com.application.foundation.features.common.presenter.MainPresenterInterface

class MainActivity : BaseActivity() {

	private lateinit var presenter: MainPresenterInterface

	@BindView(R.id.text_products) lateinit var textProducts: TextView
	@BindView(R.id.text_profile) lateinit var textProfile: TextView

	@BindView(R.id.view_bottom_navigation_bar) lateinit var viewBottomNavigationBar: View
	@BindView(R.id.view_bottom_navigation_bar_bottom) lateinit var viewBottomNavigationBarBottom: View

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		setContentView(R.layout.main)


		ViewCompat.setOnApplyWindowInsetsListener(contentView) { _, windowInsets ->

			val insets = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars() or WindowInsetsCompat.Type.ime() or WindowInsetsCompat.Type.displayCutout())

			contentView.setPadding(insets.left,
				0,
				insets.right,
				0)

			windowInsetTop = insets.top
			windowInsetBottom = insets.bottom

			val displayCutout = windowInsets.displayCutout
			displayCutoutTop = displayCutout?.safeInsetTop


			presenter.onApplyWindowInsetsFinished()
			updateWindowInsets()

			WindowInsetsCompat.CONSUMED
		}

		setSystemUi(false, true)

		onViewCreationFinished()
	}

	override fun createPresenter(): MainPresenter {
		return MainPresenter().also { presenter = it }
	}


	private fun updateWindowInsets() {

		val lp = viewBottomNavigationBarBottom.layoutParams
		if (lp.height != windowInsetBottom) {
			lp.height = windowInsetBottom!!
			viewBottomNavigationBarBottom.requestLayout()
		}
	}

	@Suppress("DEPRECATION")
	fun setSystemUi(fullscreen: Boolean, lightNavigationBarBackground: Boolean) {
		activity.window.decorView.systemUiVisibility =
			View.SYSTEM_UI_FLAG_LAYOUT_STABLE or
					View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or
					View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION or
					(if (fullscreen) View.SYSTEM_UI_FLAG_FULLSCREEN else 0) or
					if (Build.VERSION.SDK_INT >= 27 && lightNavigationBarBackground) View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR else 0
	}


	fun selectTab(tab: Tab) {
		textProducts.setTextColor(ContextCompat.getColor(context, if (tab == Tab.Products) R.color.purple_500 else R.color.black_87per))
		textProfile.setTextColor(ContextCompat.getColor(context, if (tab == Tab.Profile) R.color.purple_500 else R.color.black_87per))
	}

	enum class Tab {
		Products, Profile
	}

	@OnClick(R.id.view_products)
	fun onProductsClicked() {
		if (checkClick()) {
			return
		}
		presenter.onProductsClicked()
	}

	@OnClick(R.id.view_profile)
	fun onProfileClicked() {
		if (checkClick()) {
			return
		}
		presenter.onProfileClicked()
	}
}