package com.example.moneymanagement.presentation.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Savings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.moneymanagement.domain.model.Category
import com.example.moneymanagement.domain.model.Saving
import com.example.moneymanagement.domain.model.Transaction
import com.example.moneymanagement.domain.model.TransactionType
import com.example.moneymanagement.presentation.component.AddTransactionDialog
import com.example.moneymanagement.presentation.component.SavingItem
import com.example.moneymanagement.presentation.component.TransactionItem
import com.example.moneymanagement.presentation.history.HistoryScreen
import com.example.moneymanagement.presentation.savings.SavingsScreen
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun DashboardScreen(
    totalBalance: Double,
    availableBalance: Double,
    savingBalance: Double,
    totalIncome: Double,
    totalExpense: Double,
    transactions: List<Transaction>,
    savings: List<Saving>,
    onAddTransaction: (String, Double, TransactionType, Category) -> Unit,
    onAddSaving: (String, Double, TransactionType) -> Unit,
    onUpdateSaving: (String, Double) -> Unit
) {
    var showDialog by remember { mutableStateOf(false) }
    var selectedTab by remember { mutableIntStateOf(1) }
    val currencyFormatter = remember { NumberFormat.getCurrencyInstance(Locale("in", "ID")) }
    val dateFormatter = remember { SimpleDateFormat("dd MMMM yyyy", Locale("in", "ID")) }

    Scaffold(
        topBar = {
            Box(modifier = Modifier.fillMaxWidth().padding(16.dp).height(50.dp), contentAlignment = Alignment.CenterEnd) {
                Text(text = dateFormatter.format(Date()), style = MaterialTheme.typography.bodyMedium)
            }
        },
        bottomBar = {
            NavigationBar {
                NavigationBarItem(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    icon = { Icon(Icons.Default.History, contentDescription = null) },
                    label = { Text("Histori") }
                )
                NavigationBarItem(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    icon = { Icon(Icons.Default.Home, contentDescription = null) },
                    label = { Text("Beranda") }
                )
                NavigationBarItem(
                    selected = selectedTab == 2,
                    onClick = { selectedTab = 2 },
                    icon = { Icon(Icons.Default.Savings, contentDescription = null) },
                    label = { Text("Tabungan") }
                )
            }
        }
    ) { paddingValues ->
        Box(modifier = Modifier.padding(paddingValues).fillMaxSize()) {
            when (selectedTab) {
                0 -> HistoryScreen(transactions = transactions, formatter = currencyFormatter)
                1 -> MainSummaryContent(
                    totalBalance = totalBalance,
                    availableBalance = availableBalance,
                    savingBalance = savingBalance,
                    totalIncome = totalIncome,
                    totalExpense = totalExpense,
                    latestTransactions = transactions.take(4),
                    latestSavings = savings.take(2),
                    formatter = currencyFormatter,
                    onAddClick = { showDialog = true }
                )
                2 -> SavingsScreen(savings = savings, formatter = currencyFormatter, onUpdateSaving = onUpdateSaving)
            }
        }
    }

    if (showDialog) {
        AddTransactionDialog(
            onDismiss = { showDialog = false },
            onConfirm = onAddTransaction,
            onConfirmSaving = onAddSaving
        )
    }
}

@Composable
private fun MainSummaryContent(
    totalBalance: Double,
    availableBalance: Double,
    savingBalance: Double,
    totalIncome: Double,
    totalExpense: Double,
    latestTransactions: List<Transaction>,
    latestSavings: List<Saving>,
    formatter: NumberFormat,
    onAddClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        BalanceCard(
            totalBalance = totalBalance,
            availableBalance = availableBalance,
            savingBalance = savingBalance,
            formatter = formatter
        )

        Spacer(modifier = Modifier.height(16.dp))

        IncomeExpenseSummary(
            totalIncome = totalIncome,
            totalExpense = totalExpense,
            formatter = formatter
        )

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = onAddClick,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(5.dp)
        ) {
            Icon(Icons.Default.Add, contentDescription = null)
            Spacer(modifier = Modifier.width(5.dp))
            Text("Tambah Transaksi / Tabungan")
        }

        Spacer(modifier = Modifier.height(24.dp))

        if (latestTransactions.isNotEmpty()) {
            SectionHeader("Transaksi Terbaru")
            latestTransactions.forEach { TransactionItem(it, formatter); Spacer(modifier = Modifier.height(8.dp)) }
        }

        if (latestSavings.isNotEmpty()) {
            SectionHeader("Tabungan Terbaru")
            latestSavings.forEach { SavingItem(it, formatter); Spacer(modifier = Modifier.height(8.dp)) }
        }
    }
}

@Composable
private fun BalanceCard(
    totalBalance: Double,
    availableBalance: Double,
    savingBalance: Double,
    formatter: NumberFormat
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(5.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("Total Saldo", style = MaterialTheme.typography.titleMedium)
            Text(formatter.format(totalBalance), style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                BalanceSubItem("Tersedia", formatter.format(availableBalance))
                BalanceSubItem("Tabungan", formatter.format(savingBalance))
            }
        }
    }
}

@Composable
private fun BalanceSubItem(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(label, style = MaterialTheme.typography.bodySmall)
        Text(value, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun IncomeExpenseSummary(
    totalIncome: Double,
    totalExpense: Double,
    formatter: NumberFormat
) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        SummaryMiniCard("Pemasukan", formatter.format(totalIncome), Color(0xFF4CAF50), Modifier.weight(1f))
        SummaryMiniCard("Pengeluaran", formatter.format(totalExpense), Color.Red, Modifier.weight(1f))
    }
}

@Composable
private fun SummaryMiniCard(label: String, value: String, valueColor: Color, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(5.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
    ) {
        Column(modifier = Modifier.padding(12.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(label, style = MaterialTheme.typography.bodySmall)
            Text(value, color = valueColor, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
        }
    }
}

@Composable
private fun SectionHeader(title: String) {
    Text(
        text = title,
        modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
        style = MaterialTheme.typography.titleLarge,
        fontWeight = FontWeight.Bold
    )
}

@Preview(showBackground = true)
@Composable
fun DashboardPreview() {
    MaterialTheme {
        DashboardScreen(
            totalBalance = 5000000.0,
            availableBalance = 4000000.0,
            savingBalance = 1000000.0,
            totalIncome = 6000000.0,
            totalExpense = 1000000.0,
            transactions = listOf(
                Transaction(
                    description = "Gaji Bulanan",
                    amount = 5000000.0,
                    type = TransactionType.INCOME,
                    category = Category.SALARY
                ),
                Transaction(
                    description = "Makan Siang",
                    amount = 50000.0,
                    type = TransactionType.EXPENSE,
                    category = Category.FOOD
                ),
                Transaction(
                    description = "Belanja Bulanan",
                    amount = 500000.0,
                    type = TransactionType.EXPENSE,
                    category = Category.SHOPPING
                ),
                Transaction(
                    description = "Bonus Freelance",
                    amount = 1000000.0,
                    type = TransactionType.INCOME,
                    category = Category.FREELANCE
                )
            ),
            savings = listOf(
                Saving(
                    name = "Dana Darurat",
                    amount = 800000.0,
                    type = TransactionType.Saving
                ),
                Saving(
                    name = "Liburan Akhir Tahun",
                    amount = 200000.0,
                    type = TransactionType.Saving
                )
            ),
            onAddTransaction = { _, _, _, _ -> },
            onAddSaving = { _, _, _ -> },
            onUpdateSaving = { _, _ -> }
        )
    }
}
