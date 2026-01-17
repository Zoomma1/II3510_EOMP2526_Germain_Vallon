package isep.fr.ii3510_eomp2526_germain_vallon.ui.viewModel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import isep.fr.ii3510_eomp2526_germain_vallon.data.model.QueueItem
import isep.fr.ii3510_eomp2526_germain_vallon.data.model.Room
import isep.fr.ii3510_eomp2526_germain_vallon.data.model.Track
import isep.fr.ii3510_eomp2526_germain_vallon.data.repository.QueueRepository
import isep.fr.ii3510_eomp2526_germain_vallon.data.repository.RoomRepository
import isep.fr.ii3510_eomp2526_germain_vallon.data.repository.TrackRepository
import isep.fr.ii3510_eomp2526_germain_vallon.data.repository.UserRepository
import isep.fr.ii3510_eomp2526_germain_vallon.player.HostPlayerController
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class RoomScreenState(
    val room: Room,
    val queue: List<QueueItem> = emptyList(),
    val searchQuery: String = "",
    val searchResults: List<Track> = emptyList(),
    val isSearching: Boolean = false,
    val error: String? = null,
    val hostIsPlaying: Boolean = false
) {
    val nowPlaying: QueueItem? get() = queue.firstOrNull { it.status == "PLAYING" }
    val upNext: List<QueueItem> get() = queue.filter { it.status == "QUEUED" }
    val history: List<QueueItem> get() = queue.filter { it.status == "DONE" }
}

class RoomScreenViewModel(
    private val room: Room,
    private val trackRepo: TrackRepository = TrackRepository(),
    private val queueRepo: QueueRepository = QueueRepository(),
    private val userRepo: UserRepository = UserRepository(),
    private val roomRepo: RoomRepository = RoomRepository()
) : ViewModel() {

    private val _state = MutableStateFlow(RoomScreenState(room = room))
    val state: StateFlow<RoomScreenState> = _state

    // Host-only playback
    private var player: HostPlayerController? = null
    private var currentlyPlayingQueueItemId: String? = null

    init {
        viewModelScope.launch {
            queueRepo.observeQueue(room.id).collect { items ->
                _state.update { it.copy(queue = items) }
            }
        }

        // Presence-lite heartbeat
        viewModelScope.launch {
            while (true) {
                runCatching { roomRepo.heartbeat(room.id) }
                delay(30_000)
            }
        }
    }

    // ---- Host playback hooks (same as your Milestone 4) ----
    fun connectHostPlayback(context: Context) {
        if (player != null) return
        val p = HostPlayerController(context.applicationContext)
        player = p

        p.setOnEnded {
            viewModelScope.launch {
                val current = _state.value.nowPlaying ?: return@launch
                runCatching { queueRepo.setStatus(room.id, current.id, "DONE") }
                currentlyPlayingQueueItemId = null
                playNext()
            }
        }

        maybeStartCurrentPlaying()
    }

    fun maybeStartCurrentPlaying() {
        val p = player ?: return
        val current = _state.value.nowPlaying ?: return

        if (currentlyPlayingQueueItemId == current.id) {
            _state.update { it.copy(hostIsPlaying = p.isPlaying()) }
            return
        }

        val url = current.track.previewUrl
        if (url.isNullOrBlank()) {
            _state.update { it.copy(error = "No previewUrl for this track") }
            return
        }

        currentlyPlayingQueueItemId = current.id
        p.playUrl(url)
        _state.update { it.copy(hostIsPlaying = true) }
    }

    fun togglePauseResume() {
        val p = player ?: return
        if (p.isPlaying()) {
            p.pause()
            _state.update { it.copy(hostIsPlaying = false) }
        } else {
            p.resume()
            _state.update { it.copy(hostIsPlaying = true) }
        }
    }

    override fun onCleared() {
        player?.release()
        player = null
        super.onCleared()
    }

    // ---- Search + queue ----
    fun onSearchQueryChange(q: String) {
        _state.update { it.copy(searchQuery = q) }
    }

    fun search() = viewModelScope.launch {
        val q = _state.value.searchQuery
        _state.update { it.copy(isSearching = true, error = null) }

        runCatching { trackRepo.search(q) }
            .onSuccess { results -> _state.update { it.copy(isSearching = false, searchResults = results) } }
            .onFailure { e -> _state.update { it.copy(isSearching = false, error = e.message ?: "Search error") } }
    }

    fun addTrack(track: Track) = viewModelScope.launch {
        val exists = _state.value.queue.any { it.track.trackId == track.trackId && it.status != "DONE" }
        if (exists) {
            _state.update { it.copy(error = "Track already in queue") }
            return@launch
        }

        val name = runCatching { userRepo.getDisplayName() }.getOrDefault("Guest")

        runCatching { queueRepo.addToQueue(room.id, track, addedByName = name) }
            .onFailure { e -> _state.update { it.copy(error = e.message ?: "Queue error") } }
    }

    fun removeItem(itemId: String) = viewModelScope.launch {
        runCatching { queueRepo.deleteQueueItem(room.id, itemId) }
            .onFailure { e -> _state.update { it.copy(error = e.message ?: "Delete error") } }
    }

    fun skipCurrent() = viewModelScope.launch {
        val currentPlaying = _state.value.queue.firstOrNull { it.status == "PLAYING" } ?: return@launch
        runCatching { queueRepo.setStatus(room.id, currentPlaying.id, "DONE") }
            .onFailure { e -> _state.update { it.copy(error = e.message ?: "Skip error") } }
        currentlyPlayingQueueItemId = null
        playNext()
    }

    fun playNext() = viewModelScope.launch {
        val currentPlaying = _state.value.queue.firstOrNull { it.status == "PLAYING" }
        val next = _state.value.queue.firstOrNull { it.status == "QUEUED" } ?: return@launch

        runCatching {
            if (currentPlaying != null) queueRepo.setStatus(room.id, currentPlaying.id, "DONE")
            queueRepo.setStatus(room.id, next.id, "PLAYING")
        }.onFailure { e ->
            _state.update { it.copy(error = e.message ?: "PlayNext error") }
        }
        maybeStartCurrentPlaying()
    }
}
