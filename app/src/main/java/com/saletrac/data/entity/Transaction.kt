package com.saletrac.data.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "transactions",
    indices = [Index(value = ["imei"], unique = true)]
)
data class Transaction(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val timestamp: Long,
    val brand: String,
    val model: String,
    val variant: String,
    val imei: String,
    val price: Double,
    val paymentMode: String, // Stored as JSON string for multiple modes
    val customerName: String,
    val customerPhone: String
)
