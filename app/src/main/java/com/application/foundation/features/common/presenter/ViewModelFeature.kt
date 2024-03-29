package com.application.foundation.features.common.presenter

import com.application.foundation.App.Companion.injector
import com.application.foundation.utils.CommonUtils
import com.application.foundation.features.common.model.utils.Creator

class ViewModelFeature(private val listener: Listener) : ViewModelInterface {

	override fun <ViewModel : Any> restoreOrCreateViewModel(
		viewModelTag: String,
		id: Long,
		viewModelCreator: Creator<ViewModel>
	): ViewModel {
		return restoreOrCreateViewModel(viewModelTag, id.toString(), viewModelCreator)
	}

	override fun <ViewModel : Any> restoreOrCreateViewModel(
		viewModelTag: String,
		id: String,
		viewModelCreator: Creator<ViewModel>
	): ViewModel {
		return restoreOrCreateViewModel(viewModelTag + "_" + id, viewModelCreator)
	}


	override fun <ViewModel : Any> restoreOrCreateViewModel(
		viewModelTag: String?,
		viewModelCreator: Creator<ViewModel>
	): ViewModel {
		val className = CommonUtils.getClassNameFull(listener.getInstance())

		val viewModel = injector.getViewModel<ViewModel>(className, viewModelTag)
		return viewModel ?: viewModelCreator.create().also { injector.setViewModel(className, viewModelTag, it) }
	}

	override fun removeViewModels() {
		injector.removeViewModels(CommonUtils.getClassNameFull(listener.getInstance()))
	}


	fun interface Listener {
		fun getInstance(): Any
	}
}