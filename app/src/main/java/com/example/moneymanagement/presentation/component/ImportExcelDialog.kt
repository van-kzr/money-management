package com.example.moneymanagement.presentation.component

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.moneymanagement.domain.model.Category
import com.example.moneymanagement.domain.model.ParsedTransactionRow
import com.example.moneymanagement.domain.model.RowValidationError
import com.example.moneymanagement.domain.model.TransactionType
import com.example.moneymanagement.domain.usecase.ImportTransactionsUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ImportExcelDialog(
    onDismiss: () -> Unit,
    onImportConfirmed: (List<ParsedTransactionRow>) -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val useCase = remember { ImportTransactionsUseCase() }

    var currentStep by remember { mutableIntStateOf(0) } // 0: Select File, 1: Mapping, 2: Preview & Validation
    var selectedUri by remember { mutableStateOf<Uri?>(null) }
    var fileName by remember { mutableStateOf("") }
    
    var excelHeaders by remember { mutableStateOf<List<String>>(emptyList()) }
    val requiredFields = listOf("Tanggal", "Deskripsi", "Tipe", "Kategori", "Jumlah")
    var columnMapping by remember { mutableStateOf<Map<String, Int>>(emptyMap()) }

    var validTransactions by remember { mutableStateOf<List<ParsedTransactionRow>>(emptyList()) }
    var validationErrors by remember { mutableStateOf<List<RowValidationError>>(emptyList()) }
    var isLoading by remember { mutableStateOf(false) }

    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent(),
        onResult = { uri ->
            if (uri != null) {
                selectedUri = uri
                fileName = "Excel File Selected"
                isLoading = true
                scope.launch(Dispatchers.IO) {
                    try {
                        context.contentResolver.openInputStream(uri)?.use { inputStream ->
                            var errorMsg = "Gagal membaca header file. Pastikan file tidak rusak."
                            val headers = try {
                                useCase.extractHeaders(inputStream)
                            } catch (e: Exception) {
                                android.util.Log.e("ImportExcelDialog", "Error extractHeaders", e)
                                errorMsg = e.localizedMessage ?: errorMsg
                                emptyList()
                            }
                            val initialMapping = useCase.detectInitialMapping(headers)
                            withContext(Dispatchers.Main) {
                                if (headers.isNotEmpty()) {
                                    excelHeaders = headers
                                    columnMapping = initialMapping
                                    currentStep = 1
                                } else {
                                    android.widget.Toast.makeText(context, errorMsg, android.widget.Toast.LENGTH_LONG).show()
                                }
                            }
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    } finally {
                        withContext(Dispatchers.Main) { isLoading = false }
                    }
                }
            }
        }
    )

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .fillMaxHeight(0.85f),
            shape = RoundedCornerShape(12.dp),
            color = MaterialTheme.colorScheme.surface
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                // Header Dialog
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Import Data Excel",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close")
                    }
                }

                Divider()

                // Step Content Box
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                    } else {
                        when (currentStep) {
                            0 -> SelectFileStep(onSelectFileClick = { filePickerLauncher.launch("*/*") })
                            1 -> MappingColumnStep(
                                requiredFields = requiredFields,
                                excelHeaders = excelHeaders,
                                columnMapping = columnMapping,
                                onMappingChanged = { field, index ->
                                    columnMapping = columnMapping.toMutableMap().apply { put(field, index) }
                                }
                            )
                            2 -> PreviewAndValidationStep(
                                validTransactions = validTransactions,
                                validationErrors = validationErrors
                            )
                        }
                    }
                }

                Divider()

                // Action Footer Bottom Buttons
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (currentStep > 0) {
                        TextButton(
                            onClick = { currentStep-- },
                            modifier = Modifier.padding(end = 8.dp)
                        ) {
                            Text("Kembali")
                        }
                    }

                    if (currentStep == 1) {
                        Button(
                            onClick = {
                                val uri = selectedUri ?: return@Button
                                isLoading = true
                                scope.launch(Dispatchers.IO) {
                                    try {
                                        context.contentResolver.openInputStream(uri)?.use { inputStream ->
                                            val (valid, errs) = useCase.parseAndValidate(inputStream, columnMapping)
                                            withContext(Dispatchers.Main) {
                                                validTransactions = valid
                                                validationErrors = errs
                                                currentStep = 2
                                            }
                                        }
                                    } catch (e: Exception) {
                                        e.printStackTrace()
                                    } finally {
                                        withContext(Dispatchers.Main) { isLoading = false }
                                    }
                                }
                            },
                            shape = RoundedCornerShape(5.dp)
                        ) {
                            Text("Validasi & Lanjut")
                        }
                    } else if (currentStep == 2) {
                        Button(
                            onClick = {
                                onImportConfirmed(validTransactions)
                                onDismiss()
                            },
                            enabled = validTransactions.isNotEmpty(),
                            shape = RoundedCornerShape(5.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50))
                        ) {
                            Text("Import (${validTransactions.size} Transaksi)")
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SelectFileStep(onSelectFileClick: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Silakan pilih file Excel (.xlsx) data keuangan Anda untuk memulai proses import.",
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(bottom = 24.dp)
        )
        Button(
            onClick = onSelectFileClick,
            shape = RoundedCornerShape(5.dp)
        ) {
            Text("Pilih File Excel")
        }
    }
}

@Composable
private fun MappingColumnStep(
    requiredFields: List<String>,
    excelHeaders: List<String>,
    columnMapping: Map<String, Int>,
    onMappingChanged: (String, Int) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        Text(
            text = "Cocokkan kolom aplikasi dengan header kolom yang terdeteksi di file Excel Anda:",
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(bottom = 16.dp),
            fontWeight = FontWeight.Medium
        )

        requiredFields.forEach { field ->
            var expanded by remember { mutableStateOf(false) }
            val selectedIdx = columnMapping[field] ?: -1
            val selectedHeaderLabel = if (selectedIdx in excelHeaders.indices) excelHeaders[selectedIdx] else "Pilih Kolom..."

            Column(modifier = Modifier.padding(vertical = 8.dp)) {
                Text(
                    text = "$field *",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 4.dp)
                )
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(4.dp))
                        .clickable { expanded = true }
                        .padding(horizontal = 12.dp, vertical = 12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = selectedHeaderLabel, style = MaterialTheme.typography.bodyMedium)
                        Icon(Icons.Default.ArrowDropDown, contentDescription = null)
                    }

                    DropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false },
                        modifier = Modifier.fillMaxWidth(0.85f)
                    ) {
                        excelHeaders.forEachIndexed { index, header ->
                            DropdownMenuItem(
                                text = { Text(header) },
                                onClick = {
                                    onMappingChanged(field, index)
                                    expanded = false
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun PreviewAndValidationStep(
    validTransactions: List<ParsedTransactionRow>,
    validationErrors: List<RowValidationError>
) {
    var selectedTab by remember { mutableIntStateOf(0) } // 0: Preview Valid, 1: Error List
    val currencyFormatter = remember { NumberFormat.getCurrencyInstance(Locale("in", "ID")) }
    val dateFormat = remember { SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()) }

    Column(modifier = Modifier.fillMaxSize()) {
        TabRow(selectedTabIndex = selectedTab) {
            Tab(
                selected = selectedTab == 0,
                onClick = { selectedTab = 0 },
                text = { Text("Valid (${validTransactions.size})") }
            )
            Tab(
                selected = selectedTab == 1,
                onClick = { selectedTab = 1 },
                text = { Text("Error (${validationErrors.size})") }
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        if (selectedTab == 0) {
            if (validTransactions.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("Tidak ada baris transaksi valid yang ditemukan.")
                }
            } else {
                LazyColumn(modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(validTransactions) { item ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text(text = item.description, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
                                    NominalText(
                                        amountText = currencyFormatter.format(item.amount).replace("Rp", "Rp "),
                                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                        color = if (item.type == TransactionType.INCOME) Color(0xFF4CAF50) else Color.Red
                                    )
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text(text = "Baris Excel: ${item.rowIndex} | Kategori: ${item.category.categoryName}", style = MaterialTheme.typography.labelSmall)
                                    Text(text = dateFormat.format(item.date), style = MaterialTheme.typography.labelSmall)
                                }
                            }
                        }
                    }
                }
            }
        } else {
            if (validationErrors.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("Bersih! Tidak ada data error yang terdeteksi.")
                }
            } else {
                LazyColumn(modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(validationErrors) { err ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)
                        ) {
                            Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                                Column {
                                    Text(
                                        text = "Baris Excel ${err.rowIndex}",
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onErrorContainer,
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = err.errorMessage,
                                        color = MaterialTheme.colorScheme.onErrorContainer,
                                        style = MaterialTheme.typography.bodySmall
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ImportExcelDialogPreview() {
    MaterialTheme {
        PreviewAndValidationStep(
            validTransactions = listOf(
                ParsedTransactionRow(
                    rowIndex = 2,
                    description = "Gaji Masuk",
                    amount = 5000000.0,
                    type = TransactionType.INCOME,
                    category = Category.SALARY,
                    date = Date()
                ),
                ParsedTransactionRow(
                    rowIndex = 3,
                    description = "Makan Siang Bakso",
                    amount = 35000.0,
                    type = TransactionType.EXPENSE,
                    category = Category.FOOD,
                    date = Date()
                )
            ),
            validationErrors = listOf(
                RowValidationError(
                    rowIndex = 4,
                    errorMessage = "Format tanggal tidak valid / tidak dikenal"
                )
            )
        )
    }
}
