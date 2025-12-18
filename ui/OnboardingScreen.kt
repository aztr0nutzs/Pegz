package com.neon.peggame.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.neon.peggame.data.SettingsManager
import kotlinx.coroutines.launch

@Composable
fun OnboardingScreen(
    settingsManager: SettingsManager,
    onOnboardingComplete: () -> Unit
) {
    val scope = rememberCoroutineScope()
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(CyberpunkTheme.colors.background)
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "PEG NEON: PROTOCOL START",
            fontSize = 30.sp,
            fontWeight = FontWeight.Black,
            color = CyberpunkTheme.colors.primary,
            modifier = Modifier.padding(bottom = 32.dp)
        )

        OnboardingCard(
            title = "THE OBJECTIVE",
            content = "Eliminate all but one peg. A 'Genius' score is achieved if the last peg lands in the center hole."
        )
        Spacer(Modifier.height(16.dp))

        OnboardingCard(
            title = "THE JUMP",
            content = "Tap a peg to select it. Tap an empty hole two positions away to jump over the adjacent peg. The jumped peg is removed."
        )
        Spacer(Modifier.height(16.dp))

        OnboardingCard(
            title = "CYBERPUNK INTERFACE",
            content = "Watch the counter! In TIMED mode, quick successive jumps build Combos for high scores. Use the UNDO button to rewind a mistake."
        )

        Spacer(Modifier.height(48.dp))

        Button(
            onClick = { 
                scope.launch {
                    settingsManager.completeOnboarding()
                    onOnboardingComplete()
                }
            },
            modifier = Modifier.fillMaxWidth(0.8f).height(64.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = CyberpunkTheme.colors.secondary,
                contentColor = CyberpunkTheme.colors.onPrimary
            )
        ) {
            Text("ENGAGE PROTOCOL", fontSize = 22.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun OnboardingCard(title: String, content: String) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(CyberpunkTheme.colors.boardFrame.copy(alpha = 0.3f))
            .padding(16.dp)
    ) {
        Text(
            text = title,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = CyberpunkTheme.colors.secondary
        )
        Spacer(Modifier.height(8.dp))
        Text(
            text = content,
            fontSize = 14.sp,
            color = CyberpunkTheme.colors.onBackground,
            textAlign = TextAlign.Start
        )
    }
}
