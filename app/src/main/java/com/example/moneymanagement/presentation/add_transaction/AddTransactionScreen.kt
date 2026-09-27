package com.example.moneymanagement.presentation.add_transaction

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.moneymanagement.domain.model.Category
import com.example.moneymanagement.domain.model.TransactionType
import com.example.moneymanagement.presentation.component.RupiahVisualTransformation
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddTransactionScreen(
    onDismiss: () -> Unit,
    onConfirm: (String, Double, TransactionType, Category) -> Unit,
    initialType: TransactionType = TransactionType.EXPENSE
) {
    var description by remember { mutableStateOf("") }
    var amount by remember { mutableStateOf("") }
    var type by remember { mutableStateOf(initialType) }
    var category by remember { mutableStateOf(Category.OTHER) }
    var selectedDate by remember { mutableStateOf(Date()) }
    
    var showCategoryPicker by remember { mutableStateOf(false) }

    val amountDouble = amount.toDoubleOrNull() ?: 0.0
    val isInputValid = description.isNotBlank() && amountDouble > 0

    if (showCategoryPicker) {
        CategoryPickerScreen(
            onBack = { showCategoryPicker = false },
            onCategorySelected = {
                category = it
                showCategoryPicker = false
            },
            initialType = type
        )
        return
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
            .padding(vertical = 26.dp)
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
            Text(text = "Tambah Transaksi", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        }

        Column(modifier = Modifier.padding(16.dp)) {
            // Segmented Button
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f), RoundedCornerShape(28.dp))
                    .padding(4.dp)
            ) {
                listOf(
                    TransactionType.INCOME to "Masuk", 
                    TransactionType.EXPENSE to "Keluar"
                ).forEach { (t, label) ->
                    val selected = type == t
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .background(
                                color = if (selected) MaterialTheme.colorScheme.primary else Color.Transparent,
                                shape = RoundedCornerShape(24.dp)
                            )
                            .clickable { type = t }
                            .padding(horizontal = 16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = label,
                            color = if (selected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Date & Time
            Text(text = "Tanggal & Waktu", style = MaterialTheme.typography.labelMedium, color = Color.Gray)
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale("in", "ID")).format(selectedDate),
                onValueChange = { },
                readOnly = true,
                leadingIcon = { Icon(Icons.Default.CalendarToday, contentDescription = null, modifier = Modifier.size(20.dp)) },
                modifier = Modifier.fillMaxWidth().clickable { /* Show date picker logic here if needed */ },
                enabled = false,
                colors = OutlinedTextFieldDefaults.colors(
                    disabledTextColor = MaterialTheme.colorScheme.onSurface,
                    disabledBorderColor = MaterialTheme.colorScheme.outline,
                    disabledLeadingIconColor = MaterialTheme.colorScheme.onSurfaceVariant
                ),
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Deskripsi
            Text(text = "Deskripsi", style = MaterialTheme.typography.labelMedium, color = Color.Gray)
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                placeholder = { Text("Contoh: Gaji Bulanan") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Kategori
            if (type != TransactionType.Saving) {
                Text(text = "Kategori", style = MaterialTheme.typography.labelMedium, color = Color.Gray)
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = category.categoryName,
                    onValueChange = { },
                    readOnly = true,
                    leadingIcon = { 
                        Box(modifier = Modifier.padding(start = 12.dp, end = 8.dp).size(24.dp).background(category.color.copy(alpha = 0.1f), CircleShape), contentAlignment = Alignment.Center) {
                            Icon(painterResource(id = category.logo), contentDescription = null, tint = category.color, modifier = Modifier.size(16.dp))
                        }
                    },
                    trailingIcon = { Icon(Icons.Default.ArrowDropDown, contentDescription = null) },
                    modifier = Modifier.fillMaxWidth().clickable { showCategoryPicker = true },
                    enabled = false,
                    colors = OutlinedTextFieldDefaults.colors(
                        disabledTextColor = MaterialTheme.colorScheme.onSurface,
                        disabledBorderColor = MaterialTheme.colorScheme.outline,
                        disabledLeadingIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        disabledTrailingIconColor = MaterialTheme.colorScheme.onSurfaceVariant
                    ),
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(modifier = Modifier.height(16.dp))
            }

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

            Spacer(modifier = Modifier.height(32.dp))

            // Simpan Button
            Button(
                onClick = {
                    onConfirm(description, amountDouble, type, category)
                },
                enabled = isInputValid,
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = RoundedCornerShape(28.dp)
            ) {
                Text(text = "Simpan", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun AddTransactionScreenPreview() {
    com.example.moneymanagement.ui.theme.MoneyManagementTheme {
        AddTransactionScreen(
            onDismiss = {},
            onConfirm = { _, _, _, _ -> }
        )
    }
}
