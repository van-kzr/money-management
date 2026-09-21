package com.example.moneymanagement

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.room.Room
import com.example.moneymanagement.data.local.MoneyDatabase
import com.example.moneymanagement.presentation.viewmodel.MoneyViewModel
import com.example.moneymanagement.presentation.viewmodel.MoneyViewModelFactory
import com.example.moneymanagement.data.repository.MoneyRepositoryImpl
import com.example.moneymanagement.domain.usecase.*
import com.example.moneymanagement.presentation.dashboard.DashboardScreen

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        val splashScreen = installSplashScreen()
        super.onCreate(savedInstanceState)
        
        val db = Room.databaseBuilder(
            applicationContext,
            MoneyDatabase::class.java,
            MoneyDatabase.DATABASE_NAME
        ).fallbackToDestructiveMigration()
            .build()
        
        val repository = MoneyRepositoryImpl(db.dao)
        
        val useCases = MoneyUseCases(
            getTransactions = GetTransactionsUseCase(repository),
            getSavings = GetSavingsUseCase(repository),
            addTransaction = AddTransactionUseCase(repository),
            addSaving = AddSavingUseCase(repository),
            updateSaving = UpdateSavingUseCase(repository),
            exportTransactions = ExportTransactionsUseCase(),
            clearData = ClearDataUseCase(repository)
        )
        
        enableEdgeToEdge()
        setContent {
            val viewModel: MoneyViewModel = viewModel(
                factory = MoneyViewModelFactory(useCases)
            )
            
            splashScreen.setKeepOnScreenCondition {
                viewModel.isLoading.value
            }

            com.example.moneymanagement.ui.theme.MoneyManagementTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    DashboardScreen(
                        totalBalance = viewModel.totalBalance,
                        availableBalance = viewModel.availableBalance,
                        savingBalance = viewModel.savingBalance,
                        totalIncome = viewModel.totalIncome,
                        totalExpense = viewModel.totalExpense,
                        transactions = viewModel.transactions,
                        savings = viewModel.saving,
                        onAddTransaction = viewModel::addTransaction,
                        onAddSaving = viewModel::addSaving,
                        onUpdateSaving = viewModel::updateSavingAmount,
                        onExportClick = { uri ->
                            viewModel.exportToExcel(uri, applicationContext.contentResolver) { success ->
                                if (success) {
                                    Toast.makeText(this@MainActivity, "Data berhasil dieksport ke Excel", Toast.LENGTH_SHORT).show()
                                } else {
                                    Toast.makeText(this@MainActivity, "Gagal mengeksport data", Toast.LENGTH_SHORT).show()
                                }
                            }
                        },
                        onImportConfirmed = { parsedRows ->
                            viewModel.importParsedTransactions(parsedRows)
                            Toast.makeText(this@MainActivity, "${parsedRows.size} transaksi berhasil di-import", Toast.LENGTH_SHORT).show()
                        },
                        onClearData = {
                            viewModel.clearAllData()
                            Toast.makeText(this@MainActivity, "Seluruh data telah dibersihkan", Toast.LENGTH_SHORT).show()
                        }
                    )
                }
            }
        }
    }
}
