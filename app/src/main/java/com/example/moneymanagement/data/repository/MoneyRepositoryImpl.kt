package com.example.moneymanagement.data.repository

import com.example.moneymanagement.domain.model.Saving
import com.example.moneymanagement.domain.model.Transaction
import com.example.moneymanagement.domain.repository.MoneyRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class MoneyRepositoryImpl : MoneyRepository {
    private val _transactions = MutableStateFlow<List<Transaction>>(emptyList())
    private val _savings = MutableStateFlow<List<Saving>>(emptyList())

    override fun getTransactions(): Flow<List<Transaction>> = _transactions.asStateFlow()
    override fun getSavings(): Flow<List<Saving>> = _savings.asStateFlow()

    override suspend fun addTransaction(transaction: Transaction) {
        _transactions.update { (listOf(transaction) + it) }
    }

    override suspend fun addSaving(saving: Saving) {
        _savings.update { currentSavings ->
            val index = currentSavings.indexOfFirst { it.name.equals(saving.name, ignoreCase = true) }
            if (index != -1) {
                currentSavings.mapIndexed { i, s ->
                    if (i == index) s.copy(amount = s.amount + saving.amount) else s
                }
            } else {
                listOf(saving) + currentSavings
            }
        }
    }

    override suspend fun updateSaving(saving: Saving) {
        _savings.update { currentSavings ->
            currentSavings.map { if (it.id == saving.id) saving else it }
        }
    }
}
