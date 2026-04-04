package com.idlestables.app.idlestables.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.idlestables.app.idlestables.demo.DemoRepository
import com.idlestables.app.idlestables.model.SilksPattern
import com.idlestables.app.idlestables.model.SilksProfile

@Composable
fun SilksScreen(repo: DemoRepository) {
    val state by repo.state.collectAsState()

    var primary by remember(state.silks.primaryColor) { mutableStateOf(state.silks.primaryColor) }
    var secondary by remember(state.silks.secondaryColor) { mutableStateOf(state.silks.secondaryColor ?: "") }
    var pattern by remember(state.silks.pattern) { mutableStateOf(state.silks.pattern) }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text("Silks", style = MaterialTheme.typography.titleLarge)

        Card {
            Column(Modifier.fillMaxWidth().padding(12.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("Wallet-wide silks (updates all horses)", style = MaterialTheme.typography.titleMedium)

                OutlinedTextField(
                    value = primary,
                    onValueChange = { primary = it },
                    label = { Text("Primary color (hex)") },
                    singleLine = true,
                )

                OutlinedTextField(
                    value = secondary,
                    onValueChange = { secondary = it },
                    label = { Text("Secondary color (hex, optional)") },
                    singleLine = true,
                )

                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    SilksPattern.entries.forEach { p ->
                        FilterChip(
                            selected = pattern == p,
                            onClick = { pattern = p },
                            label = { Text(p.name) }
                        )
                    }
                }

                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    Button(onClick = {
                        repo.updateSilks(
                            SilksProfile(
                                pattern = pattern,
                                primaryColor = primary,
                                secondaryColor = secondary.takeIf { it.isNotBlank() }
                            )
                        )
                    }) {
                        Text("Save")
                    }
                }
            }
        }
    }
}
