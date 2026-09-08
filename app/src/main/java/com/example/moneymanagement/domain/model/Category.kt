package com.example.moneymanagement.domain.model

import androidx.compose.ui.graphics.Color
import com.example.moneymanagement.R

enum class Category(
    val categoryName: String,
    val logo: Int,
    val color: Color,
    val type: TransactionType? = null
) {
    SALARY("Gaji", R.drawable.salary, Color(0xFF4CAF50), TransactionType.INCOME),
    BONUS("Bonus", R.drawable.other, Color(0xFFFFC107), TransactionType.INCOME),
    GIFT("Hadiah", R.drawable.other, Color(0xFFFF4081), TransactionType.INCOME),
    BUSINESS("Bisnis", R.drawable.other, Color(0xFF3F51B5), TransactionType.INCOME),
    FREELANCE("Freelance", R.drawable.other, Color(0xFF00BCD4), TransactionType.INCOME),

    FOOD("Makanan", R.drawable.food, Color(0xFFFF9800), TransactionType.EXPENSE),
    SHOPPING("Belanja", R.drawable.shopping, Color(0xFFE91E63), TransactionType.EXPENSE),
    TRANSPORT("Transportasi", R.drawable.transport, Color(0xFF2196F3), TransactionType.EXPENSE),
    BILLS("Tagihan", R.drawable.bill, Color(0xFF9C27B0), TransactionType.EXPENSE),
    HEALTHCARE("Kesehatan", R.drawable.healthcare, Color(0xFFF44336), TransactionType.EXPENSE),

    OTHER("Lainnya", R.drawable.other, Color.Gray, null)
}
