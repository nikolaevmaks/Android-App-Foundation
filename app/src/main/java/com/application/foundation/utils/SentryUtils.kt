package com.application.foundation.utils

import androidx.annotation.AnyThread
import com.application.foundation.App.Companion.injector
import io.sentry.Sentry
import io.sentry.SentryEvent
import io.sentry.SentryLevel
import io.sentry.protocol.Message
import io.sentry.protocol.User
import kotlin.collections.HashMap

object SentryUtils {

	@JvmStatic
	@JvmOverloads
	@AnyThread
	fun log(target: String,
			level: SentryLevel,
			message: String? = null,
			tags: Map<String, String?>? = null,
			extras: Map<String, String?>? = null) {

		val event = SentryEvent()
		val messageType = Message()

		messageType.message = message ?: target // without message will be unlabeled event in Sentry
		event.message = messageType


		event.setTags(if (tags == null) HashMap(mapCapacity(1)) else HashMap(tags))
		event.setTag("target", target)


		if (extras != null) {
			val extrasMap: MutableMap<String, Any?> = HashMap(mapCapacity(extras.size))
			for ((key, value) in extras) {
				extrasMap[key] = value
			}
			event.setExtras(extrasMap)
		}

		setUserInfo(event)
		event.level = level

		Sentry.captureEvent(event)
	}

	private fun setUserInfo(event: SentryEvent) {

		val id = injector.profile.id
		val email = injector.profile.userEmail
		val userUUID = injector.userUUID


		val user = User()

		if (id != null) {
			user.id = id.toString()
		}
		if (email != null) {
			user.email = email
		}

		val map: MutableMap<String, String> = HashMap(mapCapacity(1))
		if (userUUID != null) {
			map["uuid"] = userUUID
		}

		user.others = map

		event.user = user
	}
}