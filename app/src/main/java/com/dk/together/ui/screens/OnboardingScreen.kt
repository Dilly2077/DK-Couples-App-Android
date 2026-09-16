package com.dk.together.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import java.time.LocalDate

@Composable
fun OnboardingScreen(onSave: (String, String, LocalDate) -> Unit) {
    var you by remember { mutableStateOf("") }
    var partner by remember { mutableStateOf("") }
    var dateText by remember { mutableStateOf(LocalDate.now().minusMonths(10).toString()) }
    var error by remember { mutableStateOf<String?>(null) }

    Column(
        modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 24.dp, vertical = 56.dp),
        verticalArrangement = Arrangement.Center
    ) {
        Text("DK Together", style = MaterialTheme.typography.displaySmall, fontWeight = FontWeight.Black)
        Text("A private little world for two.", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(Modifier.height(32.dp))

        OutlinedTextField(
            value = you,
            onValueChange = { you = it },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Your name") },
            singleLine = true
        )
        Spacer(Modifier.height(14.dp))
        OutlinedTextField(
            value = partner,
            onValueChange = { partner = it },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Partner's name") },
            singleLine = true
        )
        Spacer(Modifier.height(14.dp))
        OutlinedTextField(
            value = dateText,
            onValueChange = { dateText = it },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Relationship start date (YYYY-MM-DD)") },
            singleLine = true
        )
        error?.let {
            Spacer(Modifier.height(8.dp))
            Text(it, color = MaterialTheme.colorScheme.error)
        }
        Spacer(Modifier.height(24.dp))
        Button(
            onClick = {
                val parsed = runCatching { LocalDate.parse(dateText.trim()) }.getOrNull()
                error = when {
                    you.isBlank() -> "Enter your name."
                    partner.isBlank() -> "Enter your partner's name."
                    parsed == null -> "Use a valid date in YYYY-MM-DD format."
                    parsed.isAfter(LocalDate.now()) -> "The relationship start date cannot be in the future."
                    else -> null
                }
                if (error == null) onSave(you, partner, parsed!!)
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Create our space")
        }
        Spacer(Modifier.height(12.dp))
        Text(
            "This first build stores relationship data locally on this device. Partner sync is scaffolded for a later release.",
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            style = MaterialTheme.typography.bodySmall
        )
    }
}
