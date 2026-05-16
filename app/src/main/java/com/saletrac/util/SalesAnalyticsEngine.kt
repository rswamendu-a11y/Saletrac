package com.saletrac.util

class SalesAnalyticsEngine {

    /**
     * Determines the price segment for a given price according to APP_LOGIC.md.
     */
    fun getPriceSegment(price: Double): String {
        return when {
            price < 0 -> "Unknown"
            price <= 10000.0 -> "0 - 10,000 (Entry)"
            price <= 15000.0 -> "10,000 - 15,000 (Budget)"
            price <= 20000.0 -> "15,000 - 20,000 (Lower Mid)"
            price <= 25000.0 -> "20,000 - 25,000"
            price <= 30000.0 -> "25,000 - 30,000"
            price <= 35000.0 -> "30,000 - 35,000"
            price <= 40000.0 -> "35,000 - 40,000"
            price <= 45000.0 -> "40,000 - 45,000"
            price <= 50000.0 -> "45,000 - 50,000"
            price <= 100000.0 -> "50,000 - 1,00,000 (Premium)"
            price <= 200000.0 -> "1,00,000 - 2,00,000 (Luxury)"
            else -> "2,00,000+ (Ultra-Luxury)"
        }
    }

    /**
     * Calculates the percentage growth between the current period and the last period.
     * Handles division by zero for cases where the previous period had no sales.
     */
    fun calculateGrowth(currentPeriod: Double, previousPeriod: Double): Double {
        if (previousPeriod == 0.0) {
            return if (currentPeriod > 0.0) 100.0 else 0.0
        }
        return ((currentPeriod - previousPeriod) / previousPeriod) * 100.0
    }


    /**
     * Calculates the start and end Unix timestamps for the current Month-To-Date (MTD).
     */
    fun getCurrentMTDRange(currentTimeMillis: Long = System.currentTimeMillis()): Pair<Long, Long> {
        val calendar = java.util.Calendar.getInstance()
        calendar.timeInMillis = currentTimeMillis

        // Start of current month
        calendar.set(java.util.Calendar.DAY_OF_MONTH, 1)
        calendar.set(java.util.Calendar.HOUR_OF_DAY, 0)
        calendar.set(java.util.Calendar.MINUTE, 0)
        calendar.set(java.util.Calendar.SECOND, 0)
        calendar.set(java.util.Calendar.MILLISECOND, 0)
        val startMtd = calendar.timeInMillis

        return Pair(startMtd, currentTimeMillis)
    }

    /**
     * Calculates the start and end Unix timestamps for the Last Month-To-Date (LMTD).
     */
    fun getLastMTDRange(currentTimeMillis: Long = System.currentTimeMillis()): Pair<Long, Long> {
        val calendar = java.util.Calendar.getInstance()
        calendar.timeInMillis = currentTimeMillis

        val currentDay = calendar.get(java.util.Calendar.DAY_OF_MONTH)

        // Go to previous month
        calendar.add(java.util.Calendar.MONTH, -1)

        // Ensure we don't exceed the max days of the previous month
        val maxDaysInPrevMonth = calendar.getActualMaximum(java.util.Calendar.DAY_OF_MONTH)
        val targetDay = if (currentDay > maxDaysInPrevMonth) maxDaysInPrevMonth else currentDay

        // Start of previous month
        calendar.set(java.util.Calendar.DAY_OF_MONTH, 1)
        calendar.set(java.util.Calendar.HOUR_OF_DAY, 0)
        calendar.set(java.util.Calendar.MINUTE, 0)
        calendar.set(java.util.Calendar.SECOND, 0)
        calendar.set(java.util.Calendar.MILLISECOND, 0)
        val startLmtd = calendar.timeInMillis

        // End of LMTD
        calendar.set(java.util.Calendar.DAY_OF_MONTH, targetDay)
        // Keep the exact time of day as the current time millis but in the previous month
        val currentCal = java.util.Calendar.getInstance()
        currentCal.timeInMillis = currentTimeMillis
        calendar.set(java.util.Calendar.HOUR_OF_DAY, currentCal.get(java.util.Calendar.HOUR_OF_DAY))
        calendar.set(java.util.Calendar.MINUTE, currentCal.get(java.util.Calendar.MINUTE))
        calendar.set(java.util.Calendar.SECOND, currentCal.get(java.util.Calendar.SECOND))
        calendar.set(java.util.Calendar.MILLISECOND, currentCal.get(java.util.Calendar.MILLISECOND))
        val endLmtd = calendar.timeInMillis

        return Pair(startLmtd, endLmtd)
    }
}
