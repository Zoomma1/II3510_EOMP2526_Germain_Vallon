package isep.fr.ii3510_eomp2526_germain_vallon

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import isep.fr.ii3510_eomp2526_germain_vallon.ui.screens.HomeScreen
import isep.fr.ii3510_eomp2526_germain_vallon.ui.screens.TestTags
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

class HomeScreenTest {
    @get:Rule val rule = createComposeRule()

    @Test fun join_callsCallback() {
        var joined: String? = null

        rule.setContent {
            HomeScreen(
                isLoading = false,
                error = null,
                displayName = "Guest",
                lastRoomCode = null,
                onSaveName = {},
                onRejoinLast = {},
                onCreate = {},
                onJoin = { joined = it },
                onScanQr = {},
                canRejoinAsHost = false
            )
        }

        rule.onNodeWithTag(TestTags.ROOM_CODE_FIELD).performTextInput("XNFS55")
        rule.onNodeWithTag(TestTags.JOIN_ROOM_BUTTON).performClick()

        assertEquals("XNFS55", joined)
    }
}
