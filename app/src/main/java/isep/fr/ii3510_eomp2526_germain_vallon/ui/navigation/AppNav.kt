package isep.fr.ii3510_eomp2526_germain_vallon.ui.navigation

import androidx.compose.runtime.*
import androidx.navigation.compose.*
import isep.fr.ii3510_eomp2526_germain_vallon.ui.viewModel.RoomViewModel
import isep.fr.ii3510_eomp2526_germain_vallon.ui.screens.HomeScreen
import isep.fr.ii3510_eomp2526_germain_vallon.ui.screens.QrScannerScreen
import isep.fr.ii3510_eomp2526_germain_vallon.data.model.Role
import isep.fr.ii3510_eomp2526_germain_vallon.ui.screens.RoomScreen

@Composable
fun AppNav(vm: RoomViewModel) {
    val navController = rememberNavController()
    val state by vm.uiState.collectAsState()

    // If room becomes non-null, go to room screen (one-way)
    LaunchedEffect(state.currentRoom) {
        if (state.currentRoom != null) {
            navController.navigate(Routes.ROOM) {
                popUpTo(Routes.HOME) { inclusive = false }
            }
        }
    }

    NavHost(navController = navController, startDestination = Routes.HOME) {
        composable(Routes.HOME) {
            HomeScreen(
                isLoading = state.isLoading,
                error = state.error,
                displayName = state.displayName,
                lastRoomCode = state.lastRoomCode,
                onSaveName = { vm.saveDisplayName(it) },
                onRejoinLast = { vm.rejoinLastRoom() },
                onCreate = { vm.createRoom(it) },
                onJoin = { vm.joinRoom(it) },
                onScanQr = { navController.navigate(Routes.SCAN) },
                canRejoinAsHost = state.canRejoinAsHost
            )
        }

        composable(Routes.SCAN) {
            QrScannerScreen(
                onCodeScanned = { scanned ->
                    // QR contains the room code directly
                    vm.joinRoom(scanned)
                },
                onBack = { navController.popBackStack() }
            )
        }

        composable(Routes.ROOM) {
            val room = state.currentRoom
            if (room == null) {
                // If user left room, return home
                LaunchedEffect(Unit) {
                    navController.navigate(Routes.HOME) {
                        popUpTo(Routes.HOME) { inclusive = true }
                    }
                }
            } else {
                val uid = com.google.firebase.auth.FirebaseAuth.getInstance().currentUser?.uid
                val role = if (uid == room.hostUid) Role.HOST else Role.GUEST

                RoomScreen(
                    room = room,
                    role = role,
                    onLeave = {
                        vm.clearRoom()
                        navController.navigate(Routes.HOME) {
                            popUpTo(Routes.HOME) { inclusive = true }
                        }
                    }
                )
            }
        }
    }
}
