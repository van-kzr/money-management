package com.example.moneymanagement.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.moneymanagement.data.local.entity.SavingEntity
import com.example.moneymanagement.data.local.entity.TransactionEntity

@Database(
    entities = [TransactionEntity::class, SavingEntity::class],
    version = 2,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class MoneyDatabase : RoomDatabase() {
    abstract val dao: MoneyDao

    companion object {
        const val DATABASE_NAME = "money_db"
    }
}
