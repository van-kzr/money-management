package com.example.moneymanagement.presentation.add_transaction

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.moneymanagement.domain.model.Category
import com.example.moneymanagement.domain.model.TransactionType

@Composable
fun CategoryPickerScreen(
    onBack: () -> Unit,
    onCategorySelected: (Category) -> Unit,
    initialType: TransactionType? = null
) {
    var selectedTypeFilter by remember { mutableStateOf(initialType) }

    val filteredCategories = remember(selectedTypeFilter) {
        if (selectedTypeFilter == null) {
            Category.entries
        } else {
            Category.entries.filter { it.type == null || it.type == selectedTypeFilter }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF8F9FA))
    ) {
        // Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null)
            }
            Text(text = "Pilih Kategori", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        }

        // Filter Chips
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            val filters = listOf(null to "Semua", TransactionType.INCOME to "Pemasukan", TransactionType.EXPENSE to "Pengeluaran")
            filters.forEach { (type, label) ->
                val selected = selectedTypeFilter == type
                Surface(
                    onClick = { selectedTypeFilter = type },
                    shape = RoundedCornerShape(20.dp),
                    color = if (selected) MaterialTheme.colorScheme.primary else Color.White,
                    border = if (selected) null else androidx.compose.foundation.BorderStroke(1.dp, Color.LightGray),
                    modifier = Modifier.height(40.dp).weight(1f)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(text = label, color = if (selected) Color.White else Color.Gray, style = MaterialTheme.typography.labelLarge)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // List
        LazyColumn(
            modifier = Modifier.fillMaxSize()
        ) {
            items(filteredCategories) { category ->
                CategoryListItem(category = category, onClick = { onCategorySelected(category) })
                HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp), color = Color.LightGray.copy(alpha = 0.3f))
            }
        }
    }
}

@Composable
private fun CategoryListItem(category: Category, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(category.color.copy(alpha = 0.1f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                painter = painterResource(id = category.logo),
                contentDescription = null,
                tint = category.color,
                modifier = Modifier.size(20.dp)
            )
        }
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(text = category.categoryName, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold)
            Text(
                text = when(category.type) {
                    TransactionType.INCOME -> "Pemasukan"
                    TransactionType.EXPENSE -> "Pengeluaran"
                    else -> "Lainnya"
                },
                style = MaterialTheme.typography.bodySmall,
                color = Color.Gray
            )
        }
        Icon(Icons.Default.ChevronRight, contentDescription = null, tint = Color.LightGray)
    }
}

@Preview(showBackground = true)
@Composable
fun CategoryPickerPreview() {
    com.example.moneymanagement.ui.theme.MoneyManagementTheme {
        CategoryPickerScreen(onBack = {}, onCategorySelected = {})
    }
}
