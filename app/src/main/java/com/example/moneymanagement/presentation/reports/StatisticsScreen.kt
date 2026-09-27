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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.moneymanagement.domain.model.Category
import com.example.moneymanagement.domain.model.Transaction
import com.example.moneymanagement.domain.model.TransactionType
import com.example.moneymanagement.presentation.component.NominalText
import com.example.moneymanagement.ui.theme.IncomeGreen
import com.example.moneymanagement.ui.theme.ExpenseRed
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun StatisticsScreen(
    onBack: () -> Unit,
    transactions: List<Transaction>,
    formatter: NumberFormat
) {
    var calendarState by remember { mutableStateOf(Calendar.getInstance()) }
    var selectedTab by remember { mutableIntStateOf(0) } // 0: Pemasukan, 1: Pengeluaran

    val filteredTransactions = remember(transactions, calendarState, selectedTab) {
        val targetType = if (selectedTab == 0) TransactionType.INCOME else TransactionType.EXPENSE
        transactions.filter {
            val transCal = Calendar.getInstance().apply { time = it.date }
            transCal.get(Calendar.MONTH) == calendarState.get(Calendar.MONTH) &&
            transCal.get(Calendar.YEAR) == calendarState.get(Calendar.YEAR) &&
            it.type == targetType
        }
    }

    val totalAmount = filteredTransactions.sumOf { it.amount }
    val monthLabel = remember(calendarState) {
        SimpleDateFormat("MMMM yyyy", Locale("in", "ID")).format(calendarState.time)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF8F9FA))
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
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null)
            }
            Text(text = "Statistik", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.width(48.dp))
        }

        // Month Selector
        Card(
            modifier = Modifier.padding(horizontal = 16.dp),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            border = androidx.compose.foundation.BorderStroke(1.dp, Color.LightGray.copy(alpha = 0.5f))
        ) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = {
                    val newCal = calendarState.clone() as Calendar
                    newCal.add(Calendar.MONTH, -1)
                    calendarState = newCal
                }) {
                    Icon(Icons.Default.ArrowBack, contentDescription = null, modifier = Modifier.size(18.dp), tint = Color.Gray)
                }
                Text(text = monthLabel, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                IconButton(onClick = {
                    val newCal = calendarState.clone() as Calendar
                    newCal.add(Calendar.MONTH, 1)
                    calendarState = newCal
                }) {
                    Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, modifier = Modifier.size(18.dp), tint = Color.Gray)
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Toggle Pemasukan/Pengeluaran
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .height(48.dp)
                .background(Color(0xFFE9ECEF), RoundedCornerShape(24.dp))
                .padding(4.dp)
        ) {
            TabOption(
                label = "Pemasukan",
                selected = selectedTab == 0,
                color = IncomeGreen,
                onClick = { selectedTab = 0 },
                modifier = Modifier.weight(1f)
            )
            TabOption(
                label = "Pengeluaran",
                selected = selectedTab == 1,
                color = ExpenseRed,
                onClick = { selectedTab = 1 },
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Total Summary
        Column(modifier = Modifier.padding(horizontal = 16.dp)) {
            NominalText(
                amountText = formatter.format(totalAmount).replace("Rp", "Rp "),
                style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface
            )
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = if (selectedTab == 0) Icons.Default.ArrowUpward else Icons.Default.ArrowDownward,
                    contentDescription = null,
                    tint = if (selectedTab == 0) IncomeGreen else ExpenseRed,
                    modifier = Modifier.size(14.dp)
                )
                Text(
                    text = "Total ${if (selectedTab == 0) "Pemasukan" else "Pengeluaran"} di $monthLabel",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Line Chart
        LineChart(
            modifier = Modifier.padding(horizontal = 16.dp),
            transactions = filteredTransactions,
            color = if (selectedTab == 0) IncomeGreen else ExpenseRed
        )

        Spacer(modifier = Modifier.height(32.dp))

        // Top Sources
        val categoriesData = remember(filteredTransactions) {
            filteredTransactions.groupBy { it.category }
                .mapValues { it.value.sumOf { t -> t.amount } }
                .toList()
                .sortedByDescending { it.second }
        }

        Column(modifier = Modifier.padding(horizontal = 16.dp)) {
            Text(
                text = "Sumber ${if (selectedTab == 0) "Pemasukan" else "Pengeluaran"}", 
                style = MaterialTheme.typography.titleMedium, 
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(16.dp))
            
            if (categoriesData.isEmpty()) {
                Box(modifier = Modifier.fillMaxWidth().padding(24.dp), contentAlignment = Alignment.Center) {
                    Text("Tidak ada data untuk periode ini", color = Color.Gray)
                }
            } else {
                categoriesData.forEach { (category, amount) ->
                    SourceProgressItem(category.categoryName, amount, totalAmount, category.color, formatter)
                }
            }
        }
        
        Spacer(modifier = Modifier.height(32.dp))
    }
}

@Composable
private fun TabOption(
    label: String,
    selected: Boolean,
    color: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxHeight()
            .background(if (selected) color else Color.Transparent, RoundedCornerShape(20.dp))
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            color = if (selected) Color.White else Color.Gray,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal
        )
    }
}

@Composable
private fun LineChart(
    modifier: Modifier = Modifier,
    transactions: List<Transaction>,
    color: Color
) {
    // Grouping by days (simplified to 5 segments/weeks for visual)
    val points = remember(transactions) {
        val calendar = Calendar.getInstance()
        val dailyMap = mutableMapOf<Int, Double>()
        for (i in 1..31) dailyMap[i] = 0.0
        
        transactions.forEach {
            calendar.time = it.date
            val day = calendar.get(Calendar.DAY_OF_MONTH)
            dailyMap[day] = dailyMap[day]!! + it.amount
        }
        
        val maxVal = dailyMap.values.maxOrNull()?.takeIf { it > 0 } ?: 1.0
        listOf(1, 7, 14, 21, 28).map { day ->
            (dailyMap[day]!! / maxVal).toFloat().coerceIn(0.1f, 1f)
        }
    }
    
    val labels = listOf("1", "7", "14", "21", "28")

    Column(modifier = modifier) {
        Canvas(modifier = Modifier.fillMaxWidth().height(200.dp)) {
            val width = size.width
            val height = size.height
            val spacing = width / (points.size - 1)
            
            val path = Path().apply {
                moveTo(0f, height - (points[0] * height))
                for (i in 1 until points.size) {
                    lineTo(i * spacing, height - (points[i] * height))
                }
            }
            
            drawPath(
                path = path,
                color = color,
                style = Stroke(width = 10f, cap = StrokeCap.Round)
            )
            
            for (i in points.indices) {
                drawCircle(
                    color = color,
                    radius = 10f,
                    center = androidx.compose.ui.geometry.Offset(i * spacing, height - (points[i] * height))
                )
                drawCircle(
                    color = Color.White,
                    radius = 5f,
                    center = androidx.compose.ui.geometry.Offset(i * spacing, height - (points[i] * height))
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
private fun SourceProgressItem(
    label: String,
    amount: Double,
    total: Double,
    color: Color,
    formatter: NumberFormat
) {
    val progress = (amount / total).toFloat()
    Column(modifier = Modifier.padding(vertical = 8.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(modifier = Modifier.size(10.dp).background(color, CircleShape))
            Spacer(modifier = Modifier.width(12.dp))
            Text(text = label, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.weight(1f))
            NominalText(
                amountText = formatter.format(amount).replace("Rp", "Rp "),
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
        LinearProgressIndicator(
            progress = { progress },
            modifier = Modifier.fillMaxWidth().height(8.dp).clip(CircleShape),
            color = color,
            trackColor = Color.LightGray.copy(alpha = 0.2f),
            strokeCap = StrokeCap.Round
        )
        Text(
            text = "${(progress * 100).toInt()}%",
            style = MaterialTheme.typography.labelSmall,
            color = Color.Gray,
            modifier = Modifier.align(Alignment.End).padding(top = 4.dp)
        )
    }
}

@Preview(showBackground = true)
@Composable
fun StatisticsPreview() {
    val formatter = NumberFormat.getCurrencyInstance(Locale("in", "ID"))
    com.example.moneymanagement.ui.theme.MoneyManagementTheme {
        StatisticsScreen(onBack = {}, transactions = emptyList(), formatter = formatter)
    }
}
