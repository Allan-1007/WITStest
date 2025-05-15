package com.interviewfor.witstest.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.interviewfor.witstest.data.Resource
import com.interviewfor.witstest.data.StockSummary
import com.interviewfor.witstest.repository.StockRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class SortOrder {
    ASCENDING,
    DESCENDING
}

class StockViewModel(
    private val repository: StockRepository
) : ViewModel() {

    private val _sortOrder = MutableStateFlow(SortOrder.ASCENDING)
    val sortOrder: StateFlow<SortOrder> = _sortOrder.asStateFlow()

    @OptIn(ExperimentalCoroutinesApi::class)
    val stockSummaries: Flow<PagingData<StockSummary>> = _sortOrder
        .flatMapLatest { order ->
            Pager(
                config = PagingConfig(
                    pageSize = 100,
                    initialLoadSize = 100,
                    prefetchDistance = 20
                ),
                pagingSourceFactory = { repository.getStockSummariesPaged(order) }
            ).flow
        }
        .cachedIn(viewModelScope)

    // 原始數據
    private val stockSummaryData: StateFlow<Resource<List<StockSummary>>> =
        repository.stockSummaryData

    // 排序後的數據
    val sortedStockSummaryData: StateFlow<Resource<List<StockSummary>>> =
        stockSummaryData.combine(_sortOrder) { resource, order ->
            when (resource) {
                is Resource.Success -> {
                    val sortedList = when (order) {
                        SortOrder.ASCENDING -> resource.data.sortedBy { it.code }
                        SortOrder.DESCENDING -> resource.data.sortedByDescending { it.code }
                    }
                    Resource.Success(sortedList)
                }

                else -> resource
            }
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = Resource.Loading
        )

    init {
        viewModelScope.launch {
            Log.d("StockViewModel", "ViewModel initialized")
        }
    }

    fun toggleSortOrder(newOrder: SortOrder) {
        _sortOrder.value = newOrder
    }
}

fun StockSummary.getChangeAsDouble(): Double? = change?.toDoubleOrNull()
fun StockSummary.getLowestPriceAsDouble(): Double? = lowestPrice?.toDoubleOrNull()
fun StockSummary.getMonthlyAveragePriceAsDouble(): Double? = monthlyAveragePrice?.toDoubleOrNull()
fun StockSummary.getClosingPriceAsDouble(): Double? = closingPrice?.toDoubleOrNull()
fun StockSummary.getPeRatioAsDouble(): Double? = peRatio?.toDoubleOrNull()
fun StockSummary.getDividendYieldAsDouble(): Double? = dividendYield?.toDoubleOrNull()
fun StockSummary.getPbRatioAsDouble(): Double? = pbRatio?.toDoubleOrNull()