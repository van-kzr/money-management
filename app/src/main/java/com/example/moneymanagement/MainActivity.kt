package com.example.moneymanagement

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.moneymanagement.presentation.viewmodel.MoneyViewModel
import com.example.moneymanagement.presentation.viewmodel.MoneyViewModelFactory
import com.example.moneymanagement.data.repository.MoneyRepositoryImpl
import com.example.moneymanagement.domain.usecase.*
import com.example.moneymanagement.presentation.dashboard.DashboardScreen

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            // Manual DI for simplicity in this refactor
            val repository = remember { MoneyRepositoryImpl() }
            val useCases = remember {
                MoneyUseCases(
                    getTransactions = GetTransactionsUseCase(repository),
                    getSavings = GetSavingsUseCase(repository),
                    addTransaction = AddTransactionUseCase(repository),
                    addSaving = AddSavingUseCase(repository),
                    updateSaving = UpdateSavingUseCase(repository)
                )
            }
            
            val viewModel: MoneyViewModel = viewModel(
                factory = MoneyViewModelFactory(useCases)
            )

            MaterialTheme {
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
                        onUpdateSaving = viewModel::updateSavingAmount
                    )
                }
            }
        }
    }
}
