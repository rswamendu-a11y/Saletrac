package com.exclusive.saletrac.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.exclusive.saletrac.domain.SalesRepository
import com.exclusive.saletrac.util.CurrencyFormatter
import com.exclusive.saletrac.util.SalesAnalyticsEngine
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

class DashboardViewModel(
    private val repository: SalesRepository,
    private val analyticsEngine: SalesAnalyticsEngine
) : ViewModel() {

    private val ftdRange = analyticsEngine.getCurrentFTDRange()
    private val mtdRange = analyticsEngine.getCurrentMTDRange()
    private val lmtdRange = analyticsEngine.getLastMTDRange()

    private val mtdTransactions = repository.getTransactionsInRange(mtdRange.first, mtdRange.second)
    private val lmtdTransactions = repository.getTransactionsInRange(lmtdRange.first, lmtdRange.second)

    val dashboardItems: StateFlow<List<DashboardItem>> = combine(mtdTransactions, lmtdTransactions) { mtdList, lmtdList ->
        if (mtdList.isEmpty() && lmtdList.isEmpty()) {
            return@combine emptyList()
        }

        val items = mutableListOf<DashboardItem>()

        // 1. Summary Card Calculations
        val ftdList = mtdList.filter { it.timestamp in ftdRange.first..ftdRange.second }
        val ftdValue = ftdList.sumOf { it.price }
        val mtdValue = mtdList.sumOf { it.price }
        val lmtdValue = lmtdList.sumOf { it.price }

        val growth = analyticsEngine.calculateGrowth(mtdValue, lmtdValue)

        items.add(
            DashboardItem.Summary(
                ftdValue = CurrencyFormatter.formatINR(ftdValue),
                mtdValue = CurrencyFormatter.formatINR(mtdValue),
                growthPercent = growth
            )
        )

        // Only add charts if we have MTD data
        if (mtdList.isNotEmpty()) {
            // 2. Brand Share Card Calculations
            val brandMap = mtdList.groupBy { it.brand }
                .mapValues { entry -> entry.value.sumOf { it.price } }
            items.add(DashboardItem.BrandShare(brandMap))

            // 3. Price Segment Card Calculations (Using APP_LOGIC.md mappings directly from Phase 2 engine)
            // Desired exact order based on logic
            val segments = mtdList.groupBy { analyticsEngine.getPriceSegment(it.price) }
                .mapValues { entry -> entry.value.size }

            items.add(DashboardItem.PriceSegments(segments))
        }

        items
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
}
