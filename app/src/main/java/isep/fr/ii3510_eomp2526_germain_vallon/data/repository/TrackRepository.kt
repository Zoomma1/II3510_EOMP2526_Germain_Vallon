package isep.fr.ii3510_eomp2526_germain_vallon.data.repository

import isep.fr.ii3510_eomp2526_germain_vallon.data.model.Track
import isep.fr.ii3510_eomp2526_germain_vallon.data.model.toTrack
import isep.fr.ii3510_eomp2526_germain_vallon.data.remote.NetworkModule

class TrackRepository {
    private val api = NetworkModule.itunesApi

    suspend fun search(term: String): List<Track> {
        val q = term.trim()
        if (q.isBlank()) return emptyList()

        val res = api.searchSongs(term = q)
        return res.results.mapNotNull { it.toTrack() }
    }
}
