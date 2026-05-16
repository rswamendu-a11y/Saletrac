package com.exclusive.saletrac.util

import java.text.NumberFormat
import java.util.Locale

object CurrencyFormatter {
    fun formatINR(amount: Double): String {
        val format = NumberFormat.getCurrencyInstance(Locale("en", "IN"))
        format.maximumFractionDigits = 0
        return format.format(amount)
    }
}
