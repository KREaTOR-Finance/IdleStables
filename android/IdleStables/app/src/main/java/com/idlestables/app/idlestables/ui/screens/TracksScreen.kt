package com.idlestables.app.idlestables.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.idlestables.core.demo.DemoRepository

@Composable
fun TracksScreen(
    repo: DemoRepository,
    onOpenTrack: (String) -> Unit,
) {
    val state by repo.state.collectAsState()

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text("Tracks", style = MaterialTheme.typography.titleLarge)
        Spacer(Modifier.height(12.dp))

        LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            items(state.tracks, key = { it.id }) { t ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onOpenTrack(t.id) }
                ) {
                    Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(t.name, style = MaterialTheme.typography.titleMedium)
                        Text("Cadence: ${t.cadenceMinutes}m  Field: ${t.fieldSize}  Distance: ${t.distanceLabel}")
                    }
                }
            }
        }
    }
}
