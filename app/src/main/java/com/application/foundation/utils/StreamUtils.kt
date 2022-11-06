package com.application.foundation.utils

import android.net.Uri
import com.application.foundation.App.Companion.injector
import com.application.foundation.network.RequestBuilder
import com.application.foundation.network.WebResponseHandler
import kotlin.Throws
import java.io.*
import java.lang.StringBuilder
import java.nio.charset.StandardCharsets

object StreamUtils {

	private val TAG = StreamUtils::class.java.simpleName

	@JvmStatic
	fun readStringFromInputStream(inputStream: InputStream): String? {

		var reader: BufferedReader? = null
		try {
			reader = BufferedReader(InputStreamReader(inputStream, StandardCharsets.UTF_8))
			var line: String?

			return StringBuilder().apply {
				while (reader.readLine().also { line = it } != null) {
					append(line)
				}
			}.toString()

		} catch (e: IOException) {
			LogUtils.logError(TAG, e, "readStringFromInputStream")
		} finally {
			closeQuietly(reader)
		}

		return null
	}

	@JvmStatic
	@Throws(IOException::class)
	fun getInputStream(url: String): InputStream? {

		val response = injector.webClient.client
			.newCall(RequestBuilder().url(url).build()).execute()

		val code = response.code
		if (WebResponseHandler.isSuccessfulResponse(code)) {
			return response.body!!.byteStream()
		} else {
			LogUtils.logError(TAG, "$url error $code", "getInputStream", url)
		}
		return null
	}

	@JvmStatic
	@Throws(FileNotFoundException::class)
	fun getInputStream(uri: Uri): InputStream {
		return BufferedInputStream(injector.applicationContext.contentResolver.openInputStream(uri))
	}

	@JvmStatic
	fun closeQuietly(closeable: Closeable?) {
		if (closeable != null) {
			try {
				closeable.close()
			} catch (e: IOException) {
				LogUtils.logError(TAG, e, "closeQuietly")
			}
		}
	}
}