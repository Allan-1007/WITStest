package com.interviewfor.witstest

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.ViewModelProvider
import androidx.room.Room
import com.interviewfor.witstest.data.room.AppDatabase
import com.interviewfor.witstest.network.RetrofitClient
import com.interviewfor.witstest.repository.StockRepository
import com.interviewfor.witstest.ui.theme.WITStestTheme
import com.interviewfor.witstest.ui.theme.ui.StockListScreen
import com.interviewfor.witstest.viewmodel.StockViewModel
import com.interviewfor.witstest.viewmodel.StockViewModelFactory

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val apiService = RetrofitClient.apiService
        val database = Room.databaseBuilder(
            applicationContext,
            AppDatabase::class.java,
            "stock_database"
        ).build()
        val repository = StockRepository(apiService,database.stockSummaryDao(),database.metadataDao())
        val viewModel = ViewModelProvider(this, StockViewModelFactory(repository))[StockViewModel::class.java]
        setContent {
            WITStestTheme {
                StockListScreen(viewModel = viewModel)
            }
        }
    }
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!",
        modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    WITStestTheme {
        Greeting("Android")
    }
}