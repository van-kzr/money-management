package com.example.moneymanagement.data.mapper

import com.example.moneymanagement.data.local.entity.SavingEntity
import com.example.moneymanagement.data.local.entity.TransactionEntity
import com.example.moneymanagement.domain.model.Saving
import com.example.moneymanagement.domain.model.Transaction

fun TransactionEntity.toDomain(): Transaction {
    return Transaction(
        id = id,
        description = description,
        amount = amount,
        type = type,
        date = date,
        category = category
    )
}

fun Transaction.toEntity(): TransactionEntity {
    return TransactionEntity(
        id = id,
        description = description,
        amount = amount,
        type = type,
        date = date,
        category = category
    )
}

fun SavingEntity.toDomain(): Saving {
    return Saving(
        id = id,
        name = name,
        amount = amount,
        targetAmount = targetAmount,
        type = type,
        date = date
    )
}

fun Saving.toEntity(): SavingEntity {
    return SavingEntity(
        id = id,
        name = name,
        amount = amount,
        targetAmount = targetAmount,
        type = type,
        date = date
    )
}
