package com.exclusive.saletrac.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.exclusive.saletrac.data.entity.Transaction
import kotlinx.coroutines.flow.Flow

@Dao
interface TransactionDao {
    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertTransaction(transaction: Transaction): Long

    @Query("SELECT * FROM transactions ORDER BY timestamp DESC")
    fun getAllTransactions(): Flow<List<Transaction>>

    @Query("SELECT * FROM transactions WHERE timestamp BETWEEN :startDate AND :endDate ORDER BY timestamp DESC")
    fun getTransactionsInRange(startDate: Long, endDate: Long): Flow<List<Transaction>>

    @Query("SELECT SUM(price) FROM transactions WHERE timestamp BETWEEN :startDate AND :endDate")
    fun getTotalValueForDateRange(startDate: Long, endDate: Long): Flow<Double?>

    @Query("SELECT COUNT(*) FROM transactions WHERE timestamp BETWEEN :startDate AND :endDate")
    fun getTotalVolumeForDateRange(startDate: Long, endDate: Long): Flow<Int>

    @Query("SELECT * FROM transactions WHERE brand = :brand ORDER BY timestamp DESC")
    fun getTransactionsByBrand(brand: String): Flow<List<Transaction>>


    @Query("SELECT EXISTS(SELECT 1 FROM transactions WHERE imei = :imei)")
    suspend fun isImeiExists(imei: String): Boolean
}
