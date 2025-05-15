package com.interviewfor.witstest.network

import com.interviewfor.witstest.data.StockBWIBBU
import com.interviewfor.witstest.data.StockDay
import com.interviewfor.witstest.data.StockDayAvg
import retrofit2.http.GET

interface TwseApiService {
    @GET("/v1/exchangeReport/BWIBBU_ALL")
    suspend fun getBWIBBUData(): List<StockBWIBBU>

    @GET("/v1/exchangeReport/STOCK_DAY_AVG_ALL")
    suspend fun getStockDayAvgData(): List<StockDayAvg>

    @GET("/v1/exchangeReport/STOCK_DAY_ALL")
    suspend fun getStockDayData(): List<StockDay>
}