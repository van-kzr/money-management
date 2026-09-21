package com.example.moneymanagement.data.local

import androidx.room.*
import com.example.moneymanagement.data.local.entity.SavingEntity
import com.example.moneymanagement.data.local.entity.TransactionEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface MoneyDao {
    @Query("SELECT * FROM transactions ORDER BY date DESC")
    fun getTransactions(): Flow<List<TransactionEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransaction(transaction: TransactionEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransactions(transactions: List<TransactionEntity>)

    @Query("SELECT * FROM savings ORDER BY date DESC")
    fun getSavings(): Flow<List<SavingEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSaving(saving: SavingEntity)

    @Update
    suspend fun updateSaving(saving: SavingEntity)

    @Query("SELECT * FROM savings WHERE name = :name LIMIT 1")
    suspend fun getSavingByName(name: String): SavingEntity?

    @Query("DELETE FROM transactions")
    suspend fun clearTransactions()

    @Query("DELETE FROM savings")
    suspend fun clearSavings()
}
