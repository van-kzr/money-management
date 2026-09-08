package com.example.moneymanagement.presentation.component

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.*
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.moneymanagement.domain.model.Category
import com.example.moneymanagement.domain.model.TransactionType
import java.text.NumberFormat
import java.util.Locale

@Composable
fun AddTransactionDialog(
    onDismiss: () -> Unit,
    onConfirm: (String, Double, TransactionType, Category) -> Unit,
    onConfirmSaving: (String, Double, TransactionType) -> Unit
) {
    var description by remember { mutableStateOf("") }
    var amount by remember { mutableStateOf("") }
    var type by remember { mutableStateOf(TransactionType.EXPENSE) }
    var category by remember { mutableStateOf(Category.OTHER) }

    val amountDouble = amount.toDoubleOrNull() ?: 0.0
    val isInputValid = description.isNotBlank() && amountDouble > 0

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Tambah Transaksi") },
        shape = RoundedCornerShape(5.dp),
        text = {
            AddTransactionForm(
                description = description,
                onDescriptionChange = { description = it },
                amount = amount,
                onAmountChange = { amount = it },
                type = type,
                onTypeChange = { 
                    type = it
                    category = Category.OTHER
                },
                category = category,
                onCategoryChange = { category = it }
            )
        },
        confirmButton = {
            Button(
                onClick = {
                    if (type == TransactionType.Saving) {
                        onConfirmSaving(description, amountDouble, type)
                    } else {
                        onConfirm(description, amountDouble, type, category)
                    }
                    onDismiss()
                },
                enabled = isInputValid
            ) {
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddTransactionForm(
    description: String,
    onDescriptionChange: (String) -> Unit,
    amount: String,
    onAmountChange: (String) -> Unit,
    type: TransactionType,
    onTypeChange: (TransactionType) -> Unit,
    category: Category,
    onCategoryChange: (Category) -> Unit,
    modifier: Modifier = Modifier
) {
    var categoryExpanded by remember { mutableStateOf(false) }

    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Tipe Transaksi
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            TransactionTypeOption(
                label = "Pemasukan",
                selected = type == TransactionType.INCOME,
                onSelect = { onTypeChange(TransactionType.INCOME) },
                modifier = Modifier.weight(1f)
            )
            TransactionTypeOption(
                label = "Pengeluaran",
                selected = type == TransactionType.EXPENSE,
                onSelect = { onTypeChange(TransactionType.EXPENSE) },
                modifier = Modifier.weight(1f)
            )
            TransactionTypeOption(
                label = "Tabungan",
                selected = type == TransactionType.Saving,
                onSelect = { onTypeChange(TransactionType.Saving) },
                modifier = Modifier.weight(1f)
            )
        }

        // Kategori
        if (type != TransactionType.Saving) {
            ExposedDropdownMenuBox(
                expanded = categoryExpanded,
                onExpandedChange = { categoryExpanded = !categoryExpanded }
            ) {
                OutlinedTextField(
                    value = category.categoryName,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Kategori") },
                    leadingIcon = {
                        Icon(
                            painter = painterResource(id = category.logo),
                            contentDescription = null,
                            tint = category.color,
                            modifier = Modifier.size(24.dp)
                        )
                    },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = categoryExpanded) },
                    modifier = Modifier
                        .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable)
                        .fillMaxWidth()
                )

                ExposedDropdownMenu(
                    expanded = categoryExpanded,
                    onDismissRequest = { categoryExpanded = false }
                ) {
                    Category.entries
                        .filter { it.type == null || it.type == type }
                        .forEach { item ->
                            DropdownMenuItem(
                                text = { Text(item.categoryName) },
                                leadingIcon = {
                                    Icon(
                                        painter = painterResource(id = item.logo),
                                        contentDescription = null,
                                        tint = item.color,
                                        modifier = Modifier.size(24.dp)
                                    )
                                },
                                onClick = {
                                    onCategoryChange(item)
                                    categoryExpanded = false
                                }
                            )
                        }
                }
            }
        }

        // Deskripsi
        OutlinedTextField(
            value = description,
            onValueChange = onDescriptionChange,
            label = { Text(if (type == TransactionType.Saving) "Nama Tabungan" else "Deskripsi") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next)
        )

        // Jumlah
        OutlinedTextField(
            value = amount,
            onValueChange = { 
                // Only allow digits
                if (it.all { char -> char.isDigit() }) {
                    onAmountChange(it)
                }
            },
            label = { Text("Jumlah") },
            leadingIcon = {
                Text(
                    text = "Rp",
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(start = 8.dp)
                )
            },
            visualTransformation = RupiahVisualTransformation(),
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Number,
                imeAction = ImeAction.Done
            ),
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )
    }
}

class RupiahVisualTransformation : VisualTransformation {
    override fun filter(text: AnnotatedString): TransformedText {
        val rawText = text.text
        if (rawText.isEmpty()) {
            return TransformedText(text, OffsetMapping.Identity)
        }

        val formatted = try {
            val parsed = rawText.toLongOrNull() ?: 0L
            val formatter = NumberFormat.getInstance(Locale.forLanguageTag("id-ID"))
            formatter.format(parsed)
        } catch (_: Exception) {
            rawText
        }

        val annotatedString = AnnotatedString(formatted)

        val offsetMapping = object : OffsetMapping {
            override fun originalToTransformed(offset: Int): Int {
                if (offset <= 0) return 0
                val originalBefore = rawText.substring(0, offset)
                val formattedBefore = try {
                    val parsed = originalBefore.toLongOrNull() ?: 0L
                    val formatter = NumberFormat.getInstance(Locale.forLanguageTag("id-ID"))
                    formatter.format(parsed)
                } catch (_: Exception) {
                    originalBefore
                }
                return formattedBefore.length
            }

            override fun transformedToOriginal(offset: Int): Int {
                val transformedBefore = formatted.substring(0, offset.coerceAtMost(formatted.length))
                return transformedBefore.count { it.isDigit() }
            }
        }

        return TransformedText(annotatedString, offsetMapping)
    }
}

@Composable
private fun TransactionTypeOption(
    label: String,
    selected: Boolean,
    onSelect: () -> Unit,
    modifier: Modifier = Modifier
) {
    val containerColor = if (selected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface
    val contentColor = if (selected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface
    val borderColor = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline

    Surface(
        onClick = onSelect,
        modifier = modifier.height(40.dp),
        shape = RoundedCornerShape(5.dp),
        color = containerColor,
        contentColor = contentColor,
        border = androidx.compose.foundation.BorderStroke(1.dp, borderColor)
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center,

        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun AddTransactionDialogPreview() {
    MaterialTheme {
        Surface(
            modifier = Modifier.padding(10.dp),
            shape = RoundedCornerShape(5.dp),
            tonalElevation = 6.dp
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "Tambah Transaksi",
                    style = MaterialTheme.typography.headlineSmall
                )
                AddTransactionForm(
                    description = "",
                    onDescriptionChange = {},
                    amount = "",
                    onAmountChange = {},
                    type = TransactionType.EXPENSE,
                    onTypeChange = {},
                    category = Category.OTHER,
                    onCategoryChange = {}
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(onClick = {}) {
                        Text("Batal")
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(onClick = {}) {
                        Text("Simpan")
                    }
                }
            }
        }
    }
}
