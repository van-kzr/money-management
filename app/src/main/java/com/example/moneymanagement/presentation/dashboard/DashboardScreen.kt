package com.example.moneymanagement.presentation.dashboard

import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.activity.compose.LocalActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.moneymanagement.domain.model.Category
import com.example.moneymanagement.domain.model.Saving
import com.example.moneymanagement.domain.model.Transaction
import com.example.moneymanagement.domain.model.TransactionType
import com.example.moneymanagement.presentation.component.AddTransactionDialog
import com.example.moneymanagement.presentation.history.HistoryScreen
import com.example.moneymanagement.presentation.reports.ReportsScreen
import com.example.moneymanagement.presentation.savings.SavingsScreen
import com.example.moneymanagement.presentation.settings.SettingsScreen
import com.example.moneymanagement.ui.theme.IncomeGreen
import com.example.moneymanagement.ui.theme.ExpenseRed
import java.text.NumberFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
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
    onAddSaving: (String, Double, Double, TransactionType) -> Unit,
    onUpdateSaving: (String, Double) -> Unit,
    onExportClick: (Uri) -> Unit = {},
    onImportConfirmed: (List<com.example.moneymanagement.domain.model.ParsedTransactionRow>) -> Unit = {},
    onClearData: () -> Unit = {}
) {
    var showImportDialog by remember { mutableStateOf(false) }
    var showClearConfirmation by remember { mutableStateOf(false) }
    var selectedTab by remember { mutableIntStateOf(0) }
    var showAddTransactionScreen by remember { mutableStateOf(false) }
    var showAddSavingScreen by remember { mutableStateOf(false) }
    var addTransactionInitialType by remember { mutableStateOf(TransactionType.EXPENSE) }
    var showStatisticsScreen by remember { mutableStateOf(false) }
    var selectedTransaction by remember { mutableStateOf<Transaction?>(null) }
    var showExitConfirmation by remember { mutableStateOf(false) }

    var selectedMonth by remember { mutableIntStateOf(Calendar.getInstance().get(Calendar.MONTH)) }
    var selectedYear by remember { mutableIntStateOf(Calendar.getInstance().get(Calendar.YEAR)) }
    
    val currencyFormatter = remember { NumberFormat.getCurrencyInstance(Locale("in", "ID")) }
    
    val exportLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"),
        onResult = { uri ->
            uri?.let { onExportClick(it) }
        }
    )

    Scaffold(
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surface,
                tonalElevation = 8.dp
            ) {
                NavigationBarItem(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    icon = { Icon(Icons.Default.Home, contentDescription = null) },
                    label = { Text("Beranda") }
                )
                NavigationBarItem(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    icon = { Icon(Icons.AutoMirrored.Filled.ReceiptLong, contentDescription = null) },
                    label = { Text("Transaksi") }
                )
                NavigationBarItem(
                    selected = selectedTab == 2,
                    onClick = { selectedTab = 2 },
                    icon = { Icon(Icons.Default.Savings, contentDescription = null) },
                    label = { Text("Tabungan") }
                )
                
                // Spacing for FAB
                Spacer(modifier = Modifier.weight(0.1f))
                
                NavigationBarItem(
                    selected = selectedTab == 3,
                    onClick = { selectedTab = 3 },
                    icon = { Icon(Icons.Default.BarChart, contentDescription = null) },
                    label = { Text("Laporan") }
                )
                NavigationBarItem(
                    selected = selectedTab == 4,
                    onClick = { selectedTab = 4 },
                    icon = { Icon(Icons.Default.Settings, contentDescription = null) },
                    label = { Text("Pengaturan") }
                )
            }
        }
    ) { paddingValues ->
        Box(modifier = Modifier.padding(paddingValues).fillMaxSize().background(MaterialTheme.colorScheme.background)) {
            when (selectedTab) {
                0 -> HomeContent(
                    totalBalance = totalBalance,
                    availableBalance = availableBalance,
                    savingBalance = savingBalance,
                    transactions = transactions,
                    selectedMonth = selectedMonth,
                    selectedYear = selectedYear,
                    onMonthYearChange = { m, y -> 
                        selectedMonth = m
                        selectedYear = y
                    },
                    formatter = currencyFormatter,
                    onAddTransactionClick = { 
                        addTransactionInitialType = TransactionType.EXPENSE
                        showAddTransactionScreen = true 
                    },
                    onSeeAllClick = { selectedTab = 1 }
                )
                1 -> HistoryScreen(
                    transactions = transactions, 
                    formatter = currencyFormatter,
                    onTransactionClick = { selectedTransaction = it }
                )
                2 -> SavingsScreen(
                    savings = savings,
                    allTransactions = transactions,
                    formatter = currencyFormatter,
                    onAddSavingClick = {
                        showAddSavingScreen = true
                    },
                    onUpdateSaving = onUpdateSaving
                )
                3 -> ReportsScreen(
                    transactions = transactions, 
                    formatter = currencyFormatter,
                    onDetailClick = { showStatisticsScreen = true },
                    onSeeAllTransactions = { selectedTab = 1 }
                )
                4 -> SettingsScreen(
                    onImportClick = { showImportDialog = true },
                    onExportClick = { exportLauncher.launch("Laporan_Keuangan_${System.currentTimeMillis()}.xlsx") },
                    onClearClick = { showClearConfirmation = true }
                )
            }
        }
    }

    // Handle system back button for exit confirmation
    if (!showStatisticsScreen && selectedTransaction == null && !showAddTransactionScreen && !showAddSavingScreen) {
        BackHandler {
            showExitConfirmation = true
        }
    }

    if (showStatisticsScreen) {
        BackHandler { showStatisticsScreen = false }
        com.example.moneymanagement.presentation.reports.StatisticsScreen(
            onBack = { showStatisticsScreen = false },
            transactions = transactions,
            formatter = currencyFormatter
        )
    }

    if (selectedTransaction != null) {
        BackHandler { selectedTransaction = null }
        com.example.moneymanagement.presentation.history.TransactionDetailScreen(
            transaction = selectedTransaction!!,
            onBack = { selectedTransaction = null },
            formatter = currencyFormatter
        )
    }

    if (showAddTransactionScreen) {
        BackHandler { showAddTransactionScreen = false }
        com.example.moneymanagement.presentation.add_transaction.AddTransactionScreen(
            onDismiss = { showAddTransactionScreen = false },
            initialType = addTransactionInitialType,
            onConfirm = { d, a, t, c ->
                onAddTransaction(d, a, t, c)
                showAddTransactionScreen = false
            }
        )
    }

    if (showAddSavingScreen) {
        BackHandler { showAddSavingScreen = false }
        com.example.moneymanagement.presentation.savings.AddSavingScreen(
            onDismiss = { showAddSavingScreen = false },
            onConfirm = { n, a, tg, t ->
                onAddSaving(n, a, tg, t)
                showAddSavingScreen = false
            }
        )
    }

    if (showImportDialog) {
        com.example.moneymanagement.presentation.component.ImportExcelDialog(
            onDismiss = { showImportDialog = false },
            onImportConfirmed = onImportConfirmed
        )
    }

    if (showClearConfirmation) {
        AlertDialog(
            onDismissRequest = { showClearConfirmation = false },
            title = { Text("Konfirmasi Hapus Data") },
            text = { Text("Apakah Anda yakin ingin menghapus seluruh data transaksi dan tabungan? Tindakan ini tidak dapat dibatalkan.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        onClearData()
                        showClearConfirmation = false
                    },
                    colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Hapus Semua")
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearConfirmation = false }) {
                    Text("Batal")
                }
            }
        )
    }

    if (showExitConfirmation) {
        val activity = LocalActivity.current
        AlertDialog(
            onDismissRequest = { showExitConfirmation = false },
            title = { Text("Keluar Aplikasi") },
            text = { Text("Apakah Anda yakin ingin keluar dari aplikasi MoneyFlow?") },
            confirmButton = {
                TextButton(onClick = { activity?.finish() }) {
                    Text("Keluar")
                }
            },
            dismissButton = {
                TextButton(onClick = { showExitConfirmation = false }) {
                    Text("Batal")
                }
            }
        )
    }
}

@Composable
private fun HomeContent(
    totalBalance: Double,
    availableBalance: Double,
    savingBalance: Double,
    transactions: List<Transaction>,
    selectedMonth: Int,
    selectedYear: Int,
    onMonthYearChange: (Int, Int) -> Unit,
    formatter: NumberFormat,
    onAddTransactionClick: () -> Unit,
    onSeeAllClick: () -> Unit
) {
    val filteredTransactions = remember(transactions, selectedMonth, selectedYear) {
        transactions.filter {
            val cal = Calendar.getInstance().apply { time = it.date }
            cal.get(Calendar.MONTH) == selectedMonth && cal.get(Calendar.YEAR) == selectedYear
        }
    }

    val monthlyIncome = filteredTransactions.filter { it.type == TransactionType.INCOME }.sumOf { it.amount }
    val monthlyExpense = filteredTransactions.filter { it.type == TransactionType.EXPENSE }.sumOf { it.amount }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        HeaderSection()
        
        Column(modifier = Modifier.padding(horizontal = 16.dp)) {
            BalanceCard(totalBalance, availableBalance, savingBalance, formatter)
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Button(
                onClick = onAddTransactionClick,
                modifier = Modifier.fillMaxWidth().height(48.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
            ) {
                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Tambah Transaksi", style = MaterialTheme.typography.labelLarge)
            }

            Spacer(modifier = Modifier.height(16.dp))

            MonthYearSelector(
                selectedMonth = selectedMonth,
                selectedYear = selectedYear,
                onMonthYearChange = onMonthYearChange
            )

            Spacer(modifier = Modifier.height(24.dp))
            
            MonthlySummarySection(monthlyIncome, monthlyExpense, formatter)
            
            Spacer(modifier = Modifier.height(24.dp))
            
            ExpenseCategorySection(filteredTransactions, formatter, onSeeAllClick)
            
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun MonthYearSelector(
    selectedMonth: Int,
    selectedYear: Int,
    onMonthYearChange: (Int, Int) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    val months = listOf(
        "Januari", "Februari", "Maret", "April", "Mei", "Juni",
        "Juli", "Agustus", "September", "Oktober", "November", "Desember"
    )

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White, RoundedCornerShape(12.dp))
            .clickable { expanded = true }
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.CalendarToday, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = "Periode: ${months[selectedMonth]} $selectedYear",
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Bold
            )
        }
        Icon(Icons.Default.ArrowDropDown, contentDescription = null, tint = Color.Gray)

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier.fillMaxWidth(0.9f)
        ) {
            // Let's show current year and previous year
            val currentYear = Calendar.getInstance().get(Calendar.YEAR)
            for (year in currentYear downTo currentYear - 1) {
                Text(
                    text = year.toString(),
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary
                )
                months.forEachIndexed { index, month ->
                    DropdownMenuItem(
                        text = { Text(text = "$month $year") },
                        onClick = {
                            onMonthYearChange(index, year)
                            expanded = false
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun HeaderSection() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(180.dp)
            .background(
                color = MaterialTheme.colorScheme.primary,
                shape = RoundedCornerShape(bottomStart = 32.dp, bottomEnd = 32.dp)
            )
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(top = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.3f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.Eco, contentDescription = null, tint = Color.White, modifier = Modifier.size(32.dp))
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(
                    text = "Selamat datang di",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.White.copy(alpha = 0.8f)
                )
                Text(
                    text = "MoneyFlow",
                    style = MaterialTheme.typography.titleLarge,
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Kelola keuanganmu dengan mudah",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.White.copy(alpha = 0.7f)
                )
            }
            Spacer(modifier = Modifier.weight(1f))
            IconButton(onClick = { }) {
                Icon(Icons.Default.Notifications, contentDescription = null, tint = Color.White)
            }
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
    var isVisible by remember { mutableStateOf(true) }
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .offset(y = (-40).dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                Text(text = "Total Saldo", style = MaterialTheme.typography.labelMedium, color = Color.Gray)
                Icon(
                    imageVector = if (isVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp).clickable { isVisible = !isVisible },
                    tint = Color.Gray
                )
            }
            Text(
                text = if (isVisible) formatter.format(totalBalance) else "Rp ********",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            HorizontalDivider(color = Color.LightGray.copy(alpha = 0.5f))
            Spacer(modifier = Modifier.height(16.dp))
            
            Row(modifier = Modifier.fillMaxWidth()) {
                BalanceItem(
                    icon = Icons.Default.AccountBalanceWallet,
                    label = "Saldo Tersedia",
                    amount = availableBalance,
                    formatter = formatter,
                    isVisible = isVisible,
                    modifier = Modifier.weight(1f)
                )
                VerticalDivider(modifier = Modifier.height(40.dp).padding(horizontal = 16.dp))
                BalanceItem(
                    icon = Icons.Default.Savings,
                    label = "Tabungan",
                    amount = savingBalance,
                    formatter = formatter,
                    isVisible = isVisible,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun BalanceItem(
    icon: ImageVector,
    label: String,
    amount: Double,
    formatter: NumberFormat,
    isVisible: Boolean,
    modifier: Modifier = Modifier
) {
    Row(modifier = modifier, verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier.size(32.dp).background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(16.dp))
        }
        Spacer(modifier = Modifier.width(8.dp))
        Column {
            Text(text = label, style = MaterialTheme.typography.labelSmall, color = Color.Gray)
            Text(
                text = if (isVisible) formatter.format(amount) else "Rp ****",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun MonthlySummarySection(
    totalIncome: Double,
    totalExpense: Double,
    formatter: NumberFormat
) {
    Column {
        Text(text = "Ringkasan Bulan Ini", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            SummaryCard(
                title = "Pemasukan",
                amount = totalIncome,
                percentage = "+ 22%",
                color = IncomeGreen,
                icon = Icons.Default.ArrowUpward,
                formatter = formatter,
                modifier = Modifier.weight(1f)
            )
            SummaryCard(
                title = "Pengeluaran",
                amount = totalExpense,
                percentage = "+ 5%",
                color = ExpenseRed,
                icon = Icons.Default.ArrowDownward,
                formatter = formatter,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun SummaryCard(
    title: String,
    amount: Double,
    percentage: String,
    color: Color,
    icon: ImageVector,
    formatter: NumberFormat,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier.size(24.dp).background(color.copy(alpha = 0.1f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(14.dp))
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(text = title, style = MaterialTheme.typography.labelMedium, color = Color.Gray)
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = formatter.format(amount), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "$percentage dari Agustus",
                style = MaterialTheme.typography.labelSmall,
                color = color
            )
        }
    }
}

@Composable
private fun ExpenseCategorySection(
    transactions: List<Transaction>,
    formatter: NumberFormat,
    onSeeAllClick: () -> Unit
) {
    val expenseCategories = remember(transactions) {
        transactions.filter { it.type == TransactionType.EXPENSE }
            .groupBy { it.category }
            .mapValues { it.value.sumOf { t -> t.amount } }
            .toList()
            .sortedByDescending { it.second }
    }
    
    val totalExpense = expenseCategories.sumOf { it.second }

    Column {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Text(text = "Pengeluaran Bulan Ini", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Text(
                text = "Lihat Semua", 
                style = MaterialTheme.typography.labelMedium, 
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.clickable { onSeeAllClick() }
            )
        } 
        
        Spacer(modifier = Modifier.height(16.dp))
        
        expenseCategories.take(5).forEach { (category, amount) ->
            val percentage = if (totalExpense > 0) (amount / totalExpense).toFloat() else 0f
            val percentageText = (percentage * 100).toInt()
            
            Column(modifier = Modifier.padding(vertical = 8.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier.size(32.dp).background(category.color.copy(alpha = 0.1f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            painter = painterResource(id = category.logo),
                            contentDescription = null,
                            tint = category.color,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(text = category.categoryName, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.weight(1f))
                    Text(text = formatter.format(amount).replace("Rp", "Rp "), style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = "$percentageText%", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                }
                Spacer(modifier = Modifier.height(8.dp))
                LinearProgressIndicator(
                    progress = { percentage },
                    modifier = Modifier.fillMaxWidth().height(6.dp).clip(CircleShape),
                    color = category.color,
                    trackColor = Color.LightGray.copy(alpha = 0.2f),
                    strokeCap = StrokeCap.Round
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun DashboardPreview() {
    val transactions = listOf(
        Transaction(description = "Gaji Bulanan", amount = 7500000.0, type = TransactionType.INCOME, category = Category.SALARY),
        Transaction(description = "Belanja Bulanan", amount = 1200000.0, type = TransactionType.EXPENSE, category = Category.SHOPPING),
        Transaction(description = "Tagihan Listrik", amount = 450000.0, type = TransactionType.EXPENSE, category = Category.BILLS),
        Transaction(description = "Makan Malam", amount = 350000.0, type = TransactionType.EXPENSE, category = Category.FOOD),
        Transaction(description = "Bensin Motor", amount = 120000.0, type = TransactionType.EXPENSE, category = Category.TRANSPORT)
    )
    com.example.moneymanagement.ui.theme.MoneyManagementTheme {
        DashboardScreen(
            totalBalance = 12170000.0,
            availableBalance = 4170000.0,
            savingBalance = 8000000.0,
            totalIncome = 9500000.0,
            totalExpense = 1330000.0,
            transactions = transactions,
            savings = emptyList(),
            onAddTransaction = { _, _, _, _ -> },
            onAddSaving = { _, _, _, _ -> },
            onUpdateSaving = { _, _ -> }
        )
    }
}
