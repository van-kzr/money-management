package com.example.moneymanagement.domain.model

import java.util.Date
import java.util.UUID

data class Saving(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val amount: Double,
    val targetAmount: Double = 0.0,
    val type: TransactionType,
    val date: Date = Date(),
)
