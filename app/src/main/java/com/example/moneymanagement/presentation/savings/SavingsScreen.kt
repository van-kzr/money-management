package com.example.moneymanagement.presentation.savings

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.moneymanagement.domain.model.Saving
import com.example.moneymanagement.R
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

@Composable
fun SavingsScreen(
    savings: List<Saving>,
    formatter: NumberFormat,
    onUpdateSaving: (String, Double) -> Unit
) {
    var selectedMonth by remember { mutableStateOf<Int?>(null) }
    var selectedYear by remember { mutableStateOf<Int?>(null) }

    var monthExpanded by remember { mutableStateOf(false) }
    var yearExpanded by remember { mutableStateOf(false) }

    var adjustSavingId by remember { mutableStateOf<String?>(null) }

    val months = listOf(
        "Januari", "Februari", "Maret", "April", "Mei", "Juni",
        "Juli", "Agustus", "September", "Oktober", "November", "Desember"
    )
    val years = (2020..2030).toList()

    val filteredSavings = savings.filter { saving ->
        val cal = Calendar.getInstance().apply { time = saving.date }
        val monthMatch = selectedMonth == null || cal.get(Calendar.MONTH) == selectedMonth
        val yearMatch = selectedYear == null || cal.get(Calendar.YEAR) == selectedYear
        monthMatch && yearMatch
    }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp), ) {
        Box(
            modifier = Modifier.fillMaxWidth().padding(16.dp).height(50.dp), contentAlignment = Alignment.Center,) {
            Text(text = "Manajemen Tabungan", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        }
        Spacer(modifier = Modifier.height(16.dp))

        // Filters
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Box(modifier = Modifier.weight(1f)) {
                OutlinedButton(onClick = { monthExpanded = true }, modifier = Modifier.fillMaxWidth()) {
                    Text(if (selectedMonth != null) months[selectedMonth!!] else "Bulan")
                }
                DropdownMenu(expanded = monthExpanded, onDismissRequest = { monthExpanded = false }) {
                    DropdownMenuItem(text = { Text("Semua") }, onClick = { selectedMonth = null; monthExpanded = false })
                    months.forEachIndexed { index, month ->
                        DropdownMenuItem(text = { Text(month) }, onClick = { selectedMonth = index; monthExpanded = false })
                    }
                }
            }

            Box(modifier = Modifier.weight(1f)) {
                OutlinedButton(onClick = { yearExpanded = true }, modifier = Modifier.fillMaxWidth()) {
                    Text(selectedYear?.toString() ?: "Tahun")
                }
                DropdownMenu(expanded = yearExpanded, onDismissRequest = { yearExpanded = false }) {
                    DropdownMenuItem(text = { Text("Semua") }, onClick = { selectedYear = null; yearExpanded = false })
                    years.forEach { year ->
                        DropdownMenuItem(text = { Text(year.toString()) }, onClick = { selectedYear = year; yearExpanded = false })
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(10.dp),
            contentPadding = PaddingValues(bottom = 16.dp)
        ) {
            items(filteredSavings, key = { it.id }) { saving ->
                SavingManagementItem(
                    saving = saving,
                    formatter = formatter,
                    onAdjust = { adjustSavingId = saving.id }
                )
            }
        }
    }

    if (adjustSavingId != null) {
        AdjustSavingDialog(
            onDismiss = { adjustSavingId = null },
            onConfirm = { delta ->
                onUpdateSaving(adjustSavingId!!, delta)
                adjustSavingId = null
            }
        )
    }
}

@Composable
fun SavingManagementItem(
    saving: Saving,
    formatter: NumberFormat,
    onAdjust: () -> Unit
) {
    val dateFormatter = remember { SimpleDateFormat("dd MMM yyyy", Locale("in", "ID")) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(5.dp)
    ) {
        Row(
            modifier = Modifier.padding(12.dp).fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(MaterialTheme.colorScheme.tertiary.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.other),
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.tertiary,
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(text = saving.name, fontWeight = FontWeight.Bold)
                Text(text = dateFormatter.format(saving.date), style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                Text(text = formatter.format(saving.amount), fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.tertiary)
            }

            Button(onClick = onAdjust) {
                Text("Pulihkan")
            }
        }
    }
}

@Composable
fun AdjustSavingDialog(
    onDismiss: () -> Unit,
    onConfirm: (Double) -> Unit
) {
    var amountText by remember { mutableStateOf("") }
    var isSubtract by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Sesuaikan Saldo Tabungan") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    RadioButton(selected = !isSubtract, onClick = { isSubtract = false })
                    Text("Tambah")
                    Spacer(modifier = Modifier.width(8.dp))
                    RadioButton(selected = isSubtract, onClick = { isSubtract = true })
                    Text("Kurangi")
                }
                OutlinedTextField(
                    value = amountText,
                    onValueChange = { amountText = it },
                    label = { Text("Jumlah Nominal") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(onClick = {
                val amount = amountText.toDoubleOrNull() ?: 0.0
                onConfirm(if (isSubtract) -amount else amount)
            }) {
                Text("Simpan")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Batal")
            }
        }
    )
}

@Preview(showBackground = true)
@Composable
fun SavingsScreenPreview() {
    val formatter = NumberFormat.getCurrencyInstance(Locale("in", "ID"))
    MaterialTheme {
        SavingsScreen(
            savings = listOf(
                Saving(
                    name = "Dana Darurat",
                    amount = 5000000.0,
                    type = com.example.moneymanagement.domain.model.TransactionType.Saving
                ),
                Saving(
                    name = "Liburan",
                    amount = 2000000.0,
                    type = com.example.moneymanagement.domain.model.TransactionType.Saving
                )
            ),
            formatter = formatter,
            onUpdateSaving = { _, _ -> }
        )
    }
}


