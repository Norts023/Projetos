package com.skytycoon.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.skytycoon.app.ui.navigation.SkyTycoonNavGraph
import com.skytycoon.app.ui.theme.SkyTycoonTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        setContent {
            SkyTycoonTheme {
                SkyTycoonNavGraph()
            }
        }
    }
}
