package com.example.moneymanagement.presentation.savings

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.moneymanagement.domain.model.TransactionType
import com.example.moneymanagement.presentation.component.RupiahVisualTransformation
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddSavingScreen(
    onDismiss: () -> Unit,
    onConfirm: (String, Double, Double, TransactionType) -> Unit
) {
    var description by remember { mutableStateOf("") }
    var amount by remember { mutableStateOf("") }
    var targetAmount by remember { mutableStateOf("") }
    var selectedDate by remember { mutableStateOf(Date()) }
    
    val amountDouble = amount.toDoubleOrNull() ?: 0.0
    val targetDouble = targetAmount.toDoubleOrNull() ?: 0.0
    val isInputValid = description.isNotBlank() && amountDouble > 0 && targetDouble >= amountDouble

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
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onDismiss) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null)
            }
            Text(text = "Tambah Tabungan", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        }

        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Pindahkan sebagian saldo Anda ke dalam tabungan untuk mencapai target finansial.",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.Gray,
                modifier = Modifier.padding(bottom = 24.dp)
            )

            // Date & Time
            Text(text = "Tanggal & Waktu", style = MaterialTheme.typography.labelMedium, color = Color.Gray)
            Spacer(modifier = Modifier.height(8.dp))
            val dateLabel = remember(selectedDate) { SimpleDateFormat("dd/MM/yyyy HH:mm", Locale("in", "ID")).format(selectedDate) }
            OutlinedTextField(
                value = dateLabel,
                onValueChange = { },
                readOnly = true,
                leadingIcon = { Icon(Icons.Default.CalendarToday, contentDescription = null, modifier = Modifier.size(20.dp)) },
                modifier = Modifier.fillMaxWidth(),
                enabled = false,
                colors = OutlinedTextFieldDefaults.colors(
                    disabledTextColor = MaterialTheme.colorScheme.onSurface,
                    disabledBorderColor = MaterialTheme.colorScheme.outline,
                    disabledLeadingIconColor = MaterialTheme.colorScheme.onSurfaceVariant
                ),
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Nama Tabungan / Deskripsi
            Text(text = "Nama Tabungan", style = MaterialTheme.typography.labelMedium, color = Color.Gray)
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                placeholder = { Text("Contoh: Dana Darurat") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Jumlah
            Text(text = "Jumlah", style = MaterialTheme.typography.labelMedium, color = Color.Gray)
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = amount,
                onValueChange = { if (it.all { c -> c.isDigit() }) amount = it },
                leadingIcon = { Text(text = "Rp", style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold, modifier = Modifier.padding(start = 12.dp)) },
                placeholder = { Text("Masukkan jumlah") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Done),
                visualTransformation = RupiahVisualTransformation(),
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Target Nominal
            Text(text = "Target Nominal", style = MaterialTheme.typography.labelMedium, color = Color.Gray)
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = targetAmount,
                onValueChange = { if (it.all { c -> c.isDigit() }) targetAmount = it },
                leadingIcon = { Text(text = "Rp", style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold, modifier = Modifier.padding(start = 12.dp)) },
                placeholder = { Text("Contoh: 10000000") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Done),
                visualTransformation = RupiahVisualTransformation(),
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Simpan Button
            Button(
                onClick = {
                    onConfirm(description, amountDouble, targetDouble, TransactionType.Saving)
                },
                enabled = isInputValid,
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = RoundedCornerShape(28.dp)
            ) {
                Text(text = "Simpan ke Tabungan", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun AddSavingScreenPreview() {
    com.example.moneymanagement.ui.theme.MoneyManagementTheme {
        AddSavingScreen(
            onDismiss = {},
            onConfirm = { _, _, _, _ -> }
        )
    }
}
