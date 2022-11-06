package com.application.foundation.features.common.model

import android.content.Context
import android.telephony.TelephonyManager
import com.application.foundation.App.Companion.injector
import com.google.i18n.phonenumbers.PhoneNumberUtil
import java.util.*

class DetectCountry : RequestBase<Any, Any>() {

	companion object {
		const val UK_COUNTRY_CODE = "GB"
		const val US_COUNTRY_CODE = "US"
	}

	private val telephonyManager = injector.applicationContext.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager


	// on PHONE_TYPE_CDMA is unreliable by doc
	val countryCodeBySim: String?
		get() {
			var countryCode = telephonyManager.simCountryIso
			if (countryCode.isNullOrEmpty() && telephonyManager.phoneType != TelephonyManager.PHONE_TYPE_CDMA) { // on PHONE_TYPE_CDMA is unreliable by doc
				countryCode = telephonyManager.networkCountryIso
			}
			return countryCode?.toUpperCase(Locale.ROOT)
		}

	val countryCodeByLocale: String?
		get() {
			val countryCode = Locale.getDefault().country
			return if (countryCode.isNullOrEmpty()) null else countryCode.toUpperCase(Locale.ROOT)
		}



	private fun getPhoneCodeByCountryCode(countryCode: String): Int? {
		val phoneCode = PhoneNumberUtil.getInstance().getCountryCodeForRegion(countryCode)
		return if (phoneCode == 0) null else phoneCode
	}

	private val phoneCodeBySim: Int?
		get() {
			val countryCode = countryCodeBySim
			return if (!countryCode.isNullOrEmpty()) {
				getPhoneCodeByCountryCode(countryCode)
			} else null
		}

	private val phoneCodeByLocale: Int?
		get() {
			val countryCode = countryCodeByLocale
			return if (!countryCode.isNullOrEmpty()) {
				getPhoneCodeByCountryCode(countryCode)
			} else null
		}
}