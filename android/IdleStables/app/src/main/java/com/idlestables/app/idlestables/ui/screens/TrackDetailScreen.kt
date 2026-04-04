package com.idlestables.app.idlestables.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.idlestables.app.idlestables.demo.DemoRepository
import com.idlestables.app.idlestables.model.EnterRaceMode

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TrackDetailScreen(
    repo: DemoRepository,
    trackId: String,
    onOpenRace: (String) -> Unit,
) {
    val state by repo.state.collectAsState()
    val track = state.tracks.find { it.id == trackId }

    var selectedHorseId by remember { mutableStateOf(state.horses.firstOrNull()?.id) }

    // Pull from state so entrantsCount updates live after Enter.
    val races = state.racesByTrack[trackId].orEmpty()

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text(track?.name ?: "Track", style = MaterialTheme.typography.titleLarge)
        Spacer(Modifier.height(10.dp))

        // Pick horse
        var pickerOpen by remember { mutableStateOf(false) }
        ExposedDropdownMenuBox(
            expanded = pickerOpen,
            onExpandedChange = { pickerOpen = !pickerOpen },
        ) {
            val selectedHorseName = state.horses.find { it.id == selectedHorseId }?.name ?: "Select horse"
            OutlinedTextField(
                value = selectedHorseName,
                onValueChange = {},
                readOnly = true,
                label = { Text("Horse") },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = pickerOpen) },
                modifier = Modifier
                    .menuAnchor()
                    .fillMaxWidth()
            )
            ExposedDropdownMenu(
                expanded = pickerOpen,
                onDismissRequest = { pickerOpen = false },
            ) {
                state.horses.forEach { h ->
                    DropdownMenuItem(
                        text = { Text(h.name) },
                        onClick = {
                            selectedHorseId = h.id
                            pickerOpen = false
                        }
                    )
                }
            }
        }

        Spacer(Modifier.height(12.dp))
        Text("Schedule", style = MaterialTheme.typography.titleMedium)
        Spacer(Modifier.height(8.dp))

        LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            items(races, key = { it.id }) { r ->
                Card {
                    Column(Modifier.fillMaxWidth().padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text("Race: ${r.id}")
                        Text("Entrants: ${r.entrantsCount}/${r.fieldSize}  Closes: ${r.entryClosesTs}")
                        if (r.isMegaCup) Text("MEGA CUP", color = MaterialTheme.colorScheme.primary)

                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            Button(
                                onClick = {
                                    val hid = selectedHorseId ?: state.horses.firstOrNull()?.id ?: return@Button
                                    repo.enterRace(trackId, r.id, hid, EnterRaceMode.MANUAL)
                                }
                            ) { Text("Enter (Manual)") }

                            OutlinedButton(
                                onClick = {
                                    val hid = selectedHorseId ?: state.horses.firstOrNull()?.id ?: return@OutlinedButton
                                    repo.enterRace(trackId, r.id, hid, EnterRaceMode.AUTO)
                                }
                            ) { Text("Enter (Auto)") }

                            TextButton(onClick = { onOpenRace(r.id) }) { Text("Race") }
                        }
                    }
                }
            }
        }
    }
}
