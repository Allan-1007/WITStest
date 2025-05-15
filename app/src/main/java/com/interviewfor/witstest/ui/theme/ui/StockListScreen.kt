package com.interviewfor.witstest.ui.theme.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Sort
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.paging.LoadState
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.itemKey
import com.interviewfor.witstest.data.StockSummary
import com.interviewfor.witstest.viewmodel.SortOrder
import com.interviewfor.witstest.viewmodel.StockViewModel
import com.interviewfor.witstest.viewmodel.getChangeAsDouble
import com.interviewfor.witstest.viewmodel.getDividendYieldAsDouble
import com.interviewfor.witstest.viewmodel.getLowestPriceAsDouble
import com.interviewfor.witstest.viewmodel.getMonthlyAveragePriceAsDouble
import com.interviewfor.witstest.viewmodel.getPbRatioAsDouble
import com.interviewfor.witstest.viewmodel.getPeRatioAsDouble
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StockListScreen(viewModel: StockViewModel) {
    val stockSummaries = viewModel.stockSummaries.collectAsLazyPagingItems()
    val sortOrder by viewModel.sortOrder.collectAsStateWithLifecycle(initialValue = SortOrder.ASCENDING)
    val sheetState = rememberModalBottomSheetState()
    val scope = rememberCoroutineScope()
    var showBottomSheet by remember { mutableStateOf(false) }

    LaunchedEffect(sortOrder) {
        stockSummaries.refresh()
    }
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "股票清單依照 ${
                            if (sortOrder == SortOrder.ASCENDING) {
                                "升序"
                            } else {
                                "降序"
                            }
                        }"
                    )
                },
                actions = {
                    IconButton(onClick = {
                        scope.launch {
                            showBottomSheet = true
                            sheetState.show()
                        }
                    }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Sort,
                            contentDescription = "Sort Options"
                        )
                    }
                }
            )
        },
        content = { padding ->
            Surface(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                color = MaterialTheme.colorScheme.background
            ) {
                when (stockSummaries.loadState.refresh) {
                    is LoadState.Loading -> LoadingIndicator()
                    is LoadState.Error -> ErrorMessage(
                        message = (stockSummaries.loadState.refresh as LoadState.Error).error.message
                            ?: "發生錯誤",
                        onRetry = { stockSummaries.retry() }
                    )

                    else -> StockSummaryList(stocks = stockSummaries)
                }
            }
        }
    )

    if (showBottomSheet) {
        ModalBottomSheet(
            onDismissRequest = {
                showBottomSheet = false
            },
            sheetState = sheetState
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "依股票代號排序",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                Button(
                    onClick = {
                        viewModel.toggleSortOrder(SortOrder.DESCENDING)
                        scope.launch {
                            sheetState.hide()
                            showBottomSheet = false
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("依股票代號降序")
                }
                Button(
                    onClick = {
                        viewModel.toggleSortOrder(SortOrder.ASCENDING)
                        scope.launch {
                            sheetState.hide()
                            showBottomSheet = false
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("依股票代號升序")
                }
            }
        }
    }
}

@Composable
fun StockSummaryList(stocks: LazyPagingItems<StockSummary>) {
    val listState = rememberLazyListState()
    LazyColumn(
        state = listState,
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(
            count = stocks.itemCount,
            key = stocks.itemKey { it.code ?: it.hashCode().toString() }
        ) { index ->
            stocks[index]?.let { stock ->
                StockSummaryItem(stock = stock)
            }
        }
        // 顯示底部載入狀態
        when (stocks.loadState.append) {
            is LoadState.Loading -> {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }
            }

            is LoadState.Error -> {
                item {
                    ErrorMessage(
                        message = (stocks.loadState.append as LoadState.Error).error.message
                            ?: "載入更多失敗",
                        onRetry = { stocks.retry() }
                    )
                }
            }

            else -> {}
        }
    }
}

@Composable
fun StockSummaryItem(stock: StockSummary) {
    var showDialog by remember { mutableStateOf(false) }
    AnimatedVisibility(
        visible = true,
        enter = fadeIn(animationSpec = tween(1000)),
        exit = fadeOut(animationSpec = tween(1000))
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp)
                .clickable { showDialog = true },
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = "${stock.code}",
                    style = MaterialTheme.typography.titleSmall
                )
                Text(
                    text = "${stock.name}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(text = "開盤價 : ${stock.openingPrice ?: "N/A"}")
                        Text(text = "最高價 : ${stock.highestPrice ?: "N/A"}")
                        Row {
                            Text(text = "漲跌價差 : ")
                            Text(
                                text = stock.change ?: "N/A",
                                color = if ((stock.getChangeAsDouble()
                                        ?: 0.0) > 0.0
                                ) {
                                    Color.Red
                                } else {
                                    Color.Green
                                }
                            )
                        }
                    }
                    Column {
                        Row {
                            Text(text = "收盤價 : ")
                            Text(
                                text = stock.closingPrice ?: "N/A",
                                color = if ((stock.getLowestPriceAsDouble()
                                        ?: 0.0) > (stock.getMonthlyAveragePriceAsDouble()
                                        ?: 0.0)
                                ) {
                                    Color.Red
                                } else {
                                    Color.Green
                                }
                            )
                        }
                        Text(text = "最低價 : ${stock.lowestPrice ?: "N/A"}")
                        Text(text = "月平均價 : ${stock.monthlyAveragePrice ?: "N/A"}")
                    }
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(text = "成交筆數 : ")
                        Text(text = stock.transaction ?: "N/A")
                    }
                    Column {
                        Text(text = "成交股數 : ")
                        Text(text = stock.tradeVolume ?: "N/A")
                    }
                    Column {
                        Text(text = "成交金額 : ")
                        Text(text = stock.tradeValue ?: "N/A")
                    }
                }
            }
        }
    }
    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = { Text("${stock.code} - ${stock.name}") },
            text = {
                Column {
                    Text(
                        "本益比 : ${
                            stock.getPeRatioAsDouble()?.let { "%.2f".format(it) } ?: "N/A"
                        }")
                    Text(
                        "殖利率 : ${
                            stock.getDividendYieldAsDouble()?.let { "%.2f".format(it) } ?: "N/A"
                        }")
                    Text(
                        "股價淨值比 : ${
                            stock.getPbRatioAsDouble()?.let { "%.2f".format(it) } ?: "N/A"
                        }")
                }
            },
            confirmButton = {
                TextButton(onClick = { showDialog = false }) {
                    Text("Close")
                }
            },
            containerColor = Color(0xFFF5F5F5)
        )
    }
}

@Composable
fun LoadingIndicator() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator()
    }
}

@Composable
fun ErrorMessage(message: String?, onRetry: () -> Unit) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = message ?: "發生錯誤",
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodyLarge
            )
            Spacer(modifier = Modifier.height(8.dp))
            Button(onClick = onRetry) {
                Text("重試")
            }
        }
    }
}
