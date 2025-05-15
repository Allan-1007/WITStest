package com.interviewfor.witstest.data.room

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.interviewfor.witstest.data.StockSummary

@Entity(tableName = "stock_summary")
data class StockSummaryEntity(
    @PrimaryKey
    val code: String,
    val date: String?,
    val name: String?,
    val peRatio: String?,
    val dividendYield: String?,
    val pbRatio: String?,
    val closingPrice: String?,
    val monthlyAveragePrice: String?,
    val tradeVolume: String?,
    val tradeValue: String?,
    val openingPrice: String?,
    val highestPrice: String?,
    val lowestPrice: String?,
    val change: String?,
    val transaction: String?
) {
    fun toDomain(): StockSummary = StockSummary(
        date = date,
        code = code,
        name = name,
        peRatio = peRatio,
        dividendYield = dividendYield,
        pbRatio = pbRatio,
        closingPrice = closingPrice,
        monthlyAveragePrice = monthlyAveragePrice,
        tradeVolume = tradeVolume,
        tradeValue = tradeValue,
        openingPrice = openingPrice,
        highestPrice = highestPrice,
        lowestPrice = lowestPrice,
        change = change,
        transaction = transaction
    )
}
