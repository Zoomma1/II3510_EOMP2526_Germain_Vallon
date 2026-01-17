package isep.fr.ii3510_eomp2526_germain_vallon.data.local

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(name = "local_room_store")

class LocalRoomStore(private val context: Context) {

    private val LAST_ROOM_CODE = stringPreferencesKey("last_room_code")

    fun lastRoomCodeFlow(): Flow<String?> {
        return context.dataStore.data.map { prefs -> prefs[LAST_ROOM_CODE] }
    }

    suspend fun setLastRoomCode(code: String) {
        context.dataStore.edit { prefs ->
            prefs[LAST_ROOM_CODE] = code.trim().uppercase()
        }
    }

    suspend fun clearLastRoomCode() {
        context.dataStore.edit { prefs -> prefs.remove(LAST_ROOM_CODE) }
    }
}
