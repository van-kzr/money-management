package com.example.moneymanagement.presentation.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
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
import com.example.moneymanagement.ui.theme.IncomeGreen
import com.example.moneymanagement.ui.theme.ExpenseRed
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Locale

@Composable
fun TransactionItem(transaction: Transaction, formatter: NumberFormat) {
    val timeFormatter = remember { SimpleDateFormat("HH:mm", Locale("in", "ID")) }
    
    val amountColor = when(transaction.type) {
        TransactionType.INCOME -> IncomeGreen
        TransactionType.EXPENSE -> ExpenseRed
        TransactionType.Saving -> MaterialTheme.colorScheme.tertiary
        TransactionType.WITHDRAW_SAVING -> ExpenseRed
    }

    val iconColor = if (transaction.type == TransactionType.INCOME || transaction.type == TransactionType.EXPENSE) {
        transaction.category.color
    } else {
        MaterialTheme.colorScheme.tertiary
    }

    val icon = if (transaction.type == TransactionType.INCOME || transaction.type == TransactionType.EXPENSE) {
        painterResource(id = transaction.category.logo)
    } else {
        painterResource(id = com.example.moneymanagement.R.drawable.other)
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .background(iconColor.copy(alpha = 0.1f)),
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
            Text(
                text = transaction.description, 
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Bold,
                maxLines = 1
            )
            Text(
                text = if (transaction.type == TransactionType.Saving) "Tabungan" else transaction.category.categoryName,
                style = MaterialTheme.typography.bodySmall,
                color = Color.Gray
            )
        }

        Column(horizontalAlignment = Alignment.End) {
            Text(
                text = (if (transaction.type == TransactionType.INCOME || transaction.type == TransactionType.Saving) "+ " else "- ") + 
                       formatter.format(transaction.amount).replace("Rp", "Rp "),
                color = amountColor,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = timeFormatter.format(transaction.date),
                style = MaterialTheme.typography.labelSmall,
                color = Color.Gray
            )
        }
    }
}
