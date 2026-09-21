package com.example.moneymanagement.data.local

import androidx.room.TypeConverter
import com.example.moneymanagement.domain.model.Category
import com.example.moneymanagement.domain.model.TransactionType
import java.util.Date

class Converters {
    @TypeConverter
    fun fromTimestamp(value: Long?): Date? {
        return value?.let { Date(it) }
    }

    @TypeConverter
    fun dateToTimestamp(date: Date?): Long? {
        return date?.time
    }

    @TypeConverter
    fun fromTransactionType(type: TransactionType): String {
        return type.name
    }

    @TypeConverter
    fun toTransactionType(name: String): TransactionType {
        return try {
            TransactionType.valueOf(name)
        } catch (e: IllegalArgumentException) {
            TransactionType.EXPENSE
        }
    }

    @TypeConverter
    fun fromCategory(category: Category): String {
        return category.name
    }

    @TypeConverter
    fun toCategory(name: String): Category {
        return try {
            Category.valueOf(name)
        } catch (e: IllegalArgumentException) {
            Category.OTHER
        }
    }
}
