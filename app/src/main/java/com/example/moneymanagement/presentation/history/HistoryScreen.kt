package com.example.moneymanagement.presentation.history

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.moneymanagement.domain.model.Category
import com.example.moneymanagement.domain.model.Transaction
import com.example.moneymanagement.presentation.component.TransactionItem
import java.text.NumberFormat
import java.util.Calendar
import java.util.Date

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryScreen(
    transactions: List<Transaction>,
    formatter: NumberFormat
) {
    var selectedCategory by remember { mutableStateOf<Category?>(null) }
    var selectedMonth by remember { mutableStateOf<Int?>(null) }
    var selectedYear by remember { mutableStateOf<Int?>(null) }

    var categoryExpanded by remember { mutableStateOf(false) }
    var monthExpanded by remember { mutableStateOf(false) }
    var yearExpanded by remember { mutableStateOf(false) }

    val months = listOf(
        "Januari", "Februari", "Maret", "April", "Mei", "Juni",
        "Juli", "Agustus", "September", "Oktober", "November", "Desember"
    )
    val years = (2020..2030).toList()

    val filteredHistory = transactions.filter { transaction ->
        val cal = Calendar.getInstance().apply { time = transaction.date }
        val categoryMatch = selectedCategory == null || transaction.category == selectedCategory
        val monthMatch = selectedMonth == null || cal.get(Calendar.MONTH) == selectedMonth
        val yearMatch = selectedYear == null || cal.get(Calendar.YEAR) == selectedYear
        categoryMatch && monthMatch && yearMatch
    }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Box(modifier = Modifier.fillMaxWidth().padding(16.dp).height(50.dp), contentAlignment = Alignment.Center) {
            Text(text = "Riwayat Transaksi", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        }
        Spacer(modifier = Modifier.height(16.dp))

        // Filters
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            FilterButton(
                label = selectedCategory?.categoryName ?: "Kategori",
                expanded = categoryExpanded,
                onExpandChange = { categoryExpanded = it },
                modifier = Modifier.weight(1f)
            ) {
                DropdownMenuItem(text = { Text("Semua") }, onClick = { selectedCategory = null; categoryExpanded = false })
                Category.entries.forEach { category ->
                    DropdownMenuItem(text = { Text(category.categoryName) }, onClick = { selectedCategory = category; categoryExpanded = false })
                }
            }

            FilterButton(
                label = if (selectedMonth != null) months[selectedMonth!!] else "Bulan",
                expanded = monthExpanded,
                onExpandChange = { monthExpanded = it },
                modifier = Modifier.weight(1f)
            ) {
                DropdownMenuItem(text = { Text("Semua") }, onClick = { selectedMonth = null; monthExpanded = false })
                months.forEachIndexed { index, month ->
                    DropdownMenuItem(text = { Text(month) }, onClick = { selectedMonth = index; monthExpanded = false })
                }
            }

            FilterButton(
                label = selectedYear?.toString() ?: "Tahun",
                expanded = yearExpanded,
                onExpandChange = { yearExpanded = it },
                modifier = Modifier.weight(0.8f)
            ) {
                DropdownMenuItem(text = { Text("Semua") }, onClick = { selectedYear = null; yearExpanded = false })
                years.forEach { year ->
                    DropdownMenuItem(text = { Text(year.toString()) }, onClick = { selectedYear = year; yearExpanded = false })
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(10.dp),
            contentPadding = PaddingValues(bottom = 16.dp)
        ) {
            items(filteredHistory, key = { it.id }) { transaction ->
                TransactionItem(transaction, formatter)
            }
        }
    }
}

@Composable
private fun FilterButton(
    label: String,
    expanded: Boolean,
    onExpandChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    Box(modifier = modifier) {
        OutlinedButton(
            onClick = { onExpandChange(true) },
            modifier = Modifier.fillMaxWidth(),
            contentPadding = PaddingValues(horizontal = 4.dp)
        ) {
            Text(label, maxLines = 1)
        }
        DropdownMenu(expanded = expanded, onDismissRequest = { onExpandChange(false) }, content = content)
    }
}

@Preview(showBackground = true)
@Composable
fun HistoryScreenPreview() {
    val formatter = NumberFormat.getCurrencyInstance(java.util.Locale("in", "ID"))
    MaterialTheme {
        HistoryScreen(
            transactions = listOf(
                Transaction(
                    description = "Gaji Bulanan",
                    amount = 5000000.0,
                    type = com.example.moneymanagement.domain.model.TransactionType.INCOME,
                    category = Category.SALARY
                ),
                Transaction(
                    description = "Makan Siang",
                    amount = 50000.0,
                    type = com.example.moneymanagement.domain.model.TransactionType.EXPENSE,
                    category = Category.FOOD
                ),
                Transaction(
                    description = "Dana Darurat",
                    amount = 1000000.0,
                    type = com.example.moneymanagement.domain.model.TransactionType.Saving,
                    category = Category.OTHER
                )
            ),
            formatter = formatter
        )
    }
}

