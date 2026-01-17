package isep.fr.ii3510_eomp2526_germain_vallon

import isep.fr.ii3510_eomp2526_germain_vallon.data.logic.*
import isep.fr.ii3510_eomp2526_germain_vallon.data.model.*
import org.junit.Assert.*
import org.junit.Test

class QueueLogicTest {

    private fun qi(id: String, status: String, trackId: String) =
        QueueItem(
            id = id,
            track = Track(trackId, "t", "a", 0, null, "url"),
            addedByUid = "u",
            addedByName = "n",
            createdAtMillis = null,
            status = status
        )

    @Test fun nowPlaying_firstPlaying() {
        val q = listOf(qi("1","QUEUED","x"), qi("2","PLAYING","y"), qi("3","PLAYING","z"))
        assertEquals("2", nowPlaying(q)?.id)
    }

    @Test fun upNext_onlyQueued() {
        val q = listOf(qi("1","QUEUED","x"), qi("2","DONE","y"), qi("3","PLAYING","z"))
        assertEquals(listOf("1"), upNext(q).map { it.id })
    }

    @Test fun containsActiveTrack_blocksDuplicates() {
        val q = listOf(qi("1","DONE","x"), qi("2","QUEUED","y"))
        assertTrue(containsActiveTrack(q, "y"))
        assertFalse(containsActiveTrack(q, "x"))
    }
}
