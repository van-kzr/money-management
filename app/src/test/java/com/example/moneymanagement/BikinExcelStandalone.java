package com.example.moneymanagement;

import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.poi.xssf.usermodel.XSSFRow;
import java.io.FileOutputStream;
import java.io.File;

public class BikinExcelStandalone {
    public static void main(String[] args) throws Exception {
        XSSFWorkbook workbook = new XSSFWorkbook();
        XSSFSheet sheet = workbook.createSheet("Transaksi");
        
        String[] headers = {"Tanggal", "Deskripsi", "Tipe", "Kategori", "Jumlah"};
        XSSFRow headerRow = sheet.createRow(0);
        for (int i = 0; i < headers.length; i++) {
            headerRow.createCell(i).setCellValue(headers[i]);
        }
        
        String[][] rows = {
            {"15/09/2026 09:00", "Gaji Bulanan Utama", "Pemasukan", "Gaji", "7500000"},
            {"15/09/2026 12:30", "Makan Siang Nasi Padang", "Pengeluaran", "Makanan", "45000"},
            {"15/09/2026 15:00", "Bonus Projek Sampingan", "Pemasukan", "Bonus", "2000000"},
            {"16/09/2026 19:00", "Belanja Bulanan Supermarket", "Pengeluaran", "Belanja", "850000"},
            {"16/09/2026 21:00", "Beli Bensin Motor", "Pengeluaran", "Transportasi", "50000"},
            {"17/09/2026 08:30", "Bayar Tagihan Listrik", "Pengeluaran", "Tagihan", "320000"},
            {"17/09/2026 10:00", "Beli Obat Di Apotek", "Pengeluaran", "Kesehatan", "65000"},
            {"Format Tanggal Salah", "Makan Malam Mewah", "Pengeluaran", "Makanan", "250000"},
            {"18/09/2026 14:00", "Beli Kopi Kekinian", "TipeTidakValid", "Makanan", "35000"},
            {"18/09/2026 16:00", "Beli Cemilan Sore", "Pengeluaran", "Makanan", "BukanAngkaNominal"}
        };
        
        for (int i = 0; i < rows.length; i++) {
            XSSFRow row = sheet.createRow(i + 1);
            for (int j = 0; j < rows[i].length; j++) {
                row.createCell(j).setCellValue(rows[i][j]);
            }
        }
        
        FileOutputStream fileOutputStream = new FileOutputStream("/home/vankzr/Download_Data_Transaksi_Contoh.xlsx");
        workbook.write(fileOutputStream);
        fileOutputStream.close();
        workbook.close();
        
        System.out.println("Excel file created successfully!");
    }
}
