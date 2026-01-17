package isep.fr.ii3510_eomp2526_germain_vallon.data.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import isep.fr.ii3510_eomp2526_germain_vallon.data.model.QueueItem
import isep.fr.ii3510_eomp2526_germain_vallon.data.model.Track
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class QueueRepository(
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance(),
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
) {

    fun observeQueue(roomId: String): Flow<List<QueueItem>> = callbackFlow {
        val ref = db.collection("rooms")
            .document(roomId)
            .collection("queue")
            .orderBy("createdAt")

        val reg = ref.addSnapshotListener { snap, err ->
            if (err != null) {
                close(err)
                return@addSnapshotListener
            }

            val items = snap?.documents?.mapNotNull { doc ->
                val trackId = doc.getString("trackId") ?: return@mapNotNull null
                val title = doc.getString("title") ?: return@mapNotNull null
                val artist = doc.getString("artist") ?: "Unknown"
                val durationMs = doc.getLong("durationMs") ?: 0L
                val artworkUrl = doc.getString("artworkUrl")
                val previewUrl = doc.getString("previewUrl")
                val addedByUid = doc.getString("addedByUid") ?: "?"
                val addedByName = doc.getString("addedByName") ?: "Guest"
                val createdAt = doc.getTimestamp("createdAt")?.toDate()?.time // can be null briefly
                val status = doc.getString("status") ?: "QUEUED"

                QueueItem(
                    id = doc.id,
                    track = Track(
                        trackId = trackId,
                        title = title,
                        artist = artist,
                        durationMs = durationMs,
                        artworkUrl = artworkUrl,
                        previewUrl = previewUrl
                    ),
                    addedByUid = addedByUid,
                    addedByName = addedByName,
                    createdAtMillis = createdAt,
                    status = status
                )
            } ?: emptyList()

            val sorted = items.sortedWith(
                compareBy<QueueItem> { it.createdAtMillis ?: Long.MAX_VALUE }.thenBy { it.id }
            )

            trySend(sorted)
        }

        awaitClose { reg.remove() }
    }

    suspend fun addToQueue(roomId: String, track: Track, addedByName: String = "Guest") {
        val uid = requireNotNull(auth.currentUser?.uid) { "User not signed in" }

        val data = hashMapOf(
            "trackId" to track.trackId,
            "title" to track.title,
            "artist" to track.artist,
            "durationMs" to track.durationMs,
            "artworkUrl" to track.artworkUrl,
            "previewUrl" to track.previewUrl,
            "addedByUid" to uid,
            "addedByName" to addedByName,
            "createdAt" to FieldValue.serverTimestamp(),
            "status" to "QUEUED"
        )

        db.collection("rooms")
            .document(roomId)
            .collection("queue")
            .add(data)
            .await()
    }

    suspend fun setStatus(roomId: String, queueItemId: String, status: String) {
        db.collection("rooms").document(roomId)
            .collection("queue").document(queueItemId)
            .update("status", status)
            .await()
    }

    suspend fun deleteQueueItem(roomId: String, queueItemId: String) {
        db.collection("rooms").document(roomId)
            .collection("queue").document(queueItemId)
            .delete()
            .await()
    }
}
