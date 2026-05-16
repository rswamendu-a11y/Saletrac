package com.exclusive.saletrac.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.exclusive.saletrac.data.dao.ProductCatalogDao
import com.exclusive.saletrac.data.dao.TransactionDao
import com.exclusive.saletrac.data.entity.ProductCatalog
import com.exclusive.saletrac.data.entity.Transaction

@Database(entities = [Transaction::class, ProductCatalog::class], version = 1, exportSchema = true)
abstract class AppDatabase : RoomDatabase() {

    abstract fun transactionDao(): TransactionDao
    abstract fun productCatalogDao(): ProductCatalogDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "saletrac_database"
                )
                // future-proof migrations will be added here
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
