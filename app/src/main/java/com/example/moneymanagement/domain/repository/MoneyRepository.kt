package com.example.moneymanagement.domain.repository

import com.example.moneymanagement.domain.model.Saving
import com.example.moneymanagement.domain.model.Transaction
import kotlinx.coroutines.flow.Flow

interface MoneyRepository {
    fun getTransactions(): Flow<List<Transaction>>
    fun getSavings(): Flow<List<Saving>>
    suspend fun addTransaction(transaction: Transaction)
    suspend fun addSaving(saving: Saving)
    suspend fun updateSaving(saving: Saving)
}
