package com.example.moneymanagement.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.moneymanagement.domain.model.TransactionType
import java.util.Date

@Entity(tableName = "savings")
data class SavingEntity(
    @PrimaryKey val id: String,
    val name: String,
    val amount: Double,
    val targetAmount: Double = 0.0,
    val type: TransactionType,
    val date: Date
)
