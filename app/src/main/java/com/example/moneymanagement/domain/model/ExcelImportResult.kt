package com.example.moneymanagement.domain.model

import java.util.Date

data class ParsedTransactionRow(
    val rowIndex: Int,
    val description: String,
    val amount: Double,
    val type: TransactionType,
    val category: Category,
    val date: Date
)

data class RowValidationError(
    val rowIndex: Int,
    val errorMessage: String
)

data class ExcelImportState(
    val headers: List<String> = emptyList(),
    val columnMapping: Map<String, Int> = emptyMap(),
    val parsedTransactions: List<ParsedTransactionRow> = emptyList(),
    val validationErrors: List<RowValidationError> = emptyList(),
    val isFileLoaded: Boolean = false
)
