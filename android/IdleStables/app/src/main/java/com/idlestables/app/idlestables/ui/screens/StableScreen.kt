package com.idlestables.app.idlestables.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.idlestables.core.demo.DemoRepository
import com.idlestables.core.model.Horse

@Composable
fun StableScreen(repo: DemoRepository) {
    val state by repo.state.collectAsState()

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text("My Stable", style = MaterialTheme.typography.titleLarge)
        Spacer(Modifier.height(12.dp))

        LazyVerticalGrid(
            columns = GridCells.Adaptive(minSize = 170.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier.weight(1f)
        ) {
            items(state.horses, key = { it.id }) { h ->
                HorseCard(h)
            }
        }

        Spacer(Modifier.height(10.dp))
        Card {
            Column(Modifier.fillMaxWidth().padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Actions", style = MaterialTheme.typography.titleMedium)
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedButton(onClick = { /* placeholder */ }) { Text("Train") }
                    OutlinedButton(onClick = { /* placeholder */ }) { Text("Season Lock") }
                }
            }
        }
    }
}

@Composable
private fun HorseCard(h: Horse) {
    Card {
        Column(Modifier.fillMaxWidth().padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(h.name, style = MaterialTheme.typography.titleMedium)
            Text("Tier: ${h.tier}")
            Text("Speed: ${h.speed}  Stamina: ${h.stamina}")
            Text("Daily purse: ${h.dailyPurse}")
        }
    }
}
