package isep.fr.ii3510_eomp2526_germain_vallon.data.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FieldValue
import isep.fr.ii3510_eomp2526_germain_vallon.data.model.Room
import kotlinx.coroutines.tasks.await
import kotlin.random.Random

class RoomRepository(
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance(),
    private val auth: FirebaseAuth = FirebaseAuth.getInstance(),
    private val userRepo: UserRepository = UserRepository()
) {
    private val roomsCol = db.collection("rooms")

    suspend fun createRoom(roomName: String): Room {
        val uid = requireNotNull(auth.currentUser?.uid) { "User not signed in" }
        val displayName = userRepo.getDisplayName()

        val code = generateUniqueCode()
        val roomDoc = roomsCol.document()
        val roomId = roomDoc.id

        roomDoc.set(
            mapOf(
                "code" to code,
                "name" to roomName,
                "hostUid" to uid,
                "createdAt" to FieldValue.serverTimestamp(),
                "isOpen" to true
            )
        ).await()

        roomDoc.collection("members").document(uid).set(
            mapOf(
                "displayName" to displayName,
                "role" to "HOST",
                "joinedAt" to FieldValue.serverTimestamp(),
                "lastSeenAt" to FieldValue.serverTimestamp()
            )
        ).await()

        return Room(id = roomId, code = code, name = roomName, hostUid = uid)
    }

    suspend fun joinRoomByCode(codeInput: String): Room {
        val uid = requireNotNull(auth.currentUser?.uid) { "User not signed in" }
        val displayName = userRepo.getDisplayName()
        val code = codeInput.trim().uppercase()

        val query = roomsCol.whereEqualTo("code", code).limit(1).get().await()
        val doc = query.documents.firstOrNull() ?: error("Room not found")

        val hostUid = doc.getString("hostUid") ?: error("Invalid room data")
        val name = doc.getString("name") ?: "Room"

        val role = if (uid == hostUid) "HOST" else "GUEST"

        // Upsert member doc (rejoin safe)
        doc.reference.collection("members").document(uid).set(
            mapOf(
                "displayName" to displayName,
                "role" to role,
                "joinedAt" to FieldValue.serverTimestamp(),
                "lastSeenAt" to FieldValue.serverTimestamp()
            )
        ).await()

        return Room(id = doc.id, code = code, name = name, hostUid = hostUid)
    }

    suspend fun heartbeat(roomId: String) {
        val uid = requireNotNull(auth.currentUser?.uid) { "User not signed in" }
        db.collection("rooms").document(roomId)
            .collection("members").document(uid)
            .update("lastSeenAt", FieldValue.serverTimestamp())
            .await()
    }

    private suspend fun generateUniqueCode(): String {
        repeat(10) {
            val code = randomCode6()
            val existing = roomsCol.whereEqualTo("code", code).limit(1).get().await()
            if (existing.isEmpty) return code
        }
        return randomCode6()
    }

    private fun randomCode6(): String {
        val chars = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789"
        return buildString(6) {
            repeat(6) { append(chars[Random.nextInt(chars.length)]) }
        }
    }

    suspend fun getRoomByCode(codeInput: String): Room? {
        val code = codeInput.trim().uppercase()
        val query = roomsCol.whereEqualTo("code", code).limit(1).get().await()
        val doc = query.documents.firstOrNull() ?: return null
        val hostUid = doc.getString("hostUid") ?: return null
        val name = doc.getString("name") ?: "Room"
        return Room(id = doc.id, code = code, name = name, hostUid = hostUid)
    }
}
