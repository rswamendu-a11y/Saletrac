package com.exclusive.saletrac.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "product_catalog")
data class ProductCatalog(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val brand: String,
    val modelName: String,
    val standardPrice: Double
)
