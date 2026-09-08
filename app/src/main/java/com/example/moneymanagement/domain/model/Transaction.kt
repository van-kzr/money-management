package com.example.moneymanagement.domain.model

import java.util.Date
import java.util.UUID

data class Transaction(
    val id: String = UUID.randomUUID().toString(),
    val description: String,
    val amount: Double,
    val type: TransactionType,
    val date: Date = Date(),
    val category: Category = Category.OTHER
)
