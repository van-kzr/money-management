package com.example.moneymanagement.presentation.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.moneymanagement.domain.model.Saving
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Locale

@Composable
fun SavingItem(saving: Saving, formatter: NumberFormat) {
    val dateFormatter = remember { SimpleDateFormat("dd MMM yyyy", Locale("in", "ID")) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(5.dp)
    ) {
        Row(
            modifier = Modifier.padding(12.dp).fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(MaterialTheme.colorScheme.tertiary.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(id = com.example.moneymanagement.R.drawable.other),
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.tertiary,
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(text = saving.name, fontWeight = FontWeight.Bold)
                Text(
                    text = dateFormatter.format(saving.date),
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray
                )
            }

            NominalText(
                amountText = formatter.format(saving.amount).replace("Rp", "Rp "),
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.tertiary
            )
        }
    }
}
