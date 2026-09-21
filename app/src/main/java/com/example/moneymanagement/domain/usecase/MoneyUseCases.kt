package com.example.moneymanagement.domain.usecase

import com.example.moneymanagement.domain.model.Category
import com.example.moneymanagement.domain.model.Saving
import com.example.moneymanagement.domain.model.Transaction
import com.example.moneymanagement.domain.model.TransactionType
import com.example.moneymanagement.domain.repository.MoneyRepository
import kotlinx.coroutines.flow.Flow
import kotlin.math.abs

class GetTransactionsUseCase(private val repository: MoneyRepository) {
    operator fun invoke(): Flow<List<Transaction>> = repository.getTransactions()
}

class GetSavingsUseCase(private val repository: MoneyRepository) {
    operator fun invoke(): Flow<List<Saving>> = repository.getSavings()
}

class AddTransactionUseCase(private val repository: MoneyRepository) {
    suspend operator fun invoke(description: String, amount: Double, type: TransactionType, category: Category) {
        if (description.isBlank() || amount <= 0) return
        repository.addTransaction(Transaction(description = description, amount = amount, type = type, category = category))
    }

    suspend fun addTransactions(transactions: List<Transaction>) {
        repository.addTransactions(transactions)
    }
}

class AddSavingUseCase(private val repository: MoneyRepository) {
    suspend operator fun invoke(name: String, amount: Double, target: Double, type: TransactionType) {
        if (name.isBlank() || amount <= 0) return
        
        // Add to savings aggregate
        repository.addSaving(Saving(name = name, amount = amount, targetAmount = target, type = type))
        
        // Add to individual history
        repository.addTransaction(Transaction(description = name, amount = amount, type = TransactionType.Saving, category = Category.OTHER))
    }
}

class UpdateSavingUseCase(private val repository: MoneyRepository) {
    suspend operator fun invoke(saving: Saving, delta: Double) {
        val newAmount = saving.amount + delta
        if (newAmount < 0) return
        
        repository.updateSaving(saving.copy(amount = newAmount))
        
        repository.addTransaction(Transaction(
            description = saving.name,
            amount = abs(delta),
            type = if (delta >= 0) TransactionType.Saving else TransactionType.WITHDRAW_SAVING,
            category = Category.OTHER
        ))
    }
}

class ClearDataUseCase(private val repository: MoneyRepository) {
    suspend operator fun invoke() {
        repository.clearAllData()
    }
}

data class MoneyUseCases(
    val getTransactions: GetTransactionsUseCase,
    val getSavings: GetSavingsUseCase,
    val addTransaction: AddTransactionUseCase,
    val addSaving: AddSavingUseCase,
    val updateSaving: UpdateSavingUseCase,
    val exportTransactions: ExportTransactionsUseCase,
    val importTransactions: ImportTransactionsUseCase = ImportTransactionsUseCase(),
    val clearData: ClearDataUseCase
)
