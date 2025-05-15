package com.interviewfor.witstest.data

import com.google.gson.annotations.SerializedName
import com.interviewfor.witstest.data.room.StockSummaryEntity

data class StockSummary(
    @SerializedName("Date") val date: String?,
    @SerializedName("Code") val code: String?,
    @SerializedName("Name") val name: String?,
    @SerializedName("PEratio") val peRatio: String?,
    @SerializedName("DividendYield") val dividendYield: String?,
    @SerializedName("PBratio") val pbRatio: String?,
    @SerializedName("ClosingPrice") val closingPrice: String?,
    @SerializedName("MonthlyAveragePrice") val monthlyAveragePrice: String?,
    @SerializedName("TradeVolume") val tradeVolume: String?,
    @SerializedName("TradeValue") val tradeValue: String?,
    @SerializedName("OpeningPrice") val openingPrice: String?,
    @SerializedName("HighestPrice") val highestPrice: String?,
    @SerializedName("LowestPrice") val lowestPrice: String?,
    @SerializedName("Change") val change: String?,
    @SerializedName("Transaction") val transaction: String?
) {
    fun toEntity(): StockSummaryEntity = StockSummaryEntity(
        date = date,
        code = code!!,
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
