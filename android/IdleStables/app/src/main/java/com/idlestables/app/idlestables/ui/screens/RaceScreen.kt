package com.idlestables.app.idlestables.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.idlestables.core.demo.DemoRepository

@Composable
fun RaceScreen(repo: DemoRepository, raceId: String) {
    val state by repo.state.collectAsState()
    val result = state.results.find { it.raceId == raceId }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text("Race", style = MaterialTheme.typography.titleLarge)
        Text(raceId, style = MaterialTheme.typography.bodyMedium)

        Card {
            Column(Modifier.fillMaxWidth().padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Photo Finish", style = MaterialTheme.typography.titleMedium)
                Text("(placeholder)")
            }
        }

        if (result != null) {
            Card {
                Column(Modifier.fillMaxWidth().padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text("Top 3", style = MaterialTheme.typography.titleMedium)
                    result.top3.forEach { p ->
                        Text("#${p.position}: ${p.horseId}")
                    }
                    Text("Purse won (winner): ${result.purseWon}")
                }
            }
        } else {
            Text("No result yet (fill the field to auto-resolve in demo).", style = MaterialTheme.typography.bodySmall)
        }
    }
}
