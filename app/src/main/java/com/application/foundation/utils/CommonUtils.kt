package com.application.foundation.utils

import android.annotation.SuppressLint
import android.app.Activity
import android.app.ActivityManager
import android.content.*
import android.content.pm.PackageManager
import android.content.res.ColorStateList
import android.graphics.Point
import android.media.AudioManager
import android.net.Uri
import android.os.*
import android.provider.Settings
import android.util.Base64
import android.util.TypedValue
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import com.application.foundation.App.Companion.injector
import com.application.foundation.R
import com.application.foundation.features.common.presenter.PresenterInterface
import com.application.foundation.features.common.presenter.StartActivityInterface
import com.squareup.moshi.JsonDataException
import kotlinx.coroutines.*
import java.io.IOException
import java.io.UnsupportedEncodingException
import java.lang.Exception
import java.lang.RuntimeException
import java.lang.StringBuilder
import java.lang.reflect.Type
import java.net.URLDecoder
import java.net.URLEncoder
import java.nio.charset.StandardCharsets
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException
import java.util.*

object CommonUtils {

	private val TAG = CommonUtils::class.java.simpleName

	private lateinit var handler: Handler

	init {
		try {
			handler = Handler(Looper.getMainLooper())
		} catch (e: RuntimeException) {
			// launched from unit test
		}
	}

	fun ensureMainThread(block: suspend CoroutineScope.() -> Unit): Job {
		return injector.applicationScope.launch(Dispatchers.Main.immediate) {
			block()
		}
	}

	fun launchDelayed(delay: Long, block: suspend CoroutineScope.() -> Unit): Job {
		return injector.applicationScope.launch {
			delay(delay)
			block()
		}
	}


	@JvmStatic
	val isOnMainThread: Boolean
		get() = Looper.myLooper() == Looper.getMainLooper()

	@JvmStatic
	fun postToMainThread(runnable: Runnable) {
		handler.post(runnable)
	}

	@JvmStatic
	fun postToMainThread(runnable: Runnable, delay: Long) {
		handler.postDelayed(runnable, delay)
	}

	@JvmStatic
	fun removeCallbacksOnMainThread(runnable: Runnable) {
		handler.removeCallbacks(runnable)
	}


	@JvmStatic
	@SuppressLint("HardwareIds")
	fun getDeviceId(context: Context): String {
		return Settings.Secure.getString(context.contentResolver, Settings.Secure.ANDROID_ID)
	}

	@JvmStatic
	val deviceModel: String
		get() = Build.MODEL

	@JvmStatic
	val osVersion: String
		get() = Build.VERSION.RELEASE

	@JvmStatic
	fun getPackageName(context: Context): String {
		return context.packageName
	}

	@JvmStatic
	fun getVersionName(context: Context): String {
		return try {
			context.packageManager.getPackageInfo(context.packageName, 0).versionName
		} catch (e: PackageManager.NameNotFoundException) {
			throw RuntimeException("$TAG getVersionName", e)
		}
	}

	@JvmStatic
	fun getVersionCode(context: Context): Int {
		return try {
			context.packageManager.getPackageInfo(context.packageName, 0).versionCode
		} catch (e: PackageManager.NameNotFoundException) {
			throw RuntimeException("$TAG + getVersionCode", e)
		}
	}

	@JvmStatic
	fun getClassNameSimple(clazz: Class<*>): String {
		return clazz.simpleName
	}

	@JvmStatic
	fun getClassNameFull(`object`: Any): String {
		return `object`.javaClass.name
	}



	@JvmStatic
	fun <T> concatenate(a: Array<T>, b: Array<T>): Array<T> {
		val aLen = a.size
		val bLen = b.size

		val c = java.lang.reflect.Array.newInstance(a.javaClass.componentType, aLen + bLen) as Array<T>
		System.arraycopy(a, 0, c, 0, aLen)
		System.arraycopy(b, 0, c, aLen, bLen)
		return c
	}


	@JvmStatic
	fun encodeUrl(url: String): String {
		return try {
			URLEncoder.encode(url, "UTF-8")
		} catch (e: UnsupportedEncodingException) {
			throw RuntimeException("$TAG encodeUrl", e)
		}
	}

	@JvmStatic
	fun decodeUrl(url: String): String {
		return try {
			URLDecoder.decode(url, "UTF-8")
		} catch (e: UnsupportedEncodingException) {
			throw RuntimeException("$TAG decodeUrl", e)
		}
	}



	@JvmStatic
	fun dpToPxPrecise(context: Context, dp: Int): Float {
		return dpToPxPrecise(context, dp.toFloat())
	}

	@JvmStatic
	fun dpToPxPrecise(context: Context, dp: Float): Float {
		return dp * context.resources.displayMetrics.density
	}

	/**
	 * Convert dp to px
	 *
	 * @param context The context
	 * @param dp      Number of dp
	 * @return Number of px
	 */
	@JvmStatic
	fun dpToPx(context: Context, dp: Int): Int {
		return dpToPx(context, dp.toFloat())
	}

	/**
	 * Convert dp to px
	 *
	 * @param context The context
	 * @param dp      Number of dp
	 * @return Number of px
	 */
	@JvmStatic
	fun dpToPx(context: Context, dp: Float): Int {
		// add 0.5 to round up
		return (dp * context.resources.displayMetrics.density + if (dp < 0) -0.5f else 0.5f).toInt()
	}

	/**
	 * Convert px to dp
	 *
	 * @param context The context
	 * @param px      Number of px
	 * @return Number of dp
	 */
	@JvmStatic
	fun pxToDp(context: Context, px: Int): Int {
		// add 0.5 to round up
		return (px / context.resources.displayMetrics.density + if (px < 0) -0.5f else 0.5f).toInt()
	}

	@JvmStatic
	fun getDisplayDensity(context: Context): Float {
		return context.resources.displayMetrics.density
	}

	/**
	 * Convert dp to px
	 *
	 * @param context The context
	 * @param sp      Number of sp
	 * @return Number of px
	 */
	@JvmStatic
	fun spToPx(context: Context, sp: Float): Int {
		// add 0.5 to round up
		return (sp * context.resources.displayMetrics.scaledDensity + if (sp < 0) -0.5 else 0.5).toInt()
	}



	@JvmStatic
	fun safeString(text: String?): String {
		return text ?: ""
	}

	@JvmStatic
	fun isStringEmpty(text: CharSequence?): Boolean {
		return text == null || text.isEmpty()
	}

	@JvmStatic
	fun isStringEmpty(text: String?): Boolean {
		return text == null || text.isEmpty()
	}

	@JvmStatic
	fun isListEmpty(list: List<*>?): Boolean {
		return list == null || list.isEmpty()
	}


	@JvmStatic
	val currentTimeMillis: Long
		get() = System.currentTimeMillis()

	@JvmStatic
	val currentTimeMillisForDuration: Long
		get() = SystemClock.elapsedRealtime()



	@JvmStatic
	fun showSoftKeyboard(edit: EditText?) {
		if (edit != null) {
			edit.requestFocus()
			setSoftKeyboardVisibility(edit, true)
		}
	}

	@JvmStatic
	fun postShowSoftKeyboard(edit: EditText) {
		edit.post { showSoftKeyboard(edit) }
	}

	@JvmStatic
	fun setSoftKeyboardVisibility(edit: EditText, visible: Boolean) {
		val imm = edit.context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
		if (visible) {
			imm.showSoftInput(edit, InputMethodManager.SHOW_IMPLICIT)
		} else {
			imm.hideSoftInputFromWindow(edit.windowToken, 0)
		}
	}

	@JvmStatic
	fun clearFocus(activity: Activity) {
		val view = activity.currentFocus
		view?.clearFocus()
	}

	@JvmStatic
	fun hideKeyboard(activity: Activity) {
		val view = activity.currentFocus
		if (view != null && view is EditText) {
			setSoftKeyboardVisibility(view, false)
		}
	}



	@JvmStatic
	fun getSha1Hash(text: String): String {
		return try {
			val digest = MessageDigest.getInstance("SHA-1")
			val result: ByteArray = digest.digest(text.toByteArray(StandardCharsets.UTF_8))

			StringBuilder().apply {
				for (b in result) {
					append(String.format("%02x", b))
				}
			}.toString()

		} catch (e: NoSuchAlgorithmException) {
			throw RuntimeException("$TAG getSha1Hash", e)
		}
	}

	@JvmStatic
	fun getMd5Hash(text: String): String {
		return try {
			val digest = MessageDigest.getInstance("MD5")
			val result: ByteArray = digest.digest(text.toByteArray(StandardCharsets.UTF_8))

			StringBuilder().apply {
				for (b in result) {
					append(String.format("%02x", b))
				}
			}.toString()

		} catch (e: NoSuchAlgorithmException) {
			throw RuntimeException("$TAG getMd5Hash", e)
		}
	}



	@JvmStatic
	fun startApplicationSettingsActivityForResult(activity: StartActivityInterface, requestCode: Int) {
		val intent = Intent(
			Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
			Uri.fromParts("package", activity.context.packageName, null)
		)
		activity.startActivityForResult(intent, requestCode)
	}

	@JvmStatic
	fun putStringIntoClipboard(context: Context, title: String?, text: CharSequence?) {
		val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
		clipboard.setPrimaryClip(ClipData.newPlainText(title, text))
	}

	@JvmStatic
	fun sleep(millis: Long) {
		try {
			Thread.sleep(millis)
		} catch (e: InterruptedException) {
			LogUtils.logError(TAG, e, "sleep", millis.toString())
		}
	}


	@JvmStatic
	fun getToolbarHeight(context: Context): Int {
		val tv = TypedValue()
		context.theme.resolveAttribute(R.attr.actionBarSize, tv, true)
		return context.resources.getDimensionPixelSize(tv.resourceId)
	}


	@JvmStatic
	fun generateUUID(): String {
		return UUID.randomUUID().toString()
	}




	@JvmStatic
	fun sendEmail(
		activity: StartActivityInterface,
		recipientEmail: String,
		subject: String?,
		body: String?
	) {
		try {
			activity.startActivity(getEmailIntent(recipientEmail, subject, body))
		} catch (e: ActivityNotFoundException) {
		}
	}

	@JvmStatic
	fun getEmailIntent(recipientEmail: String, subject: String?, body: String?): Intent {

		return Intent(Intent.ACTION_SENDTO).apply {
			data = Uri.parse("mailto:")
			putExtra(Intent.EXTRA_EMAIL, arrayOf(recipientEmail))
			putExtra(Intent.EXTRA_SUBJECT, subject)
			putExtra(Intent.EXTRA_TEXT, body)
		}
	}

	@JvmStatic
	fun shareText(
		activity: StartActivityInterface,
		chooserTitleRes: Int,
		text: String?,
		requestCode: Int?
	) {
		val intent = Intent(Intent.ACTION_SEND)
		intent.type = "text/plain"
		intent.putExtra(Intent.EXTRA_TEXT, text)
		try {
			if (requestCode == null) {
				activity.startActivity(Intent.createChooser(intent, activity.context.getString(chooserTitleRes)))
			} else {
				activity.startActivityForResult(Intent.createChooser(intent, activity.context.getString(chooserTitleRes)), requestCode)
			}
		} catch (e: ActivityNotFoundException) {
		}
	}


	@JvmStatic
	fun setLightStatusBarBackground(view: View, light: Boolean) {
		if (Build.VERSION.SDK_INT >= 23) {
			var flags = view.systemUiVisibility
			flags = if (light) {
				flags or View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
			} else {
				flags and View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR.inv()
			}
			view.systemUiVisibility = flags
		}
	}



	@JvmStatic
	fun getAttributesFromUri(uri: Uri): Map<String, String> {

		try {
			val queryParameters = uri.queryParameterNames
			return buildMap<String, String>(queryParameters.size) {
				queryParameters.forEach {
					put(it, uri.getQueryParameters(it)[0])
				}
			}
		} catch (e: Exception) {
			LogUtils.logError(TAG, e, "getAttributesFromUri", uri.toString())
		}

		return mapOf()
	}

	@JvmStatic
	fun isTopmostActivity(presenter: PresenterInterface): Boolean {
		return !presenter.checkClickStartActivity() &&
				(!presenter.getActivity().isFinishing() && presenter.isTopPresenter || presenter.isLastPresenterOrEmpty)
	}



	@JvmStatic
	fun getGlobalViewPosition(view: View, viewToStop: View, removePaddingFromStopView: Boolean): Point {
		var view = view

		val pos = Point()
		do {
			pos.x += view.left
			pos.y += view.top
			view = view.parent as View
		} while (view !== viewToStop)

		if (removePaddingFromStopView) {
			pos.x -= viewToStop.paddingLeft
			pos.y -= viewToStop.paddingTop
		}
		return pos
	}

	@JvmStatic
	fun convertToBase64(text: String): String {
		val data = text.toByteArray(StandardCharsets.UTF_8)
		return Base64.encodeToString(data, Base64.NO_WRAP)
	}


	@JvmStatic
	fun tintImageView(view: ImageView, color: Int) {
		view.imageTintList = ColorStateList.valueOf(color)
	}



	@JvmStatic
	fun isPhone(context: Context): Boolean {
		return getScreenType(context) == ScreenType.Phone
	}

	@JvmStatic
	fun getScreenType(context: Context): ScreenType {

		val config = context.resources.configuration
		if (config.smallestScreenWidthDp >= ScreenType.Tablet10.smallestWidth) {
			return ScreenType.Tablet10

		} else if (config.smallestScreenWidthDp >= ScreenType.Tablet7.smallestWidth) {
			return ScreenType.Tablet7
		}
		return ScreenType.Phone
	}

	enum class ScreenType(val smallestWidth: Int) {
		Tablet10(720),
		Tablet7(600),
		Phone(0)
	}



	@JvmStatic
	@SuppressLint("SetJavaScriptEnabled")
	fun prepareWebView(webView: WebView, zoomAllowed: Boolean, webClient: WebViewClient?) {

		webView.settings.javaScriptEnabled = true
		webView.settings.setSupportZoom(zoomAllowed)
		if (zoomAllowed) {
			webView.settings.builtInZoomControls = true
			webView.settings.displayZoomControls = false
		}
		if (webClient != null) {
			webView.webViewClient = webClient
		}
	}

	@JvmStatic
	fun prepareWebView(webView: WebView, zoomAllowed: Boolean) {
		prepareWebView(webView, zoomAllowed, null)
	}


	@JvmStatic
	// check https://developer.android.com/training/package-visibility/declaring, maybe not work
	fun checkResolveActivity(intent: Intent, context: Context): Boolean {
		return context.packageManager.resolveActivity(intent, PackageManager.MATCH_DEFAULT_ONLY) != null
	}



	@JvmStatic
	fun vibrateShortly() {
		val vibrator = injector.applicationContext.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator?

		if (vibrator != null && vibrator.hasVibrator() &&
			Build.VERSION.SDK_INT >= 26 && vibrator.hasAmplitudeControl()) {

			vibrator.vibrate(VibrationEffect.createOneShot(25, 10))
		}
	}

	@JvmStatic
	fun vibrateMedium() {
		val vibrator = injector.applicationContext.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator?

		if (vibrator != null && vibrator.hasVibrator() &&
			Build.VERSION.SDK_INT >= 26 && vibrator.hasAmplitudeControl()) {

			vibrator.vibrate(VibrationEffect.createOneShot(50, 20))
		}
	}

	@JvmStatic
	fun playSound() {
		val audioManager = injector.applicationContext.getSystemService(Context.AUDIO_SERVICE) as AudioManager?
		audioManager?.playSoundEffect(AudioManager.FX_KEY_CLICK, -1.0f)
	}



	// see https://stackoverflow.com/questions/4005933/how-do-i-tell-if-my-textview-has-been-ellipsized
	/**
	 * textView should be measured before
	 * toLowerCase because of textAllCaps. see subscription plate
	 */
	@JvmStatic
	fun isTextEllipsized(textView: TextView): Boolean {
		return textView.layout.text.toString().lowercase(Locale.getDefault()) !=
				textView.text.toString().lowercase(Locale.getDefault())
	}

	/**
	 * textView should be measured before
	 * need to use ellipsize for this method to work
	 */
	@JvmStatic
	fun setMaxLinesOnlyIfMoreLinesActually(textView: TextView, maxLinesCount: Int): Boolean {
		if (textView.maxLines != maxLinesCount &&
			(textView.layout.lineCount > maxLinesCount ||
					textView.layout.lineCount == textView.maxLines && maxLinesCount > textView.maxLines && isTextEllipsized(textView))) {

			textView.maxLines = maxLinesCount
			return true
		}
		return false
	}

	/**
	 * textView should be measured before
	 * need to use ellipsize for this method to work
	 */
	@JvmStatic
	fun setMaxLinesOnlyIfMoreLinesActually(textView: TextView, maxLinesCount: Int, measureRunnable: () -> Unit): Boolean {

		measureRunnable()

		if (textView.maxLines != maxLinesCount &&
			(textView.layout.lineCount > maxLinesCount ||
					textView.layout.lineCount == textView.maxLines && maxLinesCount > textView.maxLines && isTextEllipsized(textView))) {

			textView.maxLines = maxLinesCount
			measureRunnable()

			return true
		}
		return false
	}


	@JvmStatic
	fun isMainProcess(context: Context): Boolean {
		return getPackageName(context) == getProcessName(context)
	}

	// you can use this method to get current process name, you will get
	// name like "com.package.name"(main process name) or "com.package.name:remote"
	@JvmStatic
	fun getProcessName(context: Context): String? {
		val mypid = Process.myPid()

		val manager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
		val infos = manager.runningAppProcesses
		if (infos != null) {
			for (info in infos) {
				if (info.pid == mypid) {
					return info.processName
				}
			}
		}
		return null
	}


	@JvmStatic
	fun <T> fromJson(json: String?, type: Class<T>): T? {
		return if (json == null) {
			null

		} else try {
			injector.moshi.adapter(type).fromJson(json)

		} catch (e: JsonDataException) {
			LogUtils.logError(TAG, e, "fromJson", type.name, json)
			throw RuntimeException(e)

		} catch (e: IOException) {
			LogUtils.logError(TAG, e, "fromJson", type.name, json)
			throw RuntimeException(e)
		}
	}

	@JvmStatic
	fun <T> fromJson(json: String?, type: Type): T? {
		return if (json == null) {
			null

		} else try {
			injector.moshi.adapter<T>(type).fromJson(json)

		} catch (e: JsonDataException) {
			LogUtils.logError(TAG, e, "fromJson", type.toString(), json)
			throw RuntimeException(e)

		} catch (e: IOException) {
			LogUtils.logError(TAG, e, "fromJson", type.toString(), json)
			throw RuntimeException(e)
		}
	}



	@JvmStatic
	fun <T> toJson(body: T): String {
		return toJsonNullable(body)!!
	}

	@JvmStatic
	fun <T> toJsonNullable(body: T?): String? {
		return if (body == null) {
			null
		} else {
			val jsonAdapter = injector.moshi.adapter(body.javaClass as Class<T>)
			jsonAdapter.toJson(body)
		}
	}


	@JvmStatic
	fun getUrlWithoutHttp(url: String): String {
		// without http or https
		return if (url.startsWith("http://")) {
			url.substring("http://".length)

		} else if (url.startsWith("https://")) {
			url.substring("https://".length)

		} else {
			url
		}
	}
}