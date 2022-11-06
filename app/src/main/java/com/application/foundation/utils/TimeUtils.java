package com.application.foundation.utils;

import androidx.annotation.Nullable;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Comparator;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

public final class TimeUtils {

	private static final String TAG = TimeUtils.class.getSimpleName();

	public static final int NUM_DAYS_IN_WEEK = 7;
	public static final int MONTHS_IN_YEAR = 12;
	public static final TimeZone ZERO_TIMEZONE = TimeZone.getTimeZone("GMT+0");

	public static final SimpleDateFormat DATE_FORMAT_YYYYMMDDTHHMMSS_SSSZ = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ", Locale.US);
	public static final SimpleDateFormat DATE_FORMAT_YYYYMMDDTHHMMSS = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.US);
	public static final SimpleDateFormat DATE_FORMAT_YYYYMMDD = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
	public static final SimpleDateFormat DATE_FORMAT_DDMMYYYY = new SimpleDateFormat("dd-MM-yyyy", Locale.US);
	public static final SimpleDateFormat DATE_FORMAT_DDMMYYYY_DOTTED = new SimpleDateFormat("dd.MM.yyyy", Locale.US);
	public static final SimpleDateFormat DATE_FORMAT_HHMM = new SimpleDateFormat("HH:mm", Locale.US);
	public static final SimpleDateFormat DATE_FORMAT_MMDD = new SimpleDateFormat("--MM-dd", Locale.US);
	public static SimpleDateFormat HUMAN_DATE_FORMAT_DAY_MONTH; // 24 August
	public static SimpleDateFormat HUMAN_DATE_FORMAT_DAY; // 24
	public static SimpleDateFormat HUMAN_DATE_FORMAT_MONTH; // August
	public static SimpleDateFormat HUMAN_DATE_FORMAT_DAY_MONTH_YEAR; // 24 August, 2018
	public static SimpleDateFormat HUMAN_DATE_FORMAT_DAY_MONTH_SHORT_YEAR; // 21 Nov 2017
	public static SimpleDateFormat HUMAN_DATE_FORMAT_DAY_MONTH_SHORT; // 21 Nov

	static {
		DATE_FORMAT_YYYYMMDDTHHMMSS_SSSZ.setLenient(false);
		DATE_FORMAT_YYYYMMDDTHHMMSS.setLenient(false);
		DATE_FORMAT_YYYYMMDD.setLenient(false);
		DATE_FORMAT_HHMM.setLenient(false);
		DATE_FORMAT_MMDD.setLenient(false);
	}

	private static final CalendarComparatorIgnoreTime calendarComparator = new CalendarComparatorIgnoreTime();

	public static final int MILLISECOND_IN_NANOS = 1000000;
	public static final int MINUTE_IN_MILLIS = 1000 * 60;
	public static final int HOUR_IN_MILLIS = MINUTE_IN_MILLIS * 60;
	public static final int DAY_IN_MILLIS = HOUR_IN_MILLIS * 24;
	public static final int DAY_IN_MINUTES = 24 * 60;

	public synchronized static void init() {
		HUMAN_DATE_FORMAT_DAY_MONTH = new SimpleDateFormat("d MMMM", Locale.getDefault());
		HUMAN_DATE_FORMAT_DAY = new SimpleDateFormat("d", Locale.getDefault());
		HUMAN_DATE_FORMAT_MONTH = new SimpleDateFormat("MMMM", Locale.getDefault());
		HUMAN_DATE_FORMAT_DAY_MONTH_YEAR = new SimpleDateFormat("d MMMM, yyyy", Locale.getDefault());
		HUMAN_DATE_FORMAT_DAY_MONTH_SHORT_YEAR = new SimpleDateFormat("d MMM yyyy", Locale.getDefault());
		HUMAN_DATE_FORMAT_DAY_MONTH_SHORT = new SimpleDateFormat("d MMM", Locale.getDefault());
	}

	public static class CalendarComparatorIgnoreTime implements Comparator<Calendar> {
		@Override
		public int compare(Calendar cal1, Calendar cal2) {
			int dayOfYear1 = cal1.get(Calendar.DAY_OF_YEAR);
			int dayOfYear2 = cal2.get(Calendar.DAY_OF_YEAR);
			int year1 = cal1.get(Calendar.YEAR);
			int year2 = cal2.get(Calendar.YEAR);
			if (year1 > year2) {
				return 1;
			}
			if (year1 < year2) {
				return -1;
			}
			if (dayOfYear1 > dayOfYear2) {
				return 1;
			}
			if (dayOfYear1 < dayOfYear2) {
				return -1;
			}
			return 0;
		}
	}

	public static CalendarComparatorIgnoreTime getCalendarComparatorIgnoreTime() {
		return calendarComparator;
	}

	public static boolean isDatesEqualsIgnoreTime(Calendar cal1, Calendar cal2) {
		return cal1 != null && cal2 != null && calendarComparator.compare(cal1, cal2) == 0;
	}

	public static boolean isDateAfterIgnoreTime(Calendar cal1, Calendar cal2) {
		return calendarComparator.compare(cal1, cal2) == 1;
	}

	public static boolean isDateBeforeIgnoreTime(Calendar cal1, Calendar cal2) {
		return calendarComparator.compare(cal1, cal2) == -1;
	}


	/**
	 * Convert string to calendar using date format
	 *
	 * @param string          Input string
	 * @param format          Date format
	 * @param defaultTimeZone If true then uses device time zone else GMT+0
	 * @return Calendar object if input string is correct, null otherwise
	 */
	public synchronized static @Nullable Calendar stringToCalendar(@Nullable String string, DateFormat format, boolean defaultTimeZone) {
		Calendar result = null;
		if (string != null) {
			try {
				if (format == TimeUtils.DATE_FORMAT_YYYYMMDDTHHMMSS_SSSZ) {
					string = string.replaceFirst("Z", "+0000");
				}

				TimeZone timezone = defaultTimeZone ? TimeZone.getDefault() : ZERO_TIMEZONE;
				format.setTimeZone(timezone);
				Date date = format.parse(string);
				result = Calendar.getInstance(timezone);
				result.setTime(date);
			} catch (ParseException e) {
				LogUtils.logError(TAG, e, "stringToCalendar", string);
			}
		}
		return result;
	}

	public synchronized static long stringToMillis(@Nullable String string, DateFormat format) {
		if (string != null) {
			try {
				Date date = format.parse(string);
				return date.getTime();
			} catch (ParseException e) {
				LogUtils.logError(TAG, e, "stringToMillis", string);
			}
		}
		return -1;
	}

	/**
	 * Convert calendar object to string using date format
	 *
	 * @param calendar        Calendar object
	 * @param format          Date format
	 * @param defaultTimeZone if true then uses device time zone else GMT+0
	 * @return String representation of calendar using date format
	 */
	public synchronized static @Nullable String calendarToString(@Nullable Calendar calendar, DateFormat format, boolean defaultTimeZone) {
		String result = null;
		if (calendar != null) {
			try {
				format.setTimeZone(defaultTimeZone ? TimeZone.getDefault() : ZERO_TIMEZONE);
				result = format.format(calendar.getTime()).trim();
			} catch (IllegalArgumentException e) {
				LogUtils.logError(TAG, e, "calendarToString", calendar.toString(), Boolean.toString(defaultTimeZone));
			}
		}
		return result;
	}

	public synchronized static String calendarToStringWithoutTimeZone(@Nullable Calendar calendar, DateFormat format) {
		String result = null;
		if (calendar != null) {
			try {
				format.setTimeZone(calendar.getTimeZone());
				result = format.format(calendar.getTime()).trim();
			} catch (IllegalArgumentException e) {
				LogUtils.logError(TAG, e, "calendarToStringWithoutTimeZone", calendar.toString());
			}
		}
		return result;
	}

	public synchronized static String calendarToStringWithoutTimeZone(@Nullable Calendar calendar) {
		return calendarToStringWithoutTimeZone(calendar, DATE_FORMAT_YYYYMMDDTHHMMSS);
	}




	public synchronized static Calendar stringToCalendarWithoutTimeZone(@Nullable String string, DateFormat format) {
		return stringToCalendar(string, format, false);
	}


	public synchronized static Calendar stringToCalendarWithoutTimeZone(@Nullable String string) {
		return stringToCalendar(string, DATE_FORMAT_YYYYMMDDTHHMMSS, false);
	}


	public static int daysBetweenDates(Calendar calEarly, Calendar calLate) {
		int calEarlyYear = calEarly.get(Calendar.YEAR);
		int calLateYear = calLate.get(Calendar.YEAR);

		int days;

		if (calEarlyYear == calLateYear) {
			days = calLate.get(Calendar.DAY_OF_YEAR) - calEarly.get(Calendar.DAY_OF_YEAR);
		} else {
			days = calEarly.getActualMaximum(Calendar.DAY_OF_YEAR) - calEarly.get(Calendar.DAY_OF_YEAR);

			Calendar tmp = Calendar.getInstance();
			tmp.set(0, 0, 0);

			for (int year = calEarlyYear + 1; year < calLateYear; year++) {
				tmp.set(Calendar.YEAR, year);
				days += tmp.getActualMaximum(Calendar.DAY_OF_YEAR);
			}

			days += calLate.get(Calendar.DAY_OF_YEAR);
		}

		return days;
	}

	/**
	 * @return -1 if calLate before calEarly
	 */
	public static int getYearsBetweenDates(Calendar calEarly, Calendar calLate) {
		if (TimeUtils.isDateBeforeIgnoreTime(calLate, calEarly)) {
			return -1;
		} else {
			int years = calLate.get(Calendar.YEAR) - calEarly.get(Calendar.YEAR);
			if (calEarly.get(Calendar.DAY_OF_YEAR) > calLate.get(Calendar.DAY_OF_YEAR)) {
				years--;
			}
			return years;
		}
	}

	/**
	 * Remove milliseconds and formats timezone in ISO 8601 format "yyyy-MM-ddTHH:mm:ssZ".
	 * If seconds not provided then :00 added.
	 * Time zone will bo converted to RFC 822 (ex. +0400).
	 * If time zone is not provided then "+0000" will be added.
	 *
	 * @param string Date string
	 * @return Date string with time zone converted to RFC 822
	 */
	public static String fixFormat(String string) {
		int plusPos = string.lastIndexOf("+");
		int minusPos = string.lastIndexOf("-");
		int iso8601Length = 19;
		String result = string;
		if (string.length() >= iso8601Length || string.length() == iso8601Length - 3) {
			if (string.length() == iso8601Length - 3) {
				string = string + ":00";
			}
			result = string.substring(0, iso8601Length);
			String timeZone;
			if (plusPos >= iso8601Length) {
				timeZone = string.substring(plusPos);
			} else if (minusPos >= iso8601Length) {
				timeZone = string.substring(minusPos);
			} else {
				timeZone = "+0000";
			}
			int colon = timeZone.lastIndexOf(':');
			if (colon == timeZone.length() - 3) {
				timeZone = timeZone.substring(0, colon) + timeZone.substring(colon + 1);
			}
			result = result + timeZone;
		}
		return result;
	}

	public static int getWeekDayPos(Calendar calendar) {
		int weekDay = calendar.get(Calendar.DAY_OF_WEEK);
		switch (weekDay) {
			case Calendar.MONDAY:
				return 0;
			case Calendar.TUESDAY:
				return 1;
			case Calendar.WEDNESDAY:
				return 2;
			case Calendar.THURSDAY:
				return 3;
			case Calendar.FRIDAY:
				return 4;
			case Calendar.SATURDAY:
				return 5;
			case Calendar.SUNDAY:
				return 6;
		}
		return 0;
	}

	public static String getDay(Calendar calendar) {
		return String.valueOf(calendar.get(Calendar.DAY_OF_MONTH));
	}

	public static String getYear(Calendar calendar) {
		return Integer.toString(calendar.get(Calendar.YEAR));
	}

	/**
	 * @param minutes count in day
	 * @return 20:35, 00:30, 20:00
	 */
	public static String minutesInDayToString(int minutes) {
		int hours = minutes / 60;
		int minutes_ = minutes % 60;
		return (hours == 0 ? "00" : hours < 10 ? "0" + hours : hours) + ":" +
				(minutes_ == 0 ? "00" : minutes_ < 10 ? "0" + minutes_ : minutes_);
	}

	public static Calendar cloneCalendar(Calendar calendar) {
		Calendar c = Calendar.getInstance(calendar.getTimeZone());
		c.setTime(calendar.getTime());
		return c;
	}

	public static Calendar createCalendarTimezoneGMT0(int year, int month, int day) {
		Calendar c = Calendar.getInstance(ZERO_TIMEZONE);
		c.set(year, month, day, 0, 0, 0);
		return c;
	}


	public static Calendar createCalendarTimezoneGMT0(long millis) {
		Calendar c = Calendar.getInstance(ZERO_TIMEZONE);
		c.setTimeInMillis(millis);
		return c;
	}

	public static Calendar createCalendarTimezoneDefault(long millis) {
		Calendar c = Calendar.getInstance();
		c.setTimeInMillis(millis);
		return c;
	}


	public static Calendar createCalendarTimezoneDefault(int year, int month, int day) {
		Calendar c = Calendar.getInstance();
		c.set(year, month, day, 0, 0, 0);
		return c;
	}

	public static Calendar createCalendar(int year, int month, int day, TimeZone timezone) {
		Calendar c = Calendar.getInstance(timezone);
		c.set(year, month, day, 0, 0, 0);
		return c;
	}

	public static Calendar createCalendarTimezoneDefault(int year, int month, int day, int hour, int min, int sec) {
		Calendar c = Calendar.getInstance();
		c.set(year, month, day, hour, min, sec);
		return c;
	}


	public static Calendar createCalendar(int year, int month, int day, int hour, int min, int sec, TimeZone timezone) {
		Calendar c = Calendar.getInstance(timezone);
		c.set(year, month, day, hour, min, sec);
		return c;
	}

	public static String millisToString(long millis) {
		int days = (int) (millis / TimeUtils.DAY_IN_MILLIS);
		int hours = (int) (millis % TimeUtils.DAY_IN_MILLIS / TimeUtils.HOUR_IN_MILLIS);
		int minutes = (int) (millis % TimeUtils.HOUR_IN_MILLIS / TimeUtils.MINUTE_IN_MILLIS);
		int seconds = (int) (millis % TimeUtils.MINUTE_IN_MILLIS / 1000);

		StringBuilder sb = new StringBuilder(8);

		if (days > 0) {
			sb.append(days);
			sb.append(':');
		}


		if (hours < 10) {
			sb.append(0);
		}
		sb.append(hours);

		sb.append(':');


		appendMinutesAndSeconds(sb, minutes, seconds);

		return sb.toString();
	}

	public static String millisToStringWithoutDays(long millis) {

		int hours = (int) (millis / TimeUtils.HOUR_IN_MILLIS);
		int minutes = (int) (millis % TimeUtils.HOUR_IN_MILLIS / TimeUtils.MINUTE_IN_MILLIS);
		int seconds = (int) (millis % TimeUtils.MINUTE_IN_MILLIS / 1000);

		StringBuilder sb = new StringBuilder(8);

		if (hours < 10) {
			sb.append(0);
		}
		sb.append(hours);

		sb.append(':');

		appendMinutesAndSeconds(sb, minutes, seconds);

		return sb.toString();
	}

	public static String millisToStringWithoutHours(long millis) {

		int minutes = (int) (millis / TimeUtils.MINUTE_IN_MILLIS);
		int seconds = (int) (millis % TimeUtils.MINUTE_IN_MILLIS / 1000);

		StringBuilder sb = new StringBuilder(8);

		appendMinutesAndSeconds(sb, minutes, seconds);

		return sb.toString();
	}

	private static void appendMinutesAndSeconds(StringBuilder sb, int minutes, int seconds) {

		if (minutes < 10) {
			sb.append(0);
		}
		sb.append(minutes);

		sb.append(':');

		if (seconds < 10) {
			sb.append(0);
		}
		sb.append(seconds);
	}
}