package com.application.foundation.utils

import com.application.foundation.App.Companion.injector
import io.sentry.SentryLevel
import android.util.Log
import androidx.annotation.AnyThread
import com.application.foundation.Environment
import java.lang.Exception
import java.util.HashMap

object LogUtils {

	/**
	 * Write standard debug log message, does not write on production
	 *
	 * @param tag Tag
	 * @param text The text to log
	 */
	@AnyThread
	@JvmStatic
	fun logD(tag: String, text: String?) {
		if (Environment.IS_DEVELOPMENT || Environment.ENABLE_LOGGING) {
			try {
				Log.i(tag, text ?: "null message") //Log.d doesn't work on S21 ultra
			} catch (e: Exception) {
				// under JUnit
			}
		}
	}

	@AnyThread
	@JvmStatic
	fun logD(tag: String, text: Int) {
		if (Environment.IS_DEVELOPMENT || Environment.ENABLE_LOGGING) {
			try {
				Log.i(tag, text.toString()) //Log.d
			} catch (e: Exception) {
				// under JUnit
			}
		}
	}

	@AnyThread
	@JvmStatic
	fun logD(tag: String, text: Float) {
		if (Environment.IS_DEVELOPMENT || Environment.ENABLE_LOGGING) {
			try {
				Log.i(tag, text.toString()) //Log.d
			} catch (e: Exception) {
				// under JUnit
			}
		}
	}

	/**
	 * Write standard info log message
	 *
	 * @param tag Tag
	 * @param text The text to log
	 */
	@AnyThread
	@JvmStatic
	fun logI(tag: String, text: String?) {
		if (Environment.IS_DEVELOPMENT || Environment.ENABLE_LOGGING) {
			try {
				Log.i(tag, text ?: "null message")
			} catch (e: Exception) {
				// under JUnit
			}
		}
	}

	/**
	 * Write standard error log message
	 *
	 * @param tag  Tag
	 * @param text The text to log
	 */
	@AnyThread
	@JvmStatic
	fun logE(tag: String, text: String?) {
		Log.e(tag, text ?: "null message")
	}

	@AnyThread
	@JvmStatic
	fun logE(tag: String, t: Throwable?) {
		Log.e(tag, t?.toString() ?: "null message")
	}

	@AnyThread
	@JvmStatic
	fun log(target: String) {
		SentryUtils.log(target, SentryLevel.INFO)
	}

	@AnyThread
	@JvmStatic
	fun log(target: String, message: String?) {
		SentryUtils.log(target, SentryLevel.INFO, message)
	}

	@AnyThread
	@JvmStatic
	fun logError(target: String) {
		logError(target, null)
	}

	@AnyThread
	@JvmStatic
	fun logError(target: String, message: String?) {
		logError(target, message, null as String?)
	}

	@AnyThread
	@JvmStatic
	fun logError(target: String, message: String?, funcName: String?, vararg parameters: String?) {

		logE(target, message)

		val attributes = getAttributes(funcName, *parameters)

		//SentryUtils.log(target, SentryLevel.ERROR, message, attributes, null)

		injector.analytics.logError(target, message, attributes)
	}

	@AnyThread
	@JvmStatic
	fun logError(target: String, t: Throwable, funcName: String?, vararg parameters: String?) {

		logE(target, t.message)

		val attributes = getAttributes(funcName, *parameters)

		// Sentry logs "stack trace" by itself
//		Map<String, String> extras = new HashMap<>(1);
//		extras.put("stack trace", Log.getStackTraceString(t));
		SentryUtils.log(target, SentryLevel.ERROR, t.message, attributes, null) //extras

		injector.analytics.logError(target, t.message, attributes)
	}

	@AnyThread
	@JvmStatic
	fun logError(target: String, message: String?, attributes: Map<String, String?>?) {
		logE(target, message)

		SentryUtils.log(target, SentryLevel.ERROR, message, attributes, null)

		injector.analytics.logError(target, message, attributes)
	}

	private fun getAttributes(funcName: String?, vararg parameters: String?): Map<String, String?>? {

		var attributes: MutableMap<String, String?>? = null

		if (funcName != null) {
			attributes = HashMap(mapCapacity(1 + parameters.size))
			attributes["funcName"] = funcName
		}

		if (parameters.isNotEmpty()) {
			// parameters can't be null, https://dev.to/le0nidas/til-vararg-in-kotlin-is-never-nullable-3bie

			if (attributes == null) {
				attributes = HashMap(mapCapacity(parameters.size))
			}
			for (i in parameters.indices) {
				attributes["parameter$i"] = parameters[i]
			}
		}

		return attributes
	}
}