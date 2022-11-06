package com.application.foundation.network

import androidx.annotation.CallSuper
import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.JsonDataException
import com.application.foundation.App.Companion.injector
import com.application.foundation.features.common.model.RequestBase
import com.application.foundation.features.common.model.dto.BaseResponse
import com.application.foundation.features.common.model.utils.JsonValidationRequiredAnnotationChecker
import com.application.foundation.features.common.model.utils.JsonValidationRequiredAnnotationChecker.AnnotationMisuseException
import com.application.foundation.features.common.model.utils.JsonValidationRequiredAnnotationChecker.JsonValidationException
import com.application.foundation.features.common.model.utils.RequestError
import okio.BufferedSource
import java.io.IOException
import java.lang.reflect.Type

abstract class ObjectHandler<T> internal constructor(private val type: Type,
													 protected val request: RequestBase<*, *>) : WebResponseHandler<T>() {

	override fun parse(request: WebRequest, source: BufferedSource, responseCode: Int) {
		try {
			var obj: T? = null
			var error: BaseResponse.Error? = null

			if (!source.exhausted()) {

				if (isSuccessfulResponse(responseCode)) {
					val adapter: JsonAdapter<T> = injector.moshi.adapter(type)
					obj = adapter.fromJson(source)

					JsonValidationRequiredAnnotationChecker().validate(obj)
				} else {
					if (BaseResponse::class.java.isAssignableFrom((type as Class<*>))) {
						val adapter = injector.moshi.adapter(BaseResponse::class.java)
						val baseResponse = adapter.fromJson(source)

						if (baseResponse != null) {
							error = baseResponse.error
						}
					}
				}
			}

			send(request, obj, error, responseCode)

		} catch (exception: IOException) {
			sendNetworkError(request, responseCode, exception)


		} catch (exception: JsonDataException) {
			sendUnknownError(request, responseCode, exception)
			logWebResponseError(exception.toString(), request, responseCode, null)

		} catch (exception: JsonValidationException) {
			sendUnknownError(request, responseCode, exception)
			logJsonValidationError(exception.message, request, responseCode)

		} catch (exception: AnnotationMisuseException) {
			sendUnknownError(request, responseCode, exception)
			logJsonValidationError(exception.message, request, responseCode)

		} catch (exception: Exception) {
			// for example java.lang.ClassCastException: com.squareup.moshi.internal.Util$ParameterizedTypeImpl cannot be cast to java.lang.Class

			sendUnknownError(request, responseCode, exception)
			logWebResponseError(exception.toString(), request, responseCode, null)
		}
	}



	protected open fun onReceiveValidResult() {
	}


	@CallSuper
	public override fun onError(error: RequestError<*>) {
		request.fireError(error)
	}


	interface ResponseCodeHandler {
		fun handleResponseCode(responseCode: Int): Boolean
	}
}