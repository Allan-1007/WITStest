package com.interviewfor.witstest.repository

import android.util.Log
import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.interviewfor.witstest.network.TwseApiService
import com.interviewfor.witstest.data.Resource
import com.interviewfor.witstest.data.StockBWIBBU
import com.interviewfor.witstest.data.StockDay
import com.interviewfor.witstest.data.StockDayAvg
import com.interviewfor.witstest.data.StockSummary
import com.interviewfor.witstest.data.room.MetadataDao
import com.interviewfor.witstest.data.room.MetadataEntity
import com.interviewfor.witstest.data.room.StockSummaryDao
import com.interviewfor.witstest.viewmodel.SortOrder
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


class StockRepository(
    private val apiService: TwseApiService,
    private val stockSummaryDao: StockSummaryDao,
    private val metadataDao: MetadataDao
) {
    companion object {
        private const val LAST_FETCH_TIME_KEY = "last_fetch_time"
        private const val CACHE_DURATION = 60 * 60 * 1000L // 1 小時
        private const val BATCH_SIZE = 1000
    }

    private val _stockSummaryData = MutableStateFlow<Resource<List<StockSummary>>>(Resource.Loading)
    val stockSummaryData: StateFlow<Resource<List<StockSummary>>> = _stockSummaryData.asStateFlow()

    init {
        fetchFromNetwork()
    }

    fun getStockSummariesPaged(sortOrder: SortOrder): PagingSource<Int, StockSummary> {
        return object : PagingSource<Int, StockSummary>() {
            override suspend fun load(params: LoadParams<Int>): LoadResult<Int, StockSummary> {
                return try {
                    val page = params.key ?: 0
                    val pageSize = params.loadSize
                    val offset = page * pageSize

                    val stocks = withContext(Dispatchers.IO) {
                        val stockEntities = if (sortOrder == SortOrder.ASCENDING) {
                            stockSummaryDao.getStocksPagedAsc(offset, pageSize)
                        } else {
                            stockSummaryDao.getStocksPagedDesc(offset, pageSize)
                        }
                        stockEntities.map { it.toDomain() }
                    }

                    LoadResult.Page(
                        data = stocks,
                        prevKey = if (page == 0) null else page - 1,
                        nextKey = if (stocks.isEmpty() || stocks.size < pageSize) null else page + 1
                    )
                } catch (e: Exception) {
                    Log.e("StockRepository", "Error loading page: ${e.message}")
                    LoadResult.Error(e)
                }
            }

            override fun getRefreshKey(state: PagingState<Int, StockSummary>): Int? {
                return state.anchorPosition?.let { anchorPosition ->
                    val anchorPage = state.closestPageToPosition(anchorPosition)
                    anchorPage?.prevKey?.plus(1) ?: anchorPage?.nextKey?.minus(1)
                }
            }
        }
    }
    private fun fetchFromNetwork(forceRefresh: Boolean = false) {
        CoroutineScope(Dispatchers.IO).launch {
            val lastFetchTime = metadataDao.getValue(LAST_FETCH_TIME_KEY) ?: 0L
            val currentTime = System.currentTimeMillis()
            val cachedStocks = stockSummaryDao.getAllStocks().first()

            if (!forceRefresh && cachedStocks.isNotEmpty() && (currentTime - lastFetchTime) < CACHE_DURATION) {
                Log.d("StockRepository", "Using cached data: ${cachedStocks.size} items")
                _stockSummaryData.value = Resource.Success(cachedStocks.map { it.toDomain() })
            } else {
                try {
                    val bwibbuData = apiService.getBWIBBUData()
                    val dayAvgData = apiService.getStockDayAvgData()
                    val dayData = apiService.getStockDayData()

                    val stockSummaries = mergeStockData(bwibbuData, dayAvgData, dayData)

                    val validSummaries = stockSummaries.filter { it.code != null }

                    stockSummaryDao.clearAll()
                    validSummaries.chunked(BATCH_SIZE).forEach { batch ->
                        stockSummaryDao.insertAll(batch.map { it.toEntity() })
                    }

                    metadataDao.insert(MetadataEntity(LAST_FETCH_TIME_KEY, currentTime))
                    _stockSummaryData.value = Resource.Success(validSummaries)
                } catch (e: Exception) {
                    Log.e("StockRepository", "Network error: ${e.message}")
                    if (cachedStocks.isNotEmpty()) {
                        _stockSummaryData.value = Resource.Success(cachedStocks.map { it.toDomain() })
                    } else {
                        _stockSummaryData.value = Resource.Error("Network error: ${e.message}")
                    }
                }
            }
        }
    }

    private fun mergeStockData(
        bwibbuData: List<StockBWIBBU>,
        dayAvgData: List<StockDayAvg>,
        dayData: List<StockDay>
    ): List<StockSummary> {
        val summaries = mutableListOf<StockSummary>()

        dayAvgData.forEach { dayAvg ->
            dayAvg.code?.let { code ->
                summaries.add(
                    StockSummary(
                        date = dayAvg.date,
                        code = code,
                        name = dayAvg.name,
                        closingPrice = dayAvg.closingPrice,
                        monthlyAveragePrice = dayAvg.monthlyAveragePrice,
                        peRatio = null,
                        dividendYield = null,
                        pbRatio = null,
                        tradeVolume = null,
                        tradeValue = null,
                        openingPrice = null,
                        highestPrice = null,
                        lowestPrice = null,
                        change = null,
                        transaction = null
                    )
                )
            }
        }

        dayData.forEach { day ->
            day.code?.let { code ->
                val index = summaries.indexOfFirst { it.code == code }
                if (index != -1) {
                    val summary = summaries[index]
                    summaries[index] = summary.copy(
                        date = day.date,
                        tradeVolume = day.tradeVolume,
                        tradeValue = day.tradeValue,
                        openingPrice = day.openingPrice,
                        highestPrice = day.highestPrice,
                        lowestPrice = day.lowestPrice,
                        change = day.change,
                        transaction = day.transaction
                    )
                } else {
                    summaries.add(
                        StockSummary(
                            date = day.date,
                            code = code,
                            name = day.name,
                            closingPrice = day.closingPrice,
                            monthlyAveragePrice = null,
                            peRatio = null,
                            dividendYield = null,
                            pbRatio = null,
                            tradeVolume = day.tradeVolume,
                            tradeValue = day.tradeValue,
                            openingPrice = day.openingPrice,
                            highestPrice = day.highestPrice,
                            lowestPrice = day.lowestPrice,
                            change = day.change,
                            transaction = day.transaction
                        )
                    )
                }
            }
        }

        bwibbuData.forEach { bwibbu ->
            bwibbu.code?.let { code ->
                val index = summaries.indexOfFirst { it.code == code }
                if (index != -1) {
                    val summary = summaries[index]
                    summaries[index] = summary.copy(
                        date = bwibbu.date,
                        name = bwibbu.name,
                        peRatio = bwibbu.peRatio,
                        dividendYield = bwibbu.dividendYield,
                        pbRatio = bwibbu.pbRatio
                    )
                } else {
                    summaries.add(
                        StockSummary(
                            date = bwibbu.date,
                            code = code,
                            name = bwibbu.name,
                            closingPrice = null,
                            monthlyAveragePrice = null,
                            peRatio = bwibbu.peRatio,
                            dividendYield = bwibbu.dividendYield,
                            pbRatio = bwibbu.pbRatio,
                            tradeVolume = null,
                            tradeValue = null,
                            openingPrice = null,
                            highestPrice = null,
                            lowestPrice = null,
                            change = null,
                            transaction = null
                        )
                    )
                }
            }
        }

        return summaries
    }
}