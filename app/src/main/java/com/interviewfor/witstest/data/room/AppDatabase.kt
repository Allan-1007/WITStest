package com.interviewfor.witstest.data.room

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [StockSummaryEntity::class, MetadataEntity::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun stockSummaryDao(): StockSummaryDao
    abstract fun metadataDao(): MetadataDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "twse_database"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}