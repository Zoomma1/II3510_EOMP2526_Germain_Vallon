package isep.fr.ii3510_eomp2526_germain_vallon.data.remote

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import retrofit2.http.GET
import retrofit2.http.Query

@Serializable
data class ItunesSearchResponse(
    @SerialName("resultCount") val resultCount: Int = 0,
    @SerialName("results") val results: List<ItunesTrack> = emptyList()
)

@Serializable
data class ItunesTrack(
    @SerialName("trackId") val trackId: Long? = null,
    @SerialName("trackName") val trackName: String? = null,
    @SerialName("artistName") val artistName: String? = null,
    @SerialName("collectionName") val collectionName: String? = null,
    @SerialName("artworkUrl100") val artworkUrl100: String? = null,
    @SerialName("previewUrl") val previewUrl: String? = null,
    @SerialName("trackTimeMillis") val trackTimeMillis: Long? = null
)

interface ItunesApi {
    @GET("search")
    suspend fun searchSongs(
        @Query("term") term: String,
        @Query("entity") entity: String = "song",
        @Query("limit") limit: Int = 25
    ): ItunesSearchResponse
}
