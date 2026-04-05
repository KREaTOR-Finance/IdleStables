package com.idlestables.app.idlestables.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.idlestables.core.demo.DemoRepository

@Composable
fun BreedScreen(repo: DemoRepository) {
    val state by repo.state.collectAsState()

    var sireId by remember { mutableStateOf(state.horses.getOrNull(0)?.id) }
    var damId by remember { mutableStateOf(state.horses.getOrNull(1)?.id) }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text("Breed", style = MaterialTheme.typography.titleLarge)

        Card {
            Column(Modifier.fillMaxWidth().padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Select parents", style = MaterialTheme.typography.titleMedium)
                Text("Sire: ${sireId ?: "-"}")
                Text("Dam: ${damId ?: "-"}")
                Text("(Selector UI placeholder)", style = MaterialTheme.typography.bodySmall)
            }
        }

        Card {
            Column(Modifier.fillMaxWidth().padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Offspring preview", style = MaterialTheme.typography.titleMedium)
                Text("Seeded preview (demo)")
                Text("Offspring seed: ${(sireId ?: "x") + "-" + (damId ?: "y")}")
            }
        }
    }
}
