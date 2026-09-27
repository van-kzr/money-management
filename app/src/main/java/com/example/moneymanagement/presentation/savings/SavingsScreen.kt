package com.example.moneymanagement.presentation.savings

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.moneymanagement.domain.model.Saving
import com.example.moneymanagement.domain.model.Transaction
import com.example.moneymanagement.domain.model.TransactionType
import com.example.moneymanagement.ui.theme.IncomeGreen
import com.example.moneymanagement.ui.theme.ExpenseRed
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SavingsScreen(
    savings: List<Saving>,
    allTransactions: List<Transaction>,
    formatter: NumberFormat,
    onAddSavingClick: () -> Unit,
    onUpdateSaving: (String, Double) -> Unit = { _, _ -> }
) {
    val totalSaving = savings.sumOf { it.amount }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
    ) {
        // Header
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.primary, RoundedCornerShape(bottomStart = 32.dp, bottomEnd = 32.dp))
                .padding(16.dp)
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null, tint = Color.White)
                    Spacer(modifier = Modifier.width(16.dp))
                    Text(text = "Tabungan", style = MaterialTheme.typography.titleLarge, color = Color.White, fontWeight = FontWeight.Bold)
                }
                
                Spacer(modifier = Modifier.height(24.dp))
                
                Box(
                    modifier = Modifier.size(64.dp).background(Color.White.copy(alpha = 0.2f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.Savings, contentDescription = null, tint = Color.White, modifier = Modifier.size(32.dp))
                }
                
                Spacer(modifier = Modifier.height(12.dp))
                Text(text = "Total Tabungan", style = MaterialTheme.typography.bodySmall, color = Color.White.copy(alpha = 0.8f))
                Text(text = formatter.format(totalSaving), style = MaterialTheme.typography.headlineMedium, color = Color.White, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(24.dp))
            }
        }

        Column(modifier = Modifier.padding(16.dp)) {
            // Target Tabungan
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text(text = "Target Tabungan", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Goals
            if (savings.isEmpty()) {
                Box(modifier = Modifier.fillMaxWidth().padding(24.dp), contentAlignment = Alignment.Center) {
                    Text("Belum ada target tabungan", style = MaterialTheme.typography.bodyMedium, color = Color.Gray)
                }
            } else {
                savings.forEach { saving ->
                    TargetSavingItem(
                        title = saving.name,
                        current = saving.amount,
                        target = if (saving.targetAmount > 0) saving.targetAmount else 5000000.0,
                        formatter = formatter,
                        icon = Icons.Default.Savings
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                }
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            Button(
                onClick = onAddSavingClick,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                contentPadding = PaddingValues(16.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text(text = "Tambah Tabungan")
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Riwayat Tabungan
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text(text = "Riwayat Tabungan", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Text(text = "Lihat semua", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            val savingTransactions = allTransactions.filter { it.type == TransactionType.Saving || it.type == TransactionType.WITHDRAW_SAVING }
            
            if (savingTransactions.isEmpty()) {
                Box(modifier = Modifier.fillMaxWidth().padding(16.dp), contentAlignment = Alignment.Center) {
                    Text("Belum ada riwayat tabungan", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                }
            } else {
                val dateFormat = remember { SimpleDateFormat("dd MMM yyyy, HH:mm", Locale("in", "ID")) }
                savingTransactions.take(5).forEach { transaction ->
                    RiwayatSavingItem(
                        title = transaction.description,
                        date = dateFormat.format(transaction.date),
                        amount = transaction.amount,
                        isAdd = transaction.type == TransactionType.Saving,
                        formatter = formatter
                    )
                }
            }
        }
    }
}

@Composable
fun TargetSavingItem(
    title: String,
    current: Double,
    target: Double,
    formatter: NumberFormat,
    icon: androidx.compose.ui.graphics.vector.ImageVector
) {
    val progress = (current / target).toFloat()
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(modifier = Modifier.size(40.dp).background(IncomeGreen.copy(alpha = 0.1f), RoundedCornerShape(8.dp)), contentAlignment = Alignment.Center) {
                    Icon(icon, contentDescription = null, tint = IncomeGreen, modifier = Modifier.size(20.dp))
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(text = title, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                    Row {
                        Text(text = formatter.format(current), style = MaterialTheme.typography.bodySmall, color = IncomeGreen, fontWeight = FontWeight.Bold)
                        Text(text = " / ${formatter.format(target)}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
                Text(text = "${(progress * 100).toInt()}%", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Spacer(modifier = Modifier.height(12.dp))
            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier.fillMaxWidth().height(8.dp).clip(CircleShape),
                color = IncomeGreen,
                trackColor = MaterialTheme.colorScheme.surfaceVariant,
                strokeCap = StrokeCap.Round
            )
        }
    }
}

@Composable
fun RiwayatSavingItem(
    title: String,
    date: String,
    amount: Double,
    isAdd: Boolean,
    formatter: NumberFormat
) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier.size(40.dp).background(if (isAdd) IncomeGreen.copy(alpha = 0.1f) else ExpenseRed.copy(alpha = 0.1f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                if (isAdd) Icons.Default.FileUpload else Icons.Default.FileDownload,
                contentDescription = null,
                tint = if (isAdd) IncomeGreen else ExpenseRed,
                modifier = Modifier.size(20.dp)
            )
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(text = title, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurface)
            Text(text = date, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Text(
            text = (if (isAdd) "+ " else "- ") + formatter.format(amount).replace("Rp", "Rp "),
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Bold,
            color = if (isAdd) IncomeGreen else ExpenseRed
        )
    }
}

@Preview(showBackground = true)
@Composable
fun SavingsScreenPreview() {
    val formatter = NumberFormat.getCurrencyInstance(Locale("in", "ID"))
    com.example.moneymanagement.ui.theme.MoneyManagementTheme {
        SavingsScreen(
            savings = emptyList(),
            allTransactions = emptyList(),
            formatter = formatter,
            onAddSavingClick = {}
        )
    }
}
