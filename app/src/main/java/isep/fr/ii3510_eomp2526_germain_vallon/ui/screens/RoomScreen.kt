package isep.fr.ii3510_eomp2526_germain_vallon.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.foundation.Image
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import isep.fr.ii3510_eomp2526_germain_vallon.R
import isep.fr.ii3510_eomp2526_germain_vallon.data.model.QueueItem
import isep.fr.ii3510_eomp2526_germain_vallon.data.model.Role
import isep.fr.ii3510_eomp2526_germain_vallon.data.model.Room
import isep.fr.ii3510_eomp2526_germain_vallon.data.model.Track
import isep.fr.ii3510_eomp2526_germain_vallon.ui.viewModel.RoomScreenViewModel
import isep.fr.ii3510_eomp2526_germain_vallon.ui.utils.generateQrBitmap


object RoomTestTags {
    const val ROOM_HEADER = "room_header"
    const val ROOM_CODE = "room_code"
    const val ROLE = "role"

    const val PLAY_NEXT = "play_next"
    const val SKIP = "skip"
    const val PAUSE_RESUME = "pause_resume"

    const val SEARCH_FIELD = "search_field"
    const val SEARCH_BUTTON = "search_button"
    const val SEARCH_LOADING = "search_loading"
    const val ERROR_TEXT = "room_error"

    const val SEARCH_RESULTS_TOGGLE = "search_results_toggle"
    const val SEARCH_RESULTS_CARD = "search_results_card"
    const val SEARCH_RESULTS_LIST = "search_results_list"

    const val NOW_PLAYING_SECTION = "now_playing_section"
    const val UP_NEXT_SECTION = "up_next_section"
    const val LEAVE_BUTTON = "leave_button"

    const val QUEUE_ITEM_PREFIX = "queue_item_"
    const val QUEUE_REMOVE_PREFIX = "queue_remove_"
}

@Composable
fun RoomScreen(
    room: Room,
    role: Role,
    onLeave: () -> Unit,
    vmOverride: RoomScreenViewModel? = null
) {
    val vm = vmOverride ?: remember(room.id) { RoomScreenViewModel(room) }
    val state by vm.state.collectAsState()
    val context = LocalContext.current

    val snackbarHostState = remember { SnackbarHostState() }
    LaunchedEffect(state.error) {
        val err = state.error
        if (!err.isNullOrBlank()) {
            snackbarHostState.showSnackbar(
                message = err,
                withDismissAction = true
            )
        }
    }

    LaunchedEffect(role, room.id) { if (role == Role.HOST) vm.connectHostPlayback(context) }
    LaunchedEffect(role, state.nowPlaying?.id) { if (role == Role.HOST) vm.maybeStartCurrentPlaying() }

    var showResults by remember { mutableStateOf(true) }

    LaunchedEffect(state.isSearching, state.searchResults) {
        if (!state.isSearching && state.searchResults.isNotEmpty()) {
            showResults = true
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        containerColor = Color.Transparent
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Header card
            Card(
                shape = MaterialTheme.shapes.extraLarge,
                elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
            ) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(
                                text = stringResource(R.string.room_title, room.name),
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.SemiBold,
                                modifier = Modifier.testTag(RoomTestTags.ROOM_HEADER)
                            )
                            Text(
                                text = stringResource(R.string.room_code_label, room.code),
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.testTag(RoomTestTags.ROOM_CODE)
                            )
                        }
                        AssistChip(
                            onClick = {},
                            label = { Text(role.name) },
                            leadingIcon = {
                                Icon(
                                    imageVector = if (role == Role.HOST)
                                        Icons.Outlined.Star
                                    else Icons.Outlined.Group,
                                    contentDescription = null
                                )
                            },
                            colors = AssistChipDefaults.assistChipColors(
                                containerColor = if (role == Role.HOST)
                                    MaterialTheme.colorScheme.secondary
                                else
                                    MaterialTheme.colorScheme.surfaceVariant,
                                labelColor = Color.White
                            ),
                            modifier = Modifier.testTag(RoomTestTags.ROLE)
                        )

                    }

                    // Host controls
                    if (role == Role.HOST) {
                        val qrBitmap = remember(room.code) { generateQrBitmap(room.code, size = 420) }

                        Card(
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                            shape = MaterialTheme.shapes.large,
                            modifier = Modifier.fillMaxWidth()
                                .testTag("room_qr_card"),
                            elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
                            ) {
                            Column(
                                Modifier.padding(12.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = "Share this QR to join",
                                    style = MaterialTheme.typography.labelLarge,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                                Image(
                                    bitmap = qrBitmap.asImageBitmap(),
                                    contentDescription = "Room QR",
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(220.dp)
                                        .testTag("room_qr_image")
                                )
                                Text(
                                    text = "Code: ${room.code}",
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        Text(stringResource(R.string.host_controls), fontWeight = FontWeight.SemiBold)

                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            Button(
                                onClick = vm::playNext,
                                enabled = state.upNext.isNotEmpty(),
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag(RoomTestTags.PLAY_NEXT),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.primary
                                )
                            ) {
                                Icon(Icons.Outlined.PlayArrow, contentDescription = null)
                                Spacer(Modifier.width(8.dp))
                                Text(stringResource(R.string.play_next))
                            }

                            FilledTonalButton(
                                onClick = vm::skipCurrent,
                                enabled = state.nowPlaying != null,
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag(RoomTestTags.SKIP),
                                colors = ButtonDefaults.filledTonalButtonColors(
                                    containerColor = MaterialTheme.colorScheme.secondaryContainer
                                )
                            ) {
                                Icon(Icons.Outlined.SkipNext, contentDescription = null)
                                Spacer(Modifier.width(8.dp))
                                Text(stringResource(R.string.skip))
                            }
                        }

                        FilledTonalButton(
                            onClick = vm::togglePauseResume,
                            enabled = state.nowPlaying != null,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag(RoomTestTags.PAUSE_RESUME),
                            colors = ButtonDefaults.filledTonalButtonColors(
                                containerColor = MaterialTheme.colorScheme.secondaryContainer
                            )
                        ) {
                            Icon(
                                if (state.hostIsPlaying) Icons.Outlined.PauseCircle else Icons.Outlined.PlayCircle,
                                contentDescription = null
                            )
                            Spacer(Modifier.width(8.dp))
                            Text(stringResource(if (state.hostIsPlaying) R.string.pause else R.string.resume))
                        }
                    } else {
                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            Icon(Icons.Outlined.VolumeOff, contentDescription = null)
                            Text(stringResource(R.string.playback_controlled_by_host))
                        }
                    }
                }
            }

            // Search card
            Card(
                shape = MaterialTheme.shapes.extraLarge,
                elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
            ) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        Icon(Icons.Outlined.Search, contentDescription = null)
                        Text(stringResource(R.string.search_songs), fontWeight = FontWeight.SemiBold)
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = state.searchQuery,
                            onValueChange = vm::onSearchQueryChange,
                            label = { Text(stringResource(R.string.query)) },
                            singleLine = true,
                            modifier = Modifier
                                .weight(1f)
                                .testTag(RoomTestTags.SEARCH_FIELD),
                        )
                        Button(
                            onClick = vm::search,
                            enabled = !state.isSearching,
                            modifier = Modifier.testTag(RoomTestTags.SEARCH_BUTTON),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary
                            )
                        ) { Text(stringResource(R.string.search)) }
                    }

                    if (state.isSearching) {
                        LinearProgressIndicator(
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag(RoomTestTags.SEARCH_LOADING)
                        )
                    }

                    if (state.searchResults.isNotEmpty()) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Results (${state.searchResults.size})",
                                style = MaterialTheme.typography.labelLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )

                            TextButton(
                                onClick = { showResults = !showResults },
                                modifier = Modifier.testTag(RoomTestTags.SEARCH_RESULTS_TOGGLE)
                            ) {
                                Text(if (showResults) "Close" else "Open")
                            }
                        }
                    }

                    // Search results list
                    if (showResults && state.searchResults.isNotEmpty()) {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                            shape = MaterialTheme.shapes.large,
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(max = 260.dp)
                                .testTag(RoomTestTags.SEARCH_RESULTS_CARD)
                        ) {
                            LazyColumn(
                                modifier = Modifier
                                    .padding(8.dp)
                                    .testTag(RoomTestTags.SEARCH_RESULTS_LIST),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                items(state.searchResults.take(20), key = { it.trackId }) { track ->
                                    TrackRow(track = track, onAdd = { vm.addTrack(track) })
                                    Divider()
                                }
                            }
                        }
                    }
                }
            }

            // Queue card
            Card(shape = MaterialTheme.shapes.extraLarge) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        stringResource(R.string.now_playing),
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.testTag(RoomTestTags.NOW_PLAYING_SECTION)
                    )
                    if (state.nowPlaying == null) {
                        Text(
                            stringResource(R.string.nothing_playing),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    } else {
                        QueueItemRow(item = state.nowPlaying!!, showRemove = false, onRemove = {})
                    }

                    Divider()

                    Text(
                        stringResource(R.string.up_next),
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.testTag(RoomTestTags.UP_NEXT_SECTION)
                    )
                    if (state.upNext.isEmpty()) {
                        Text(
                            stringResource(R.string.queue_empty),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    } else {
                        state.upNext.forEach { item ->
                            QueueItemRow(
                                item = item,
                                showRemove = (role == Role.HOST),
                                onRemove = { vm.removeItem(item.id) }
                            )
                            Divider()
                        }
                    }
                }
            }

            Spacer(Modifier.weight(1f))

            Button(
                onClick = onLeave,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag(RoomTestTags.LEAVE_BUTTON),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            ) {
                Icon(Icons.Outlined.ExitToApp, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text(stringResource(R.string.leave_room))
            }
        }
    }
}

@Composable
private fun TrackRow(track: Track, onAdd: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column(Modifier.weight(1f)) {
            Text(track.title, fontWeight = FontWeight.Medium)
            Text(
                track.artist,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        FilledTonalButton(onClick = onAdd) { Text("+") }
    }
}

@Composable
private fun QueueItemRow(
    item: QueueItem,
    showRemove: Boolean,
    onRemove: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .testTag(RoomTestTags.QUEUE_ITEM_PREFIX + item.id),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column(Modifier.weight(1f)) {
            Text("${item.track.title} — ${item.track.artist}", fontWeight = FontWeight.Medium)
            Text(
                stringResource(R.string.added_by, item.addedByName),
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        if (showRemove) {
            OutlinedButton(
                onClick = onRemove,
                modifier = Modifier.testTag(RoomTestTags.QUEUE_REMOVE_PREFIX + item.id)
            ) {
                Icon(Icons.Outlined.Delete, contentDescription = null)
                Spacer(Modifier.width(6.dp))
                Text(stringResource(R.string.remove))
            }
        }
    }
}
