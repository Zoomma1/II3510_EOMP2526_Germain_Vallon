package isep.fr.ii3510_eomp2526_germain_vallon.data.model

data class QueueItem(
    val id: String,
    val track: Track,
    val addedByUid: String,
    val addedByName: String,
    val createdAtMillis: Long? = null,
    val status: String = "QUEUED"
)
