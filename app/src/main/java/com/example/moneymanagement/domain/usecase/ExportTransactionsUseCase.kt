package com.example.moneymanagement.domain.usecase

import com.example.moneymanagement.domain.model.Transaction
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import java.io.OutputStream
import java.text.SimpleDateFormat
import java.util.Locale

class ExportTransactionsUseCase {
    operator fun invoke(transactions: List<Transaction>, outputStream: OutputStream) {
        val workbook = XSSFWorkbook()
        val sheet = workbook.createSheet("Transaksi")
        
        // Header
        val headerRow = sheet.createRow(0)
        headerRow.createCell(0).setCellValue("Tanggal")
        headerRow.createCell(1).setCellValue("Deskripsi")
        headerRow.createCell(2).setCellValue("Tipe")
        headerRow.createCell(3).setCellValue("Kategori")
        headerRow.createCell(4).setCellValue("Jumlah")
        
        val dateFormat = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
        
        // Data
        transactions.forEachIndexed { index, transaction ->
            val row = sheet.createRow(index + 1)
            row.createCell(0).setCellValue(dateFormat.format(transaction.date))
            row.createCell(1).setCellValue(transaction.description)
            row.createCell(2).setCellValue(transaction.type.name)
            row.createCell(3).setCellValue(transaction.category.categoryName)
            row.createCell(4).setCellValue(transaction.amount)
        }
        
        workbook.write(outputStream)
        workbook.close()
        outputStream.close()
    }
}
