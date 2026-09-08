package com.example.moneymanagement.presentation.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.moneymanagement.domain.model.Transaction
import com.example.moneymanagement.domain.model.TransactionType
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Locale

@Composable
fun TransactionItem(transaction: Transaction, formatter: NumberFormat) {
    val dateFormatter = remember { SimpleDateFormat("dd MMM yyyy", Locale("in", "ID")) }
    
    val (label, amountColor, iconColor) = when(transaction.type) {
        TransactionType.INCOME -> Triple(transaction.category.categoryName, Color(0xFF4CAF50), transaction.category.color)
        TransactionType.EXPENSE -> Triple(transaction.category.categoryName, Color.Red, transaction.category.color)
        TransactionType.Saving -> Triple("[Tabungan] ${transaction.description}", MaterialTheme.colorScheme.tertiary, MaterialTheme.colorScheme.tertiary)
        TransactionType.WITHDRAW_SAVING -> Triple("[Ambil Tabungan] ${transaction.description}", Color.Red, MaterialTheme.colorScheme.tertiary)
    }

    val icon = if (transaction.type == TransactionType.INCOME || transaction.type == TransactionType.EXPENSE) {
        painterResource(id = transaction.category.logo)
    } else {
        painterResource(id = com.example.moneymanagement.R.drawable.other)
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(5.dp)
    ) {
        Row(
            modifier = Modifier.padding(12.dp).fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(iconColor.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = icon,
                    contentDescription = null,
                    tint = iconColor,
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(text = label, fontWeight = FontWeight.Bold, maxLines = 1)
                Text(
                    text = "${transaction.description} • ${dateFormatter.format(transaction.date)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray
                )
            }

            Text(
                text = (if (transaction.type == TransactionType.INCOME || transaction.type == TransactionType.Saving) "+" else "-") + formatter.format(transaction.amount),
                color = amountColor,
                fontWeight = FontWeight.Bold
            )
        }
    }
}
