package com.idlestables.app.idlestables.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.idlestables.app.idlestables.demo.DemoRepository

@Composable
fun DashboardScreen(repo: DemoRepository) {
    val state by repo.state.collectAsState()

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text("Results", style = MaterialTheme.typography.titleLarge)
        Spacer(Modifier.height(12.dp))

        LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            items(state.results, key = { it.raceId }) { r ->
                Card {
                    Column(Modifier.fillMaxWidth().padding(12.dp)) {
                        Text("Race: ${r.raceId}", style = MaterialTheme.typography.titleMedium)
                        Text("Track: ${r.trackId}")
                        Text("Winner: ${r.winnerHorseId}")
                        Text("Top 3: ${r.top3.joinToString { "#${it.position}:${it.horseId}" }}")
                        Spacer(Modifier.height(8.dp))
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                            OutlinedButton(onClick = { /* demo */ }) { Text("Claim") }
                        }
                    }
                }
            }
        }
    }
}
