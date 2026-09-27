package com.example.moneymanagement.presentation.history

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.moneymanagement.domain.model.Category
import com.example.moneymanagement.domain.model.Transaction
import com.example.moneymanagement.domain.model.TransactionType
import com.example.moneymanagement.presentation.component.TransactionItem
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryScreen(
    transactions: List<Transaction>,
    formatter: NumberFormat,
    onTransactionClick: (Transaction) -> Unit = {}
) {
    var selectedType by remember { mutableStateOf<TransactionType?>(null) }
    var searchQuery by remember { mutableStateOf("") }
    
    val filteredTransactions = remember(transactions, selectedType, searchQuery) {
        transactions.filter {
            // Filter out saving transactions
            if (it.type == TransactionType.Saving || it.type == TransactionType.WITHDRAW_SAVING) return@filter false
            
            val typeMatch = selectedType == null || it.type == selectedType
            val searchMatch = searchQuery.isEmpty() || it.description.contains(searchQuery, ignoreCase = true)
            typeMatch && searchMatch
        }
    }

    val groupedTransactions = remember(filteredTransactions) {
        val dateFormat = SimpleDateFormat("dd MMM yyyy", Locale("in", "ID"))
        val today = Calendar.getInstance()
        val yesterday = Calendar.getInstance().apply { add(Calendar.DATE, -1) }
        
        filteredTransactions.groupBy {
            val cal = Calendar.getInstance().apply { time = it.date }
            when {
                isSameDay(cal, today) -> "Hari ini, ${dateFormat.format(it.date)}"
                isSameDay(cal, yesterday) -> "Kemarin, ${dateFormat.format(it.date)}"
                else -> dateFormat.format(it.date)
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Top Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "Transaksi",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            Row {
                IconButton(onClick = { }) {
                    Icon(Icons.Default.Search, contentDescription = null)
                }
                IconButton(onClick = { }) {
                    Icon(Icons.Default.FilterList, contentDescription = null)
                }
            }
        }

        // Tabs / Chips
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            TransactionChip(label = "Semua", selected = selectedType == null) { selectedType = null }
            TransactionChip(label = "Pemasukan", selected = selectedType == TransactionType.INCOME) { selectedType = TransactionType.INCOME }
            TransactionChip(label = "Pengeluaran", selected = selectedType == TransactionType.EXPENSE) { selectedType = TransactionType.EXPENSE }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Grouped List
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 16.dp)
        ) {
            groupedTransactions.forEach { (date, items) ->
                item {
                    Text(
                        text = date,
                        style = MaterialTheme.typography.labelMedium,
                        color = Color.Gray,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                }
                items(items) { transaction ->
                    Box(modifier = Modifier.clickable { onTransactionClick(transaction) }) {
                        TransactionItem(transaction, formatter)
                    }
                }
            }
        }
    }
}

@Composable
fun TransactionChip(label: String, selected: Boolean, onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        shape = MaterialTheme.shapes.extraLarge,
        color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface,
        border = if (selected) null else androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)),
        modifier = Modifier.height(40.dp)
    ) {
        Box(
            modifier = Modifier.padding(horizontal = 20.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = label,
                color = if (selected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.labelLarge
            )
        }
    }
}

private fun isSameDay(cal1: Calendar, cal2: Calendar): Boolean {
    return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
            cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR)
}

@Preview(showBackground = true)
@Composable
fun HistoryScreenPreview() {
    val formatter = NumberFormat.getCurrencyInstance(Locale("in", "ID"))
    com.example.moneymanagement.ui.theme.MoneyManagementTheme {
        HistoryScreen(
            transactions = listOf(
                Transaction(description = "Beli Kopi Kekinian", amount = 35000.0, type = TransactionType.EXPENSE, category = Category.FOOD, date = Date()),
                Transaction(description = "Freelance", amount = 600000.0, type = TransactionType.INCOME, category = Category.FREELANCE, date = Date()),
                Transaction(description = "Bayar Internet", amount = 275000.0, type = TransactionType.EXPENSE, category = Category.BILLS, date = Date(System.currentTimeMillis() - 86400000))
            ),
            formatter = formatter
        )
    }
}
