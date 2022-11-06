package com.application.foundation.features.common.view.utils

import com.application.foundation.features.common.model.dto.Currency
import java.math.BigDecimal
import java.math.RoundingMode
import java.text.DecimalFormat
import java.util.*

object Formatter {

	private lateinit var HUMAN_PRICE_FORMAT: DecimalFormat
	private lateinit var HUMAN_DECIMAL_FORMAT: DecimalFormat
	private lateinit var HUMAN_INTEGER_NUMBER_FORMAT: DecimalFormat

	@Synchronized
	fun init() {
		HUMAN_PRICE_FORMAT = DecimalFormat("#,##0.00")
		HUMAN_PRICE_FORMAT.roundingMode = RoundingMode.HALF_UP

		HUMAN_DECIMAL_FORMAT = DecimalFormat("#,##0.##")
		HUMAN_DECIMAL_FORMAT.roundingMode = RoundingMode.HALF_UP

		HUMAN_INTEGER_NUMBER_FORMAT = DecimalFormat("#,##0")
	}

	private fun getPriceSymbol(currency: Currency): String {
		return currency.symbol ?: currency.code
	}

	@JvmStatic
	@Synchronized
	fun formatPrice(price: BigDecimal, currency: Currency): String {
		return if (currency.isSuffix) formatPriceStaff(price, currency) + " " + getPriceSymbol(currency) else
			getPriceSymbol(currency) + formatPriceStaff(price, currency)
	}

	@Synchronized
	private fun formatPriceStaff(price: BigDecimal, currency: Currency): String {
		if (HUMAN_PRICE_FORMAT.minimumFractionDigits != currency.exp) {
			HUMAN_PRICE_FORMAT.minimumFractionDigits = currency.exp
			HUMAN_PRICE_FORMAT.maximumFractionDigits = currency.exp
		}
		return HUMAN_PRICE_FORMAT.format(price)
	}

	@JvmStatic
	@Synchronized
	fun formatPriceWithoutFraction(price: BigDecimal, currency: Currency): String {
		return if (currency.isSuffix) formatPriceWithoutFraction(price) + " " + getPriceSymbol(currency) else
			getPriceSymbol(currency) + formatPriceWithoutFraction(price)
	}

	@Synchronized
	private fun formatPriceWithoutFraction(price: BigDecimal): String {
		return HUMAN_DECIMAL_FORMAT.format(price)
	}

	@Synchronized
	fun formatDecimal(value: BigDecimal): String {
		return HUMAN_DECIMAL_FORMAT.format(value)
	}

	@JvmStatic
	@Synchronized
	fun formatIntegerNumber(number: Int): String {
		return HUMAN_INTEGER_NUMBER_FORMAT.format(number.toLong())
	}

	@JvmStatic
	fun formatStringForMachine(format: String, vararg args: Any): String {
		return String.format(Locale.US, format, *args)
	}
}