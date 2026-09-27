package com.example.moneymanagement.presentation.reports

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.moneymanagement.domain.model.Category
import com.example.moneymanagement.domain.model.Transaction
import com.example.moneymanagement.domain.model.TransactionType
import com.example.moneymanagement.presentation.component.NominalText
import com.example.moneymanagement.presentation.component.TransactionItem
import com.example.moneymanagement.ui.theme.IncomeGreen
import com.example.moneymanagement.ui.theme.ExpenseRed
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportsScreen(
    transactions: List<Transaction>,
    formatter: NumberFormat,
    onDetailClick: () -> Unit = {},
    onSeeAllTransactions: () -> Unit = {}
) {
    var selectedRange by remember { mutableStateOf("Bulan Ini") }
    var calendarState by remember { mutableStateOf(Calendar.getInstance()) }
    
    val reportTransactions = remember(transactions, selectedRange, calendarState) {
        val now = Calendar.getInstance()
        transactions.filter {
            val transCal = Calendar.getInstance().apply { time = it.date }
            when (selectedRange) {
                "Bulan Ini" -> {
                    transCal.get(Calendar.MONTH) == calendarState.get(Calendar.MONTH) && 
                    transCal.get(Calendar.YEAR) == calendarState.get(Calendar.YEAR)
                }
                "Bulan Lalu" -> {
                    val lastMonth = (now.clone() as Calendar).apply { add(Calendar.MONTH, -1) }
                    transCal.get(Calendar.MONTH) == lastMonth.get(Calendar.MONTH) && 
                    transCal.get(Calendar.YEAR) == lastMonth.get(Calendar.YEAR)
                }
                "3 Bulan" -> {
                    val threeMonthsAgo = (now.clone() as Calendar).apply { 
                        add(Calendar.MONTH, -2)
                        set(Calendar.DAY_OF_MONTH, 1)
                        set(Calendar.HOUR_OF_DAY, 0)
                    }
                    it.date.after(threeMonthsAgo.time) || isSameDay(transCal, threeMonthsAgo)
                }
                else -> true // Custom / All
            }
        }
    }

    val periodText = remember(selectedRange, calendarState) {
        val sdf = SimpleDateFormat("MMMM yyyy", Locale("in", "ID"))
        when (selectedRange) {
            "Bulan Ini" -> sdf.format(calendarState.time)
            "Bulan Lalu" -> {
                val lastMonth = (Calendar.getInstance()).apply { add(Calendar.MONTH, -1) }
                sdf.format(lastMonth.time)
            }
            "3 Bulan" -> {
                val now = Calendar.getInstance()
                val threeMonthsAgo = (now.clone() as Calendar).apply { add(Calendar.MONTH, -2) }
                "${threeMonthsAgo.get(Calendar.DAY_OF_MONTH)} ${sdf.format(threeMonthsAgo.time).split(" ")[0]} - ${sdf.format(now.time)}"
            }
            else -> "Semua Periode"
        }
    }

    val totalIncome = reportTransactions.filter { it.type == TransactionType.INCOME }.sumOf { it.amount }
    val totalExpense = reportTransactions.filter { it.type == TransactionType.EXPENSE }.sumOf { it.amount }
    val savings = reportTransactions.filter { it.type == TransactionType.Saving }.sumOf { it.amount }
    val netBalance = totalIncome - totalExpense

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
    ) {
        // Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(text = "Laporan Keuangan", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            IconButton(onClick = { }) {
                Icon(Icons.Default.Share, contentDescription = null)
            }
        }

        // Time range filter
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            listOf("Bulan Ini", "Bulan Lalu", "3 Bulan", "Custom").forEach { range ->
                TimeRangeChip(
                    label = range,
                    selected = selectedRange == range,
                    onClick = { 
                        selectedRange = range 
                        if (range == "Bulan Ini") calendarState = Calendar.getInstance()
                    },
                    modifier = Modifier.weight(1f)
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Month selector (only visible/active for specific ranges if needed)
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = { 
                val newCal = calendarState.clone() as Calendar
                newCal.add(Calendar.MONTH, -1)
                calendarState = newCal
                selectedRange = "Bulan Ini" // Switch to Bulan Ini mode if navigating
            }) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null, modifier = Modifier.size(20.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Text(text = periodText, modifier = Modifier.padding(horizontal = 24.dp), style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurface)
            IconButton(onClick = { 
                val newCal = calendarState.clone() as Calendar
                newCal.add(Calendar.MONTH, 1)
                calendarState = newCal
                selectedRange = "Bulan Ini"
            }) {
                Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, modifier = Modifier.size(20.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Summary Grid
        Column(modifier = Modifier.padding(horizontal = 16.dp)) {
            Text(text = "Ringkasan", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(16.dp))
            
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                ReportSummaryCard(title = "Pemasukan", amount = totalIncome, color = IncomeGreen, icon = Icons.Default.ArrowUpward, formatter = formatter, modifier = Modifier.weight(1f))
                ReportSummaryCard(title = "Pengeluaran", amount = totalExpense, color = ExpenseRed, icon = Icons.Default.ArrowDownward, formatter = formatter, modifier = Modifier.weight(1f))
            }
            Spacer(modifier = Modifier.height(16.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                ReportSummaryCard(title = "Selisih", amount = netBalance, color = Color(0xFF2196F3), icon = Icons.Default.VerticalAlignBottom, formatter = formatter, modifier = Modifier.weight(1f))
                ReportSummaryCard(title = "Ditabung", amount = savings, color = Color(0xFF9C27B0), icon = Icons.Default.Savings, formatter = formatter, modifier = Modifier.weight(1f))
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Chart Section (Bar Chart)
        Column(modifier = Modifier.padding(horizontal = 16.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text(text = "Ringkasan Mingguan", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(modifier = Modifier.size(8.dp).background(IncomeGreen, CircleShape))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(text = "Pemasukan", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                    Spacer(modifier = Modifier.width(12.dp))
                    Box(modifier = Modifier.size(8.dp).background(ExpenseRed, CircleShape))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(text = "Pengeluaran", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
            
            BarChart(reportTransactions)
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Donut Chart Section
        Column(modifier = Modifier.padding(horizontal = 16.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text(text = "Pengeluaran Berdasarkan Kategori", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Text(
                    text = "Lihat detail", 
                    style = MaterialTheme.typography.labelMedium, 
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.clickable { onDetailClick() }
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
            
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                    val expCats = remember(reportTransactions) {
                        reportTransactions.filter { it.type == TransactionType.EXPENSE }
                            .groupBy { it.category }
                            .mapValues { it.value.sumOf { t -> t.amount } }
                            .toList()
                            .sortedByDescending { it.second }
                    }
                    val totalExp = expCats.sumOf { it.second }
                    
                    Box(modifier = Modifier.size(140.dp), contentAlignment = Alignment.Center) {
                        DonutChart(reportTransactions)
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(text = "Total", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                            NominalText(
                                amountText = formatter.format(totalExp).replace("Rp", "Rp "),
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(24.dp))
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        expCats.take(5).forEach { (cat, amt) ->
                            val perc = if (totalExp > 0) (amt / totalExp * 100).toInt() else 0
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(modifier = Modifier.size(8.dp).background(cat.color, CircleShape))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(text = cat.categoryName, style = MaterialTheme.typography.labelSmall, modifier = Modifier.weight(1f))
                                Text(text = "$perc%", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Detail Transaksi
        Column(modifier = Modifier.padding(horizontal = 16.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text(text = "Detail Transaksi", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Text(
                    text = "Lihat semua", 
                    style = MaterialTheme.typography.labelMedium, 
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.clickable { onSeeAllTransactions() }
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
            val displayTransactions = remember(reportTransactions) {
                reportTransactions.filter { it.type == TransactionType.INCOME || it.type == TransactionType.EXPENSE }
            }
            displayTransactions.take(2).forEach {
                TransactionItem(it, formatter)
            }
        }
        
        Spacer(modifier = Modifier.height(32.dp))
    }
}

@Composable
fun TimeRangeChip(label: String, selected: Boolean, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(20.dp),
        color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface,
        border = if (selected) null else androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)),
        modifier = modifier.height(40.dp)
    ) {
        Box(contentAlignment = Alignment.Center, modifier = Modifier.padding(horizontal = 12.dp)) {
            Text(
                text = label, 
                color = if (selected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant, 
                style = MaterialTheme.typography.labelLarge,
                fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium
            )
        }
    }
}

@Composable
fun ReportSummaryCard(
    title: String,
    amount: Double,
    color: Color,
    icon: ImageVector,
    formatter: NumberFormat,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Box(modifier = Modifier.size(24.dp).background(color.copy(alpha = 0.1f), CircleShape), contentAlignment = Alignment.Center) {
                Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(14.dp))
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = title, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            NominalText(
                amountText = formatter.format(amount).replace("Rp", "Rp "),
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
private fun BarChart(transactions: List<Transaction>) {
    val weeklyData = remember(transactions) {
        val calendar = Calendar.getInstance()
        // Simple grouping by week of month (1-5)
        val data = mutableMapOf<Int, Pair<Double, Double>>()
        for (i in 1..5) data[i] = 0.0 to 0.0
        
        transactions.forEach {
            calendar.time = it.date
            val week = calendar.get(Calendar.WEEK_OF_MONTH)
            val current = data[week] ?: (0.0 to 0.0)
            if (it.type == TransactionType.INCOME) {
                data[week] = (current.first + it.amount) to current.second
            } else if (it.type == TransactionType.EXPENSE) {
                data[week] = current.first to (current.second + it.amount)
            }
        }
        data.toList().sortedBy { it.first }
    }

    val labels = listOf("Mgu 1", "Mgu 2", "Mgu 3", "Mgu 4", "Mgu 5")
    
    Column {
        Canvas(modifier = Modifier.fillMaxWidth().height(180.dp)) {
            val canvasWidth = size.width
            val canvasHeight = size.height
            val barWidth = 20f
            val spacing = (canvasWidth - (weeklyData.size * 2 * barWidth)) / (weeklyData.size + 1)
            
            val maxVal = weeklyData.flatMap { listOf(it.second.first, it.second.second) }.maxOrNull()?.toFloat()?.coerceAtLeast(1000f) ?: 1000f
            
            weeklyData.forEachIndexed { index, (_, amounts) ->
                val xInc = spacing + index * (2 * barWidth + spacing)
                val hInc = (amounts.first.toFloat() / maxVal) * canvasHeight
                
                drawRoundRect(
                    color = IncomeGreen,
                    topLeft = androidx.compose.ui.geometry.Offset(xInc, canvasHeight - hInc),
                    size = androidx.compose.ui.geometry.Size(barWidth, hInc),
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(10f)
                )
                
                val xExp = xInc + barWidth + 6f
                val hExp = (amounts.second.toFloat() / maxVal) * canvasHeight
                
                drawRoundRect(
                    color = ExpenseRed,
                    topLeft = androidx.compose.ui.geometry.Offset(xExp, canvasHeight - hExp),
                    size = androidx.compose.ui.geometry.Size(barWidth, hExp),
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(10f)
                )
            }
        }
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            labels.forEach { label ->
                Text(text = label, style = MaterialTheme.typography.labelSmall, color = Color.Gray)
            }
        }
    }
}

@Composable
private fun DonutChart(transactions: List<Transaction>) {
    val expenseCategories = remember(transactions) {
        transactions.filter { it.type == TransactionType.EXPENSE }
            .groupBy { it.category }
            .mapValues { it.value.sumOf { t -> t.amount } }
            .toList()
            .sortedByDescending { it.second }
    }
    val total = expenseCategories.sumOf { it.second }

    Canvas(modifier = Modifier.fillMaxSize()) {
        if (total == 0.0) {
            drawCircle(color = Color.LightGray, style = Stroke(width = 30f))
        } else {
            var startAngle = -90f
            expenseCategories.forEach { (category, amount) ->
                val sweepAngle = (amount / total * 360f).toFloat()
                drawArc(
                    color = category.color,
                    startAngle = startAngle,
                    sweepAngle = sweepAngle,
                    useCenter = false,
                    style = Stroke(width = 30f, cap = StrokeCap.Round)
                )
                startAngle += sweepAngle
            }
        }
    }
}

private fun isSameDay(cal1: Calendar, cal2: Calendar): Boolean {
    return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
            cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR)
}

@Preview(showBackground = true)
@Composable
fun ReportsScreenPreview() {
    val formatter = NumberFormat.getCurrencyInstance(Locale("in", "ID"))
    com.example.moneymanagement.ui.theme.MoneyManagementTheme {
        ReportsScreen(
            transactions = listOf(
                Transaction(description = "Beli Kopi Kekinian", amount = 35000.0, type = TransactionType.EXPENSE, category = Category.FOOD, date = Date()),
            ),
            formatter = formatter,
            onDetailClick = {},
            onSeeAllTransactions = {}
        )
    }
}
