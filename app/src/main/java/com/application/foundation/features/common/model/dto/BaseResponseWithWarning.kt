package com.application.foundation.features.common.model.dto

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
open class BaseResponseWithWarning : BaseResponse() {

	var warning: Warning? = null

	@JsonClass(generateAdapter = true)
	class Warning (
		val code: String?,
		val message: String?,
		val fields: Map<String, Error.Field?>?
	) {

		fun containsField(code: String): Boolean {
			if (fields != null) {
				return fields.keys.contains(code)
			}
			return false
		}

		val printableMessage: String?
			get() {
				val sb = StringBuilder()
				if (fields != null) {
					for ((_, field) in fields) {
						if (field != null && !field.message.isNullOrEmpty()) {
							if (sb.isNotEmpty()) {
								sb.append('\n')
							}
							sb.append(field.message)
						}
					}
				}
				if (sb.isEmpty() && !message.isNullOrEmpty()) {
					sb.append(message)
				}
				return if (sb.isEmpty()) null else sb.toString()
			}
	}
}