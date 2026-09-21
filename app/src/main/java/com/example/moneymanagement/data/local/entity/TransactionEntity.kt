package com.example.moneymanagement.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.moneymanagement.domain.model.Category
import com.example.moneymanagement.domain.model.TransactionType
import java.util.Date

@Entity(tableName = "transactions")
data class TransactionEntity(
    @PrimaryKey val id: String,
    val description: String,
    val amount: Double,
    val type: TransactionType,
    val date: Date,
    val category: Category
)
