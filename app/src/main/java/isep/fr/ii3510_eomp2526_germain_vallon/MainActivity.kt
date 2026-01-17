package isep.fr.ii3510_eomp2526_germain_vallon

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.google.firebase.FirebaseApp
import isep.fr.ii3510_eomp2526_germain_vallon.ui.navigation.AppNav
import isep.fr.ii3510_eomp2526_germain_vallon.ui.viewModel.RoomViewModel
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import isep.fr.ii3510_eomp2526_germain_vallon.ui.theme.AppBackgroundGradient
import isep.fr.ii3510_eomp2526_germain_vallon.ui.theme.AppTheme

class MainActivity : ComponentActivity() {
    private val roomVm: RoomViewModel by viewModels()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        FirebaseApp.initializeApp(this)


        val auth = com.google.firebase.auth.FirebaseAuth.getInstance()

        if (auth.currentUser == null) {
            auth.signInAnonymously()
                .addOnFailureListener { e ->
                    throw e
                }
        }

        enableEdgeToEdge()

        setContent {
            AppTheme {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(AppBackgroundGradient)
                ) {
                    AppNav(vm = roomVm)
                }
            }
        }
    }
}