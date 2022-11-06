package com.application.foundation.network

import com.application.foundation.utils.CommonUtils
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody
import com.application.foundation.network.WebRequest.HttpMethod

open class RequestBuilder {

	private val builder = Request.Builder()


	open fun url(url: String): RequestBuilder {
		builder.url(url)
		return this
	}

	open fun postForm(builder: FormBody.Builder): RequestBuilder {
		this.builder.post(builder.build())
		return this
	}

	open fun postMultipart(builder: MultipartBody.Builder): RequestBuilder {
		this.builder.post(builder.build())
		return this
	}

	open fun post(): RequestBuilder {
		val requestBody: RequestBody = "".toRequestBody("text/plain; charset=utf-8".toMediaTypeOrNull())
		builder.post(requestBody)
		return this
	}

	open fun delete(): RequestBuilder {
		builder.delete()
		return this
	}

	open fun <T> bodyJson(method: HttpMethod, body: T): RequestBuilder {
		return body(method, "application/json; charset=utf-8", CommonUtils.toJson(body))
	}

	open fun body(method: HttpMethod, contentType: String, body: String): RequestBuilder {

		val requestBody: RequestBody = body.toRequestBody(contentType.toMediaTypeOrNull())
		when (method) {
			HttpMethod.Post -> builder.post(requestBody)
			HttpMethod.Put -> builder.put(requestBody)
			HttpMethod.Patch -> builder.patch(requestBody)
			else -> {
				throw RuntimeException("not possible")
			}
		}
		return this
	}

	open fun addHeader(name: String, value: String): RequestBuilder {
		builder.header(name, value)
		return this
	}

	open fun cacheControl(cacheControl: CacheControl): RequestBuilder {
		builder.cacheControl(cacheControl)
		return this
	}

	fun build(): Request {
		return builder.build()
	}
}