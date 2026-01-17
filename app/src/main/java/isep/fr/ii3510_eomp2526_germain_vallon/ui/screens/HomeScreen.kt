package isep.fr.ii3510_eomp2526_germain_vallon.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import isep.fr.ii3510_eomp2526_germain_vallon.R

object TestTags {
    const val HOME_TITLE = "home_title"
    const val ERROR_TEXT = "error_text"

    const val DISPLAY_NAME_FIELD = "display_name_field"
    const val SAVE_NAME_BUTTON = "save_name_button"

    const val REJOIN_LAST_BUTTON = "rejoin_last_button"

    const val ROOM_NAME_FIELD = "room_name_field"
    const val CREATE_ROOM_BUTTON = "create_room_button"

    const val ROOM_CODE_FIELD = "room_code_field"
    const val JOIN_ROOM_BUTTON = "join_room_button"

    const val SCAN_QR_BUTTON = "scan_qr_button"
    const val LOADING = "loading_indicator"
}

@Composable
fun HomeScreen(
    isLoading: Boolean,
    error: String?,
    displayName: String,
    lastRoomCode: String?,
    onSaveName: (String) -> Unit,
    onRejoinLast: () -> Unit,
    onCreate: (String) -> Unit,
    onJoin: (String) -> Unit,
    onScanQr: () -> Unit,
    canRejoinAsHost: Boolean = true
) {
    var name by remember(displayName) { mutableStateOf(displayName) }
    var roomName by remember { mutableStateOf("") }
    var roomCode by remember { mutableStateOf("") }

    val snackbarHostState = remember { SnackbarHostState() }
    LaunchedEffect(error) {
        if (!error.isNullOrBlank()) {
            snackbarHostState.showSnackbar(
                message = error,
                withDismissAction = true
            )
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        containerColor = Color.Transparent
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Profile card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.extraLarge,
                elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
            ) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        Icon(Icons.Outlined.Person, contentDescription = null)
                        Text(
                            text = "Profile",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text(stringResource(R.string.display_name)) },
                        leadingIcon = { Icon(Icons.Outlined.Badge, contentDescription = null) },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag(TestTags.DISPLAY_NAME_FIELD)
                    )

                    Button(
                        onClick = { onSaveName(name) },
                        enabled = !isLoading && name.isNotBlank(),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag(TestTags.SAVE_NAME_BUTTON),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary
                        )
                    ) {
                        Icon(Icons.Outlined.CheckCircle, contentDescription = null)
                        Spacer(Modifier.width(8.dp))
                        Text(stringResource(R.string.save_name))
                    }

                    if (canRejoinAsHost && lastRoomCode != null) {
                        FilledTonalButton(
                            onClick = onRejoinLast,
                            enabled = !isLoading,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag(TestTags.REJOIN_LAST_BUTTON),
                                colors = ButtonDefaults.filledTonalButtonColors(
                                    containerColor = MaterialTheme.colorScheme.secondaryContainer
                                )
                        ) {
                            Icon(Icons.Outlined.Restore, contentDescription = null)
                            Spacer(Modifier.width(8.dp))
                            Text(stringResource(R.string.rejoin_last_room, lastRoomCode))
                        }
                    }
                }
            }

            // Room actions
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.extraLarge,
                elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
            ) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        Icon(Icons.Outlined.MeetingRoom, contentDescription = null)
                        Text(
                            text = "Room actions",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    // Create
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text(
                            text = "Create (Host)",
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        OutlinedTextField(
                            value = roomName,
                            onValueChange = { roomName = it },
                            label = { Text(stringResource(R.string.room_name_create)) },
                            leadingIcon = { Icon(Icons.Outlined.Edit, contentDescription = null) },
                            singleLine = true,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag(TestTags.ROOM_NAME_FIELD)
                        )
                        Button(
                            onClick = { onCreate(roomName) },
                            enabled = !isLoading && roomName.isNotBlank(),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag(TestTags.CREATE_ROOM_BUTTON),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary
                            )
                        ) {
                            Icon(Icons.Outlined.AddCircle, contentDescription = null)
                            Spacer(Modifier.width(8.dp))
                            Text(stringResource(R.string.create_room_host))
                        }
                    }

                    Divider()

                    // Join
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text(
                            text = "Join (Guest)",
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        OutlinedTextField(
                            value = roomCode,
                            onValueChange = { roomCode = it.uppercase() },
                            label = { Text(stringResource(R.string.room_code_join)) },
                            leadingIcon = { Icon(Icons.Outlined.Key, contentDescription = null) },
                            singleLine = true,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag(TestTags.ROOM_CODE_FIELD)
                        )
                        Button(
                            onClick = { onJoin(roomCode) },
                            enabled = !isLoading && roomCode.isNotBlank(),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag(TestTags.JOIN_ROOM_BUTTON),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary
                            )
                        ) {
                            Icon(Icons.Outlined.Login, contentDescription = null)
                            Spacer(Modifier.width(8.dp))
                            Text(stringResource(R.string.join_room_guest))
                        }

                        FilledTonalButton(
                            onClick = onScanQr,
                            enabled = !isLoading,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag(TestTags.SCAN_QR_BUTTON),
                            colors = ButtonDefaults.filledTonalButtonColors(
                                containerColor = MaterialTheme.colorScheme.secondaryContainer
                            )
                        ) {
                            Icon(Icons.Outlined.QrCodeScanner, contentDescription = null)
                            Spacer(Modifier.width(8.dp))
                            Text(stringResource(R.string.scan_qr))
                        }
                    }
                }
            }

            if (isLoading) {
                LinearProgressIndicator(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag(TestTags.LOADING)
                )
            }
        }
    }
}
