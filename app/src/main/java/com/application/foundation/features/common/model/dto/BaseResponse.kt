package com.application.foundation.features.common.model.dto

import com.squareup.moshi.JsonClass
import com.application.foundation.utils.CommonUtils
@JsonClass(generateAdapter = true)
open class BaseResponse {

	var error: Error? = null

	@JsonClass(generateAdapter = true)
	class Error (
		val code: String?,
		val message: String?,
		val fields: Map<String, Field?>?
	) {
		fun containsCode(code: String): Boolean {
			if (code == this.code) {
				return true
			}

			if (fields != null) {
				return fields.values.any { it != null && code == it.code }
			}
			return false
		}

		val printableMessage: String?
			get() {
				val sb = StringBuilder()

				if (fields != null) {
					fields.values.asSequence()
						.filter { it != null && !it.message.isNullOrEmpty() }
						.forEach {
							if (sb.isNotEmpty()) {
								sb.append('\n')
							}
							sb.append(it!!.message)
						}
				}

				if (sb.isEmpty() && !message.isNullOrEmpty()) {
					sb.append(message)
				}
				return if (sb.isEmpty()) null else sb.toString()
			}

		@JsonClass(generateAdapter = true)
		class Field (
			val code: String?,
			val message: String?
		)
	}
}