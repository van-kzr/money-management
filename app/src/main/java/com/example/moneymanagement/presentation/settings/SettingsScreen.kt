package com.example.moneymanagement.presentation.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun SettingsScreen(
    isDarkMode: Boolean = false,
    onToggleDarkMode: () -> Unit = {},
    onImportClick: () -> Unit = {},
    onExportClick: () -> Unit = {},
    onClearClick: () -> Unit = {}
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
    ) {
        // Header Pengaturan
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.Center
        ) {
            Text(
                text = "Pengaturan",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
        }

        // Settings Groups
        SettingsGroup(title = "Umum") {
            SettingsItem(icon = Icons.Default.Category, title = "Kategori")
            SettingsItem(icon = Icons.Default.Notifications, title = "Notifikasi")
            SettingsItem(icon = Icons.Default.Security, title = "Keamanan")
        }

        SettingsGroup(title = "Data & Penyimpanan") {
            SettingsItem(icon = Icons.Default.CloudUpload, title = "Import dari Excel", onClick = onImportClick)
            SettingsItem(icon = Icons.Default.CloudDownload, title = "Export ke Excel", onClick = onExportClick)
            SettingsItem(
                icon = Icons.Default.DeleteSweep, 
                title = "Bersihkan Data Transaksi", 
                titleColor = MaterialTheme.colorScheme.error,
                onClick = onClearClick
            )
        }

        SettingsGroup(title = "Aplikasi") {
            SettingsItem(
                icon = if (isDarkMode) Icons.Default.DarkMode else Icons.Default.LightMode, 
                title = "Mode Malam",
                trailing = {
                    Switch(
                        checked = isDarkMode,
                        onCheckedChange = { onToggleDarkMode() }
                    )
                }
            )
            SettingsItem(icon = Icons.Default.Palette, title = "Tampilan")
            SettingsItem(icon = Icons.Default.Help, title = "Bantuan & Dukungan")
            SettingsItem(icon = Icons.Default.Info, title = "Tentang Aplikasi")
        }
        
        Spacer(modifier = Modifier.height(32.dp))
    }
}

@Composable
private fun SettingsGroup(title: String, content: @Composable ColumnScope.() -> Unit) {
    Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
        Text(
            text = title,
            style = MaterialTheme.typography.labelMedium,
            color = Color.Gray,
            modifier = Modifier.padding(start = 8.dp, bottom = 8.dp)
        )
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
        ) {
            Column(content = content)
        }
    }
}

@Composable
private fun SettingsItem(
    icon: ImageVector,
    title: String,
    titleColor: Color = Color.Unspecified,
    trailing: @Composable (() -> Unit)? = null,
    onClick: () -> Unit = {}
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(8.dp)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                icon, 
                contentDescription = null, 
                tint = if (titleColor == MaterialTheme.colorScheme.error) titleColor else MaterialTheme.colorScheme.onSurfaceVariant, 
                modifier = Modifier.size(20.dp)
            )
        }
        Spacer(modifier = Modifier.width(16.dp))
        Text(
            text = title, 
            style = MaterialTheme.typography.bodyLarge, 
            color = if (titleColor != Color.Unspecified) titleColor else MaterialTheme.colorScheme.onSurface
        )
        Spacer(modifier = Modifier.weight(1f))
        if (trailing != null) {
            trailing()
        } else {
            Icon(Icons.Default.ChevronRight, contentDescription = null, tint = MaterialTheme.colorScheme.outline, modifier = Modifier.size(20.dp))
        }
    }
}

@Preview(showBackground = true)
@Composable
fun SettingsPreview() {
    com.example.moneymanagement.ui.theme.MoneyManagementTheme {
        SettingsScreen()
    }
}
