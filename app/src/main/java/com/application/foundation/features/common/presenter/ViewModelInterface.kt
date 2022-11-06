package com.application.foundation.features.common.presenter

import com.application.foundation.features.common.model.utils.Creator

interface ViewModelInterface {

	fun <ViewModel : Any> restoreOrCreateViewModel(viewModelTag: String, id: Long, viewModelCreator: Creator<ViewModel>): ViewModel
	fun <ViewModel : Any> restoreOrCreateViewModel(viewModelTag: String, id: String, viewModelCreator: Creator<ViewModel>): ViewModel

	fun <ViewModel : Any> restoreOrCreateViewModel(viewModelTag: String?, viewModelCreator: Creator<ViewModel>): ViewModel
	fun <ViewModel : Any> restoreOrCreateViewModel(viewModelCreator: Creator<ViewModel>): ViewModel

	fun removeViewModels()
}