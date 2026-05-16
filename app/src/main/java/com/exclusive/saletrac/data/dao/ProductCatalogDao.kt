package com.exclusive.saletrac.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.exclusive.saletrac.data.entity.ProductCatalog
import kotlinx.coroutines.flow.Flow

@Dao
interface ProductCatalogDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProduct(product: ProductCatalog): Long

    @Query("SELECT * FROM product_catalog ORDER BY brand, modelName")
    fun getAllProducts(): Flow<List<ProductCatalog>>
}
