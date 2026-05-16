package com.exclusive.saletrac.util

import org.junit.Assert.assertEquals
import org.junit.Test

class SalesAnalyticsEngineTest {
    @Test
    fun testPriceSegment() {
        val engine = SalesAnalyticsEngine()
        val segment = engine.getPriceSegment(27500.0)
        println("Segment for ₹27,500: $segment")
        assertEquals("25,000 - 30,000", segment)
    }
}
