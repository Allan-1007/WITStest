package com.interviewfor.witstest.data.room

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface StockSummaryDao {
    @Query("SELECT * FROM stock_summary")
    fun getAllStocks(): Flow<List<StockSummaryEntity>>

    @Query("SELECT * FROM stock_summary ORDER BY code ASC LIMIT :limit OFFSET :offset")
    suspend fun getStocksPagedAsc(offset: Int, limit: Int): List<StockSummaryEntity>

    @Query("SELECT * FROM stock_summary ORDER BY code DESC LIMIT :limit OFFSET :offset")
    suspend fun getStocksPagedDesc(offset: Int, limit: Int): List<StockSummaryEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(stocks: List<StockSummaryEntity>)

    @Query("DELETE FROM stock_summary")
    suspend fun clearAll()
}