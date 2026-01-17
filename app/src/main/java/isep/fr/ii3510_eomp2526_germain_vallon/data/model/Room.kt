package isep.fr.ii3510_eomp2526_germain_vallon.data.model

data class Room(
    val id: String,
    val code: String,
    val name: String,
    val hostUid: String
)

enum class Role { HOST, GUEST }
