package com.example.moneymanagement.presentation.history

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.moneymanagement.domain.model.Category
import com.example.moneymanagement.domain.model.Transaction
import com.example.moneymanagement.domain.model.TransactionType
import com.example.moneymanagement.ui.theme.IncomeGreen
import com.example.moneymanagement.ui.theme.ExpenseRed
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun TransactionDetailScreen(
    transaction: Transaction,
    onBack: () -> Unit,
    formatter: NumberFormat
) {
    val dateFormat = remember { SimpleDateFormat("dd MMM 2026, HH:mm", Locale("in", "ID")) }
    val amountColor = if (transaction.type == TransactionType.INCOME) IncomeGreen else ExpenseRed

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF8F9FA))
    ) {
        // Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null)
            }
            Text(text = "Detail Transaksi", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Transaction Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .clip(CircleShape)
                        .background(transaction.category.color.copy(alpha = 0.1f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        painter = painterResource(id = transaction.category.logo),
                        contentDescription = null,
                        tint = transaction.category.color,
                        modifier = Modifier.size(40.dp)
                    )
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Text(text = transaction.category.categoryName, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                Text(
                    text = if (transaction.type == TransactionType.INCOME) "Pemasukan" else "Pengeluaran",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.primary
                )
                
                Spacer(modifier = Modifier.height(24.dp))
                
                Text(
                    text = (if (transaction.type == TransactionType.INCOME) "+ " else "- ") + 
                           formatter.format(transaction.amount).replace("Rp", "Rp "),
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.ExtraBold,
                    color = amountColor
                )
                
                Spacer(modifier = Modifier.height(32.dp))
                
                DetailRow(icon = Icons.Default.CalendarToday, label = "Tanggal & Waktu", value = dateFormat.format(transaction.date))
                DetailRow(icon = Icons.Default.Category, label = "Kategori", value = transaction.category.categoryName)
                DetailRow(icon = Icons.Default.Description, label = "Deskripsi", value = transaction.description)
                DetailRow(icon = Icons.Default.AccountBalanceWallet, label = "Sumber", value = "Saldo Utama")
                
                Spacer(modifier = Modifier.height(24.dp))
                
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFFE9F7F2), RoundedCornerShape(12.dp))
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.CheckCircle, contentDescription = null, tint = IncomeGreen)
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(text = "Transaksi ini telah dicatat", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
                        Text(text = "pada 15 Sep 2026, 09:00", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                    }
                }
            }
        }
    }
}

@Composable
private fun DetailRow(icon: androidx.compose.ui.graphics.vector.ImageVector, label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.Top
    ) {
        Box(
            modifier = Modifier.size(36.dp).background(Color(0xFFF1F3F5), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(18.dp))
        }
        Spacer(modifier = Modifier.width(16.dp))
        Column {
            Text(text = label, style = MaterialTheme.typography.labelSmall, color = Color.Gray)
            Text(text = value, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold)
        }
    }
}

@Preview(showBackground = true)
@Composable
fun TransactionDetailPreview() {
    val formatter = NumberFormat.getCurrencyInstance(Locale("in", "ID"))
    com.example.moneymanagement.ui.theme.MoneyManagementTheme {
        TransactionDetailScreen(
            transaction = Transaction(
                description = "Gaji Bulanan Utama",
                amount = 7500000.0,
                type = TransactionType.INCOME,
                category = Category.SALARY,
                date = Date()
            ),
            onBack = {},
            formatter = formatter
        )
    }
}
