package com.example.moneymanagement.presentation.viewmodel

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.moneymanagement.domain.model.Category
import com.example.moneymanagement.domain.model.Saving
import com.example.moneymanagement.domain.model.Transaction
import com.example.moneymanagement.domain.model.TransactionType
import com.example.moneymanagement.domain.usecase.MoneyUseCases
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

class MoneyViewModel(
    private val useCases: MoneyUseCases
) : ViewModel() {

    private val _transactions = mutableStateOf<List<Transaction>>(emptyList())
    val transactions: List<Transaction> get() = _transactions.value

    private val _savings = mutableStateOf<List<Saving>>(emptyList())
    val saving: List<Saving> get() = _savings.value

    init {
        useCases.getTransactions().onEach { _transactions.value = it }.launchIn(viewModelScope)
        useCases.getSavings().onEach { _savings.value = it }.launchIn(viewModelScope)
    }

    val totalBalance: Double
        get() = totalIncome - totalExpense

    val availableBalance: Double
        get() = totalBalance - savingBalance

    val savingBalance: Double
        get() = _savings.value.sumOf { it.amount }

    val totalIncome: Double
        get() = _transactions.value.sumOf { if (it.type == TransactionType.INCOME) it.amount else 0.0 }

    val totalExpense: Double
        get() = _transactions.value.sumOf { if (it.type == TransactionType.EXPENSE) it.amount else 0.0 }

    fun addTransaction(description: String, amount: Double, type: TransactionType, category: Category) {
        viewModelScope.launch {
            useCases.addTransaction(description, amount, type, category)
        }
    }

    fun addSaving(name: String, amount: Double, type: TransactionType) {
        viewModelScope.launch {
            useCases.addSaving(name, amount, type)
        }
    }

    fun updateSavingAmount(savingId: String, delta: Double) {
        val savingItem = _savings.value.find { it.id == savingId } ?: return
        viewModelScope.launch {
            useCases.updateSaving(savingItem, delta)
        }
    }
}

class MoneyViewModelFactory(
    private val useCases: MoneyUseCases
) : androidx.lifecycle.ViewModelProvider.Factory {
    override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MoneyViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return MoneyViewModel(useCases) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
