package com.application.foundation.utils

import android.os.Parcel
import android.os.Parcelable
import com.squareup.moshi.FromJson
import com.squareup.moshi.ToJson
import java.text.SimpleDateFormat
import java.util.*

class DateYYYYMMDDTHHMMSS_SSSZ : BaseDate {

	constructor(calendar: Calendar) : super(calendar)

	@Throws(IllegalArgumentException::class)
	constructor(date: String?) : super(date)

	constructor(source: Parcel) : super(source)


	override fun getFormat(): SimpleDateFormat {
		return FORMAT
	}

	object MoshiAdapter {

		@FromJson
		fun fromJson(string: String) = DateYYYYMMDDTHHMMSS_SSSZ(string)

		@ToJson
		fun toJson(value: DateYYYYMMDDTHHMMSS_SSSZ) = value.toString()
	}

	companion object {
		private val FORMAT = TimeUtils.DATE_FORMAT_YYYYMMDDTHHMMSS_SSSZ

		@JvmField
		val CREATOR: Parcelable.Creator<DateYYYYMMDDTHHMMSS_SSSZ> = object : Parcelable.Creator<DateYYYYMMDDTHHMMSS_SSSZ> {

			override fun createFromParcel(source: Parcel): DateYYYYMMDDTHHMMSS_SSSZ {
				return DateYYYYMMDDTHHMMSS_SSSZ(source)
			}

			override fun newArray(size: Int): Array<DateYYYYMMDDTHHMMSS_SSSZ?> {
				return arrayOfNulls(size)
			}
		}
	}
}