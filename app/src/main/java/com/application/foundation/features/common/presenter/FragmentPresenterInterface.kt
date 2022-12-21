package com.application.foundation.features.common.presenter

import android.content.Context
import android.os.Bundle
import com.application.foundation.features.common.view.ActivityInterface
import com.application.foundation.features.common.view.BaseFragment

interface FragmentPresenterInterface : BasePresenterInterface {

	fun setFragment(fragment: BaseFragment)
	fun getFragment(): BaseFragment


	fun setActivityPresenter(presenter: PresenterInterface)
	fun getActivityPresenter(): PresenterInterface


	override fun getActivity(): ActivityInterface

	override val context: Context

	val argumentsNullable: Bundle?
	val arguments: Bundle

	val hasOptionsMenu: Boolean


	fun onCreate(savedInstanceState: Bundle?)
	fun onViewCreationFinished(savedInstanceState: Bundle?)

	fun onViewAttached()
	fun onViewDetached()

	fun onDestroyView()

	fun onAnimatingScreenMoveEnd()
}