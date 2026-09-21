package com.example.moneymanagement

import com.example.moneymanagement.domain.usecase.ImportTransactionsUseCase
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream

class ImportTransactionsUseCaseTest {

    @Test
    fun create_sample_excel_file_for_user_on_device() {
        val workbook = XSSFWorkbook()
        val sheet = workbook.createSheet("Transaksi")
        
        val headers = listOf("Tanggal", "Deskripsi", "Tipe", "Kategori", "Jumlah")
        val headerRow = sheet.createRow(0)
        headers.forEachIndexed { index, header ->
            headerRow.createCell(index).setCellValue(header)
        }
        
        val rows = listOf(
            listOf("15/09/2026 09:00", "Gaji Bulanan Utama", "Pemasukan", "Gaji", "7500000"),
            listOf("15/09/2026 12:30", "Makan Siang Nasi Padang", "Pengeluaran", "Makanan", "45000"),
            listOf("15/09/2026 15:00", "Bonus Projek Sampingan", "Pemasukan", "Bonus", "2000000"),
            listOf("16/09/2026 19:00", "Belanja Bulanan Supermarket", "Pengeluaran", "Belanja", "850000"),
            listOf("16/09/2026 21:00", "Beli Bensin Motor", "Pengeluaran", "Transportasi", "50000"),
            listOf("17/09/2026 08:30", "Bayar Tagihan Listrik", "Pengeluaran", "Tagihan", "320000"),
            listOf("17/09/2026 10:00", "Beli Obat Di Apotek", "Pengeluaran", "Kesehatan", "65000"),
            // Simulated validation errors to demonstrate split tab features
            listOf("Format Tanggal Salah", "Makan Malam Mewah", "Pengeluaran", "Makanan", "250000"),
            listOf("18/09/2026 14:00", "Beli Kopi Kekinian", "TipeTidakValid", "Makanan", "35000"),
            listOf("18/09/2026 16:00", "Beli Cemilan Sore", "Pengeluaran", "Makanan", "BukanAngkaNominal")
        )
        
        rows.forEachIndexed { rowIndex, rowData ->
            val row = sheet.createRow(rowIndex + 1)
            rowData.forEachIndexed { colIndex, cellValue ->
                row.createCell(colIndex).setCellValue(cellValue)
            }
        }
        
        val fileOutputStream = java.io.FileOutputStream("/home/vankzr/Download_Data_Transaksi_Contoh.xlsx")
        workbook.write(fileOutputStream)
        fileOutputStream.close()
        workbook.close()
        
        val file = java.io.File("/home/vankzr/Download_Data_Transaksi_Contoh.xlsx")
        assertTrue(file.exists())
    }
}
