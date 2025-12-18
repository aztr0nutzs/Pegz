// ui/ModeSelector.kt (UPDATED)

package com.neon.peggame.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.android.billingclient.api.ProductDetails
import com.neon.peggame.viewmodel.GameMode
import java.text.NumberFormat
import java.util.Locale

@Composable
fun ModeSelector(
    onStartGame: (GameMode, Int) -> Unit,
    onViewScores: () -> Unit,
    onPurchase: () -> Unit,
    isPremiumFlow: State<ProductDetails?>
) {
    val productDetails by isPremiumFlow
    val priceText = productDetails?.oneTimePurchaseOfferDetails?.formattedPrice ?: "$1.99"
    // Premium flag should ideally come from SettingsManager; default to false here
    val isPremium = false

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(CyberpunkTheme.colors.background)
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Main title matching concept art
        Text(
            text = "PEGZ",
            fontSize = 48.sp,
            fontWeight = FontWeight.Black,
            color = CyberpunkTheme.colors.primary,
            modifier = Modifier.padding(vertical = 24.dp)
        )

        // Show level unlock section inspired by the concept art
        Text(
            text = "UNLOCK NEW LEVEL!",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = CyberpunkTheme.colors.secondary,
            modifier = Modifier.padding(bottom = 12.dp)
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            // Neon Bio‑Lab level button
            LevelButton(
                text = "NEON BIO‑LAB",
                icon = "\u269B", // simple DNA unicode symbol as placeholder
                onClick = { onStartGame(GameMode.CLASSIC, 1) }
            )
            // Steamy Jungle level button (leaf emoji used as placeholder)
            LevelButton(
                text = "STEAMY JUNGLE",
                icon = "🌿",
                onClick = { onStartGame(GameMode.CLASSIC, 1) }
            )
        }

        Spacer(Modifier.height(32.dp))
        // Endless mode unlock section
        Text(
            text = "GO FURTHER!",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = CyberpunkTheme.colors.secondary,
            modifier = Modifier.padding(bottom = 12.dp)
        )
        ModeButton(
            text = "ENDLESS MODE",
            onClick = {
                if (!isPremium) {
                    onPurchase()
                } else {
                    onStartGame(GameMode.ENDLESS, 1)
                }
            },
            enabled = isPremium,
            color = if (!isPremium) CyberpunkTheme.colors.boardFrame.copy(alpha = 0.6f) else CyberpunkTheme.colors.primary
        ) {
            if (!isPremium) {
                Icon(
                    Icons.Filled.Lock,
                    contentDescription = "Locked",
                    tint = CyberpunkTheme.colors.secondary,
                    modifier = Modifier.padding(start = 8.dp)
                )
            }
        }
        if (!isPremium) {
            Text(
                text = "Unlock Ad‑Free Endless Mode for $priceText",
                fontSize = 12.sp,
                color = CyberpunkTheme.colors.secondary.copy(alpha = 0.7f),
                modifier = Modifier.padding(top = 8.dp)
            )
        }

        Spacer(Modifier.height(32.dp))
        ModeButton(
            text = "VIEW HIGH SCORES",
            onClick = onViewScores,
            color = CyberpunkTheme.colors.boardFrame.copy(alpha = 0.6f)
        )
    }
}

@Composable
private fun LevelButton(text: String, icon: String, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        modifier = Modifier.weight(1f).height(64.dp).padding(horizontal = 4.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = CyberpunkTheme.colors.boardFrame.copy(alpha = 0.7f),
            contentColor = CyberpunkTheme.colors.primary
        )
    ) {
        Text(icon, fontSize = 20.sp)
        Spacer(Modifier.width(8.dp))
        Text(text, fontSize = 14.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun ModeButton(
    text: String, 
    onClick: () -> Unit, 
    color: Color? = null,
    enabled: Boolean = true,
    content: @Composable RowScope.() -> Unit = {}
) {
    Button(
        onClick = onClick,
        enabled = enabled,
        modifier = Modifier.fillMaxWidth(0.8f).height(56.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = (color ?: CyberpunkTheme.colors.primary.copy(alpha = 0.9f))
                .copy(alpha = if (enabled) 1f else 0.4f),
            contentColor = CyberpunkTheme.colors.onPrimary
        )
    ) {
        Text(text.uppercase(), fontSize = 18.sp, fontWeight = FontWeight.Bold)
        content() 
    }
}

@Composable
private fun EmptyPosButton(text: String, isSelected: Boolean, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        colors = ButtonDefaults.buttonColors(
            containerColor = if (isSelected) CyberpunkTheme.colors.secondary else CyberpunkTheme.colors.boardFrame,
            contentColor = if (isSelected) CyberpunkTheme.colors.onPrimary else CyberpunkTheme.colors.onBackground
        )
    ) {
        Text(text)
    }
}
