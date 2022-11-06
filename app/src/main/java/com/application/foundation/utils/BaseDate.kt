package com.application.foundation.utils

import android.os.Parcel
import android.os.Parcelable
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.*

abstract class BaseDate : Parcelable {

	@Transient
	@JvmField
	val calendar: Calendar

	constructor(calendar: Calendar) {
		this.calendar = calendar
	}

	constructor(parcel: Parcel) : this(parcel.readString()!!)

	@Throws(IllegalArgumentException::class)
	constructor(date: String?) : this(date, false)

	@Throws(IllegalArgumentException::class)
	private constructor(date: String?, defaultTimeZone: Boolean) {
		calendar = TimeUtils.stringToCalendar(date, getFormat(), defaultTimeZone) ?:
				throw IllegalArgumentException("stringToCalendar returned null on $date ${getFormat().toPattern()} field")
	}


	override fun toString(): String {
		return toString(false)
	}

	fun toString(defaultTimeZone: Boolean): String {
		return toString(getFormat(), defaultTimeZone)
	}

	fun toString(dateFormat: DateFormat, defaultTimeZone: Boolean): String {
		return TimeUtils.calendarToString(calendar, dateFormat, defaultTimeZone)!!
	}


	protected abstract fun getFormat(): SimpleDateFormat


	override fun describeContents(): Int {
		return 0
	}

	override fun writeToParcel(dest: Parcel, flags: Int) {
		dest.writeString(toString())
	}
}