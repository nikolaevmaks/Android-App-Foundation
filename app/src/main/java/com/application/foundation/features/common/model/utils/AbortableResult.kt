package com.application.foundation.features.common.model.utils

// fun for functional interface
// https://stackoverflow.com/questions/33590646/kotlin-use-a-lambda-in-place-of-a-functional-interface
fun interface AbortableResult {
	fun run(): Abortable
}