package isep.fr.ii3510_eomp2526_germain_vallon.data.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class UserRepository(
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance(),
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
) {
    private var cachedName: String? = null

    suspend fun getOrCreateUser(): String {
        val uid = requireNotNull(auth.currentUser?.uid) { "User not signed in" }
        val ref = db.collection("users").document(uid)
        val snap = ref.get().await()
        if (!snap.exists()) {
            ref.set(
                mapOf(
                    "displayName" to "Guest",
                    "createdAt" to FieldValue.serverTimestamp()
                )
            ).await()
            cachedName = "Guest"
            return "Guest"
        }
        val name = snap.getString("displayName") ?: "Guest"
        cachedName = name
        return name
    }

    suspend fun setDisplayName(name: String) {
        val uid = requireNotNull(auth.currentUser?.uid) { "User not signed in" }
        val clean = name.trim().take(30)
        require(clean.isNotBlank()) { "Display name cannot be empty" }

        db.collection("users").document(uid)
            .set(
                mapOf(
                    "displayName" to clean,
                    "updatedAt" to FieldValue.serverTimestamp()
                ),
                com.google.firebase.firestore.SetOptions.merge()
            )
            .await()

        cachedName = clean
    }

    suspend fun getDisplayName(): String {
        return cachedName ?: getOrCreateUser()
    }
}
