package com.example.moneymanagement.data.repository

import com.example.moneymanagement.data.local.MoneyDao
import com.example.moneymanagement.data.mapper.toDomain
import com.example.moneymanagement.data.mapper.toEntity
import com.example.moneymanagement.domain.model.Saving
import com.example.moneymanagement.domain.model.Transaction
import com.example.moneymanagement.domain.repository.MoneyRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class MoneyRepositoryImpl(
    private val dao: MoneyDao
) : MoneyRepository {

    override fun getTransactions(): Flow<List<Transaction>> {
        return dao.getTransactions().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override fun getSavings(): Flow<List<Saving>> {
        return dao.getSavings().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override suspend fun addTransaction(transaction: Transaction) {
        dao.insertTransaction(transaction.toEntity())
    }

    override suspend fun addTransactions(transactions: List<Transaction>) {
        dao.insertTransactions(transactions.map { it.toEntity() })
    }

    override suspend fun addSaving(saving: Saving) {
        val existingSaving = dao.getSavingByName(saving.name)
        if (existingSaving != null) {
            dao.updateSaving(existingSaving.copy(amount = existingSaving.amount + saving.amount))
        } else {
            dao.insertSaving(saving.toEntity())
        }
    }

    override suspend fun updateSaving(saving: Saving) {
        dao.updateSaving(saving.toEntity())
    }

    override suspend fun clearAllData() {
        dao.clearTransactions()
        dao.clearSavings()
    }
}
