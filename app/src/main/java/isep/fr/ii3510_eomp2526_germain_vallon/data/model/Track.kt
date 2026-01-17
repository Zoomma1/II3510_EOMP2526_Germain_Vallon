package isep.fr.ii3510_eomp2526_germain_vallon.data.model

import isep.fr.ii3510_eomp2526_germain_vallon.data.remote.ItunesTrack

data class Track(
    val trackId: String,
    val title: String,
    val artist: String,
    val durationMs: Long,
    val artworkUrl: String?,
    val previewUrl: String?
)

fun ItunesTrack.toTrack(): Track? {
    val id = trackId ?: return null
    val title = trackName ?: return null
    return Track(
        trackId = "itunes:$id",
        title = title,
        artist = artistName ?: "Unknown",
        durationMs = trackTimeMillis ?: 0L,
        artworkUrl = artworkUrl100,
        previewUrl = previewUrl
    )
}
