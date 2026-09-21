package com.example.moneymanagement.domain.usecase

import com.example.moneymanagement.domain.model.Category
import com.example.moneymanagement.domain.model.ParsedTransactionRow
import com.example.moneymanagement.domain.model.RowValidationError
import com.example.moneymanagement.domain.model.TransactionType
import com.opencsv.CSVReaderBuilder
import org.apache.poi.ss.usermodel.CellType
import org.apache.poi.ss.usermodel.WorkbookFactory
import java.io.File
import java.io.InputStream
import java.io.InputStreamReader
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ImportTransactionsUseCase {

    private fun isExcelFile(file: File): Boolean {
        return try {
            val bis = file.inputStream().buffered()
            val head = ByteArray(4)
            val n = bis.read(head)
            bis.close()
            if (n < 2) return false
            // Check for ZIP (OOXML) or OLE2
            (head[0] == 0x50.toByte() && head[1] == 0x4B.toByte()) || 
            (head[0] == 0xD0.toByte() && head[1] == 0xCF.toByte())
        } catch (e: Exception) {
            false
        }
    }

    fun extractHeaders(inputStream: InputStream): List<String> {
        val tempFile = File.createTempFile("import_headers", ".tmp")
        tempFile.deleteOnExit()
        inputStream.use { input ->
            tempFile.outputStream().use { output ->
                input.copyTo(output)
            }
        }

        if (isExcelFile(tempFile)) {
            return try {
                val workbook = WorkbookFactory.create(tempFile, null, true)
                val sheet = workbook.getSheetAt(0)
                val headerRow = sheet.getRow(0) ?: return emptyList()
                val headers = mutableListOf<String>()
                for (i in 0 until headerRow.lastCellNum) {
                    val cell = headerRow.getCell(i)
                    headers.add(cell?.toString()?.trim() ?: "")
                }
                workbook.close()
                tempFile.delete()
                headers
            } catch (e: Exception) {
                // Secondary fallback attempt if internal Android SAX driver conflicts still trigger namespace issue
                return try {
                    val workbook = WorkbookFactory.create(tempFile.inputStream())
                    val sheet = workbook.getSheetAt(0)
                    val headerRow = sheet.getRow(0) ?: return emptyList()
                    val headers = mutableListOf<String>()
                    for (i in 0 until headerRow.lastCellNum) {
                        val cell = headerRow.getCell(i)
                        headers.add(cell?.toString()?.trim() ?: "")
                    }
                    workbook.close()
                    tempFile.delete()
                    headers
                } catch (ex: Exception) {
                    tempFile.delete()
                    android.util.Log.e("ImportUseCase", "Excel secondary extract headers error", ex)
                    throw Exception("Gagal baca format biner Excel: ${ex.localizedMessage}")
                }
            }
        } else {
            // Try as CSV
            return try {
                val reader = InputStreamReader(tempFile.inputStream(), "UTF-8")
                val csvReader = com.opencsv.CSVReader(reader)
                val nextLine = csvReader.readNext()
                csvReader.close()
                tempFile.delete()
                nextLine?.map { it.trim() } ?: emptyList()
            } catch (e: Exception) {
                android.util.Log.e("ImportUseCase", "CSV extract headers error", e)
                tempFile.delete()
                throw Exception("Gagal baca CSV: ${e.localizedMessage}")
            }
        }
    }

    fun detectInitialMapping(headers: List<String>): Map<String, Int> {
        val mapping = mutableMapOf<String, Int>()
        val requiredFields = listOf("Tanggal", "Deskripsi", "Tipe", "Kategori", "Jumlah")
        
        requiredFields.forEach { field ->
            val index = headers.indexOfFirst { header -> 
                header.contains(field, ignoreCase = true) || 
                (field == "Tanggal" && header.contains("date", ignoreCase = true)) ||
                (field == "Deskripsi" && header.contains("desc", ignoreCase = true)) ||
                (field == "Tipe" && header.contains("type", ignoreCase = true)) ||
                (field == "Kategori" && header.contains("category", ignoreCase = true)) ||
                (field == "Jumlah" && header.contains("amount", ignoreCase = true))
            }
            if (index != -1) {
                mapping[field] = index
            }
        }
        return mapping
    }

    fun parseAndValidate(inputStream: InputStream, mapping: Map<String, Int>): Pair<List<ParsedTransactionRow>, List<RowValidationError>> {
        val tempFile = File.createTempFile("import_data", ".tmp")
        tempFile.deleteOnExit()
        inputStream.use { input ->
            tempFile.outputStream().use { output ->
                input.copyTo(output)
            }
        }

        val validRows = mutableListOf<ParsedTransactionRow>()
        val errors = mutableListOf<RowValidationError>()

        val dateIdx = mapping["Tanggal"] ?: -1
        val descIdx = mapping["Deskripsi"] ?: -1
        val typeIdx = mapping["Tipe"] ?: -1
        val catIdx = mapping["Kategori"] ?: -1
        val amountIdx = mapping["Jumlah"] ?: -1

        val dateFormats = listOf(
            SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()),
            SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()),
            SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()),
            SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        )

        if (isExcelFile(tempFile)) {
            try {
                val workbook = WorkbookFactory.create(tempFile, null, true)
                val sheet = workbook.getSheetAt(0)

                for (i in 1..sheet.lastRowNum) {
                    val row = sheet.getRow(i) ?: continue
                    
                    var isRowEmpty = true
                    for (c in 0 until row.lastCellNum) {
                        val cell = row.getCell(c)
                        if (cell != null && cell.cellType != CellType.BLANK) {
                            isRowEmpty = false
                            break
                        }
                    }
                    if (isRowEmpty) continue

                    val rowNum = i + 1
                    val rowErrors = mutableListOf<String>()

                    // 1. Parse Date
                    var parsedDate: Date? = null
                    if (dateIdx != -1) {
                        val cell = row.getCell(dateIdx)
                        if (cell != null) {
                            if (cell.cellType == CellType.NUMERIC && org.apache.poi.ss.usermodel.DateUtil.isCellDateFormatted(cell)) {
                                parsedDate = cell.dateCellValue
                            } else {
                                val dateStr = cell.toString().trim()
                                for (format in dateFormats) {
                                    try {
                                        parsedDate = format.parse(dateStr)
                                        if (parsedDate != null) break
                                    } catch (e: Exception) { }
                                }
                            }
                        }
                    }
                    if (parsedDate == null) {
                        rowErrors.add("Format Tanggal tidak valid / kosong")
                    }

                    // 2. Parse Description
                    val descStr = if (descIdx != -1) row.getCell(descIdx)?.toString()?.trim() ?: "" else ""
                    if (descStr.isEmpty()) {
                        rowErrors.add("Deskripsi tidak boleh kosong")
                    }

                    // 3. Parse Type
                    var transactionType: TransactionType? = null
                    if (typeIdx != -1) {
                        val typeStr = row.getCell(typeIdx)?.toString()?.trim()?.uppercase() ?: ""
                        transactionType = when {
                            typeStr.contains("INCOME") || typeStr.contains("MASUK") || typeStr.contains("PEMASUKAN") -> TransactionType.INCOME
                            typeStr.contains("EXPENSE") || typeStr.contains("KELUAR") || typeStr.contains("PENGELUARAN") -> TransactionType.EXPENSE
                            typeStr.contains("SAVING") || typeStr.contains("TABUNG") -> TransactionType.Saving
                            typeStr.contains("WITHDRAW") -> TransactionType.WITHDRAW_SAVING
                            else -> null
                        }
                    }
                    if (transactionType == null) {
                        rowErrors.add("Tipe Transaksi tidak valid (Gunakan: Pemasukan / Pengeluaran)")
                    }

                    // 4. Parse Category
                    var category: Category = Category.OTHER
                    if (catIdx != -1) {
                        val catStr = row.getCell(catIdx)?.toString()?.trim() ?: ""
                        if (catStr.isNotEmpty()) {
                            val matchedCat = Category.values().find { 
                                it.categoryName.equals(catStr, ignoreCase = true) || it.name.equals(catStr, ignoreCase = true)
                            }
                            if (matchedCat != null) {
                                category = matchedCat
                            }
                        }
                    }

                    // 5. Parse Amount
                    var amountValue = 0.0
                    if (amountIdx != -1) {
                        val cell = row.getCell(amountIdx)
                        if (cell != null) {
                            try {
                                if (cell.cellType == CellType.NUMERIC) {
                                    amountValue = cell.numericCellValue
                                } else {
                                    val amtStr = cell.toString().trim().replace(",", "").replace(".00", "")
                                    amountValue = amtStr.toDouble()
                                }
                                if (amountValue <= 0) {
                                    rowErrors.add("Jumlah harus lebih besar dari 0")
                                }
                            } catch (e: Exception) {
                                rowErrors.add("Jumlah harus berupa angka")
                            }
                        } else {
                            rowErrors.add("Jumlah tidak boleh kosong")
                        }
                    } else {
                        rowErrors.add("Kolom Jumlah tidak terpetakan")
                    }

                    if (rowErrors.isNotEmpty()) {
                        errors.add(RowValidationError(rowNum, rowErrors.joinToString(", ")))
                    } else {
                        validRows.add(
                            ParsedTransactionRow(
                                rowIndex = rowNum,
                                description = descStr,
                                amount = amountValue,
                                type = transactionType!!,
                                category = category,
                                date = parsedDate!!
                            )
                        )
                    }
                }
                workbook.close()
            } catch (e: Exception) {
                // Secondary fallback data parser
                try {
                    val workbook = WorkbookFactory.create(tempFile.inputStream())
                    val sheet = workbook.getSheetAt(0)

                    for (i in 1..sheet.lastRowNum) {
                        val row = sheet.getRow(i) ?: continue
                        var isRowEmpty = true
                        for (c in 0 until row.lastCellNum) {
                            val cell = row.getCell(c)
                            if (cell != null && cell.cellType != CellType.BLANK) { isRowEmpty = false; break }
                        }
                        if (isRowEmpty) continue

                        val rowNum = i + 1
                        val rowErrors = mutableListOf<String>()

                        var parsedDate: Date? = null
                        if (dateIdx != -1) {
                            val cell = row.getCell(dateIdx)
                            if (cell != null) {
                                if (cell.cellType == CellType.NUMERIC && org.apache.poi.ss.usermodel.DateUtil.isCellDateFormatted(cell)) {
                                    parsedDate = cell.dateCellValue
                                } else {
                                    val dateStr = cell.toString().trim()
                                    for (format in dateFormats) {
                                        try { parsedDate = format.parse(dateStr); if (parsedDate != null) break } catch (ex: Exception) {}
                                    }
                                }
                            }
                        }
                        if (parsedDate == null) rowErrors.add("Tanggal tidak valid")

                        val descStr = if (descIdx != -1) row.getCell(descIdx)?.toString()?.trim() ?: "" else ""
                        if (descStr.isEmpty()) rowErrors.add("Deskripsi kosong")

                        var transactionType: TransactionType? = null
                        if (typeIdx != -1) {
                            val typeStr = row.getCell(typeIdx)?.toString()?.trim()?.uppercase() ?: ""
                            transactionType = when {
                                typeStr.contains("INCOME") || typeStr.contains("MASUK") || typeStr.contains("PEMASUKAN") -> TransactionType.INCOME
                                typeStr.contains("EXPENSE") || typeStr.contains("KELUAR") || typeStr.contains("PENGELUARAN") -> TransactionType.EXPENSE
                                else -> TransactionType.INCOME
                            }
                        }

                        var category: Category = Category.OTHER
                        if (catIdx != -1) {
                            val catStr = row.getCell(catIdx)?.toString()?.trim() ?: ""
                            val matchedCat = Category.values().find { it.categoryName.equals(catStr, ignoreCase = true) }
                            if (matchedCat != null) category = matchedCat
                        }

                        var amountValue = 0.0
                        if (amountIdx != -1) {
                            val cell = row.getCell(amountIdx)
                            try {
                                amountValue = if (cell?.cellType == CellType.NUMERIC) cell.numericCellValue else cell.toString().trim().replace(",","").toDouble()
                            } catch(ex: Exception) { rowErrors.add("Jumlah bukan angka") }
                        }

                        if (rowErrors.isNotEmpty()) {
                            errors.add(RowValidationError(rowNum, rowErrors.joinToString(", ")))
                        } else {
                            validRows.add(ParsedTransactionRow(rowNum, descStr, amountValue, transactionType!!, category, parsedDate!!))
                        }
                    }
                    workbook.close()
                } catch (ex: Exception) {
                    android.util.Log.e("ImportUseCase", "Excel parse full data error", ex)
                    errors.add(RowValidationError(1, "Gagal proses Excel: ${ex.localizedMessage}"))
                }
            }
        } else {
            // Process as CSV
            try {
                val reader = InputStreamReader(tempFile.inputStream(), "UTF-8")
                val csvReader = CSVReaderBuilder(reader).build()
                val allRows = csvReader.readAll()
                csvReader.close()

                if (allRows.isNotEmpty()) {
                    for (i in 1 until allRows.size) {
                        val row = allRows[i]
                        if (row.all { it.isBlank() }) continue

                        val rowNum = i + 1
                        val rowErrors = mutableListOf<String>()

                        // 1. Parse Date
                        var parsedDate: Date? = null
                        if (dateIdx != -1 && dateIdx < row.size) {
                            val dateStr = row[dateIdx].trim()
                            for (format in dateFormats) {
                                try {
                                    parsedDate = format.parse(dateStr)
                                    if (parsedDate != null) break
                                } catch (e: Exception) {}
                            }
                        }
                        if (parsedDate == null) rowErrors.add("Tanggal tidak valid")

                        // 2. Parse Description
                        val descStr = if (descIdx != -1 && descIdx < row.size) row[descIdx].trim() else ""
                        if (descStr.isEmpty()) rowErrors.add("Deskripsi kosong")

                        // 3. Parse Type
                        var transactionType: TransactionType? = null
                        if (typeIdx != -1 && typeIdx < row.size) {
                            val typeStr = row[typeIdx].trim().uppercase()
                            transactionType = when {
                                typeStr.contains("INCOME") || typeStr.contains("MASUK") || typeStr.contains("PEMASUKAN") -> TransactionType.INCOME
                                typeStr.contains("EXPENSE") || typeStr.contains("KELUAR") || typeStr.contains("PENGELUARAN") -> TransactionType.EXPENSE
                                typeStr.contains("SAVING") || typeStr.contains("TABUNG") -> TransactionType.Saving
                                typeStr.contains("WITHDRAW") -> TransactionType.WITHDRAW_SAVING
                                else -> null
                            }
                        }
                        if (transactionType == null) rowErrors.add("Tipe tidak valid")

                        // 4. Parse Category
                        var category: Category = Category.OTHER
                        if (catIdx != -1 && catIdx < row.size) {
                            val catStr = row[catIdx].trim()
                            val matchedCat = Category.values().find { 
                                it.categoryName.equals(catStr, ignoreCase = true) || it.name.equals(catStr, ignoreCase = true)
                            }
                            if (matchedCat != null) category = matchedCat
                        }

                        // 5. Parse Amount
                        var amountValue = 0.0
                        if (amountIdx != -1 && amountIdx < row.size) {
                            try {
                                amountValue = row[amountIdx].trim().replace(",", "").toDouble()
                                if (amountValue <= 0) rowErrors.add("Jumlah <= 0")
                            } catch (e: Exception) {
                                rowErrors.add("Jumlah bukan angka")
                            }
                        } else rowErrors.add("Kolom Jumlah missing")

                        if (rowErrors.isNotEmpty()) {
                            errors.add(RowValidationError(rowNum, rowErrors.joinToString(", ")))
                        } else {
                            validRows.add(
                                ParsedTransactionRow(
                                    rowIndex = rowNum,
                                    description = descStr,
                                    amount = amountValue,
                                    type = transactionType!!,
                                    category = category,
                                    date = parsedDate!!
                                )
                            )
                        }
                    }
                }
            } catch (e: Exception) {
                errors.add(RowValidationError(1, "Gagal proses CSV: ${e.localizedMessage}"))
            }
        }

        tempFile.delete()
        return Pair(validRows, errors)
    }
}
