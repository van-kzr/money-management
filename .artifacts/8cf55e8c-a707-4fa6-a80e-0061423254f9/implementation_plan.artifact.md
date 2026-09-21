# Implementasi Room Database untuk Transaksi dan Tabungan

Mengintegrasikan Room Database dan SQLite untuk menyimpan data transaksi (`Transaction`) dan tabungan (`Saving`) secara persisten, menggantikan penyimpanan *in-memory* yang ada saat ini.

## User Review Required

> [!IMPORTANT]
> - Data yang saat ini tersimpan di *memory* akan hilang setelah aplikasi ditutup karena kita beralih ke Database.
> - Saya akan menambahkan plugin KSP untuk pemrosesan anotasi Room, yang memerlukan sinkronisasi Gradle.

## Proposed Changes

### [Component] Data Layer (Room Setup)

#### [MODIFY] [libs.versions.toml](file:///home/vankzr/project/mobile/money%20management/gradle/libs.versions.toml)
Menambahkan dependensi Room dan plugin KSP.

#### [MODIFY] [build.gradle.kts (app)](file:///home/vankzr/project/mobile/money%20management/app/build.gradle.kts)
Menerapkan plugin KSP dan menambahkan library Room ke `dependencies`.

#### [NEW] [TransactionEntity.kt](file:///home/vankzr/project/mobile/money%20management/app/src/main/java/com/example/moneymanagement/data/local/entity/TransactionEntity.kt)
Mendefinisikan entitas Room untuk data Transaksi.

#### [NEW] [SavingEntity.kt](file:///home/vankzr/project/mobile/money%20management/app/src/main/java/com/example/moneymanagement/data/local/entity/SavingEntity.kt)
Mendefinisikan entitas Room untuk data Tabungan.

#### [NEW] [Converters.kt](file:///home/vankzr/project/mobile/money%20management/app/src/main/java/com/example/moneymanagement/data/local/Converters.kt)
TypeConverters untuk tipe data `Date`, `TransactionType`, dan `Category`.

#### [NEW] [MoneyDao.kt](file:///home/vankzr/project/mobile/money%20management/app/src/main/java/com/example/moneymanagement/data/local/MoneyDao.kt)
Mendefinisikan interface DAO untuk operasi CRUD pada database.

#### [NEW] [MoneyDatabase.kt](file:///home/vankzr/project/mobile/money%20management/app/src/main/java/com/example/moneymanagement/data/local/MoneyDatabase.kt)
Kelas abstrak `RoomDatabase` utama.

#### [NEW] [MoneyMapper.kt](file:///home/vankzr/project/mobile/money%20management/app/src/main/java/com/example/moneymanagement/data/mapper/MoneyMapper.kt)
Fungsi ekstensi untuk memetakan antara *Domain Model* dan *Data Entity*.

### [Component] Repository implementation

#### [MODIFY] [MoneyRepositoryImpl.kt](file:///home/vankzr/project/mobile/money%20management/app/src/main/java/com/example/moneymanagement/data/repository/MoneyRepositoryImpl.kt)
Mengubah implementasi agar menggunakan `MoneyDao` daripada `MutableStateFlow`.

### [Component] Initialization

#### [MODIFY] [MainActivity.kt](file:///home/vankzr/project/mobile/money%20management/app/src/main/java/com/example/moneymanagement/MainActivity.kt)
Menginisialisasi Room Database dan meneruskan DAO ke repository.

## Verification Plan

### Manual Verification
1. Jalankan aplikasi.
2. Tambahkan beberapa transaksi dan tabungan.
3. Tutup aplikasi sepenuhnya (kill process).
4. Buka kembali aplikasi dan pastikan data masih ada.
5. Verifikasi perhitungan saldo total, pemasukan, dan pengeluaran tetap akurat.
