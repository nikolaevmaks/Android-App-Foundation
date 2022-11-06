package com.application.foundation.features.common.model.utils

fun interface Creator<T> {
	fun create(): T
}