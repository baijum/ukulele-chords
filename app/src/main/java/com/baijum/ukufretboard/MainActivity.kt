package com.baijum.ukufretboard

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.baijum.ukufretboard.ui.FretboardScreen
import com.baijum.ukufretboard.ui.theme.UkuFretboardTheme

/**
 * Single-activity entry point for the Ukulele Chord Explorer app.
 *
 * Sets up edge-to-edge display and applies the app theme before
 * rendering the main [FretboardScreen].
 */
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            UkuFretboardTheme {
                FretboardScreen()
            }
        }
    }
}
