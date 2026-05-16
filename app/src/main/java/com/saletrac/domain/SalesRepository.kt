package com.saletrac.domain

import com.saletrac.data.dao.TransactionDao
import com.saletrac.data.entity.Transaction
import com.saletrac.util.SalesAnalyticsEngine
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class SalesRepository(
    private val transactionDao: TransactionDao,
    private val analyticsEngine: SalesAnalyticsEngine
) {

    suspend fun insertTransaction(transaction: Transaction): Long {
        // Here we could add extra validation (e.g. 15 digit IMEI check) before delegating to DAO
        require(transaction.imei.length == 15) { "IMEI must be exactly 15 digits" }
        return transactionDao.insertTransaction(transaction)
    }

    fun getAllTransactions(): Flow<List<Transaction>> = transactionDao.getAllTransactions()

    fun getTransactionsInRange(startDate: Long, endDate: Long): Flow<List<Transaction>> =
        transactionDao.getTransactionsInRange(startDate, endDate)

    fun getTotalValueForDateRange(startDate: Long, endDate: Long): Flow<Double> =
        transactionDao.getTotalValueForDateRange(startDate, endDate).map { it ?: 0.0 }

    fun getTotalVolumeForDateRange(startDate: Long, endDate: Long): Flow<Int> =
        transactionDao.getTotalVolumeForDateRange(startDate, endDate)

    fun getTransactionsByBrand(brand: String): Flow<List<Transaction>> =
        transactionDao.getTransactionsByBrand(brand)

    fun getSegmentForPrice(price: Double): String {
        return analyticsEngine.getPriceSegment(price)
    }

    fun calculateGrowth(currentPeriod: Double, previousPeriod: Double): Double {
        return analyticsEngine.calculateGrowth(currentPeriod, previousPeriod)
    }
}
