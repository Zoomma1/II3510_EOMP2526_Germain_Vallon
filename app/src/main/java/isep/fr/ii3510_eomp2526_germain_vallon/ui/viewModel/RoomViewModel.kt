package isep.fr.ii3510_eomp2526_germain_vallon.ui.viewModel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import isep.fr.ii3510_eomp2526_germain_vallon.data.local.LocalRoomStore
import isep.fr.ii3510_eomp2526_germain_vallon.data.model.Role
import isep.fr.ii3510_eomp2526_germain_vallon.data.model.Room
import isep.fr.ii3510_eomp2526_germain_vallon.data.repository.RoomRepository
import isep.fr.ii3510_eomp2526_germain_vallon.data.repository.UserRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class RoomUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val currentRoom: Room? = null,
    val role: Role? = null,
    val displayName: String = "Guest",
    val lastRoomCode: String? = null,
    val canRejoinAsHost: Boolean = false
)

class RoomViewModel(app: Application) : AndroidViewModel(app) {

    private val userRepo = UserRepository()
    private val roomRepo = RoomRepository()
    private val localRoomStore = LocalRoomStore(app.applicationContext)

    private val _uiState = MutableStateFlow(RoomUiState())
    val uiState: StateFlow<RoomUiState> = _uiState

    init {
        viewModelScope.launch {
            val name = runCatching { userRepo.getOrCreateUser() }.getOrDefault("Guest")
            _uiState.update { it.copy(displayName = name) }
        }
        viewModelScope.launch {
            localRoomStore.lastRoomCodeFlow().collect { code ->
                _uiState.update { it.copy(lastRoomCode = code, canRejoinAsHost = false) }

                if (code.isNullOrBlank()) return@collect
                val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return@collect

                val room = runCatching { roomRepo.getRoomByCode(code) }.getOrNull()
                val canHostRejoin = (room != null && room.hostUid == uid)

                _uiState.update { it.copy(canRejoinAsHost = canHostRejoin) }
            }
        }

    }

    fun saveDisplayName(name: String) = viewModelScope.launch {
        _uiState.update { it.copy(isLoading = true, error = null) }
        runCatching { userRepo.setDisplayName(name) }
            .onSuccess {
                _uiState.update { it.copy(isLoading = false, displayName = name.trim()) }
            }
            .onFailure { e ->
                _uiState.update { it.copy(isLoading = false, error = e.message ?: "Name error") }
            }
    }

    fun createRoom(name: String) = viewModelScope.launch {
        _uiState.update { it.copy(isLoading = true, error = null) }
        runCatching { roomRepo.createRoom(name) }
            .onSuccess { room ->
                localRoomStore.setLastRoomCode(room.code)
                _uiState.update { it.copy(isLoading = false, currentRoom = room, role = Role.HOST) }
            }
            .onFailure { e ->
                _uiState.update { it.copy(isLoading = false, error = e.message ?: "Error") }
            }
    }

    fun joinRoom(code: String) = viewModelScope.launch {
        _uiState.update { it.copy(isLoading = true, error = null) }
        runCatching { roomRepo.joinRoomByCode(code) }
            .onSuccess { room ->
                localRoomStore.setLastRoomCode(room.code)

                val uid = FirebaseAuth.getInstance().currentUser?.uid
                val role = if (uid == room.hostUid) Role.HOST else Role.GUEST

                _uiState.update { it.copy(isLoading = false, currentRoom = room, role = role) }
            }
            .onFailure { e ->
                _uiState.update { it.copy(isLoading = false, error = e.message ?: "Error") }
            }
    }

    fun rejoinLastRoom() {
        val code = _uiState.value.lastRoomCode ?: return
        joinRoom(code)
    }

    fun clearRoom() {
        _uiState.update { it.copy(currentRoom = null, role = null) }
    }
}
