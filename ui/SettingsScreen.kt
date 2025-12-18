package com.neon.peggame.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.yourname.pegz.ui.theme.* // Ensure your VoidBlack, ToxicGreen, etc. are imported

@Composable
fun SettingsScreen(
    vibrationEnabled: Boolean,    // From SettingsManager.VIBRATION_ENABLED
    musicEnabled: Boolean,        // From SettingsManager.MUSIC_ENABLED
    isPremium: Boolean,           // From SettingsManager.IS_PREMIUM_USER
    masterVolume: Float,          // From SettingsManager.MASTER_VOLUME
    activeSet: String,            // From SettingsManager.ACTIVE_PEGZ_SET
    onVibrationToggle: (Boolean) -> Unit,
    onMusicToggle: (Boolean) -> Unit,
    onVolumeChange: (Float) -> Unit,
    onSetSelected: (String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(VoidBlack)
            .verticalScroll(rememberScrollState())
            .padding(24.dp)
    ) {
        // --- Header Section ---
        Text(
            text = "SYSTEM CALIBRATION",
            style = MaterialTheme.typography.headlineMedium,
            color = ToxicGreen
        )
        
        if (isPremium) {
            Text(
                text = "PREMIUM PROTOCOL ACTIVE",
                style = MaterialTheme.typography.labelLarge,
                color = CyberPink,
                modifier = Modifier.padding(top = 4.dp)
            )
        }

        Spacer(modifier = Modifier.height(32.dp))

        // --- Hardware Toggles ---
        Text("HARDWARE INTERFACE", color = Color.Gray, style = MaterialTheme.typography.labelMedium)
        Spacer(modifier = Modifier.height(8.dp))
        
        ToggleRow(
            label = "HAPTIC VIBRATION",
            description = "Tactile response on jumps",
            isEnabled = vibrationEnabled,
            onToggle = onVibrationToggle
        )

        ToggleRow(
            label = "NEON MUSIC",
            description = "Atmospheric lab themes",
            isEnabled = musicEnabled,
            onToggle = onMusicToggle
        )

        Divider(color = SlateGrey, modifier = Modifier.padding(vertical = 16.dp))

        // --- Audio Section ---
        Text("AUDIO GAIN", color = Color.White, style = MaterialTheme.typography.titleMedium)
        Spacer(modifier = Modifier.height(8.dp))
        Slider(
            value = masterVolume,
            onValueChange = onVolumeChange,
            colors = SliderDefaults.colors(
                thumbColor = ToxicGreen,
                activeTrackColor = ToxicGreen,
                inactiveTrackColor = SlateGrey
            )
        )

        Divider(color = SlateGrey, modifier = Modifier.padding(vertical = 16.dp))

        // --- The Garage (Character Set Swapper) ---
        Text(
            text = "ACTIVE CHARACTER SET",
            color = ToxicGreen,
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        val characterSets = listOf("BIO_LAB", "HAUNTED", "OOZE", "URBAN")
        
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(bottom = 32.dp)
        ) {
            items(characterSets) { setName ->
                CharacterCard(
                    setName = setName,
                    isSelected = activeSet == setName,
                    onSelect = { onSetSelected(setName) }
                )
            }
        }
    }
}

/**
 * Helper component for consistent toggle rows
 */
@Composable
fun ToggleRow(
    label: String,
    description: String,
    isEnabled: Boolean,
    onToggle: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(text = label, color = Color.White, style = MaterialTheme.typography.titleMedium)
            Text(text = description, color = Color.Gray, style = MaterialTheme.typography.bodySmall)
        }
        Switch(
            checked = isEnabled,
            onCheckedChange = onToggle,
            colors = SwitchDefaults.colors(
                checkedThumbColor = ToxicGreen,
                checkedTrackColor = ToxicGreen.copy(alpha = 0.5f),
                uncheckedThumbColor = SlateGrey
            )
        )
    }
}

@Composable
fun CharacterCard(
    setName: String,
    isSelected: Boolean,
    onSelect: () -> Unit
) {
    val borderColor = if (isSelected) ToxicGreen else SlateGrey
    val backgroundColor = if (isSelected) RustedIron else Color.Transparent

    Box(
        modifier = Modifier
            .size(width = 130.dp, height = 170.dp)
            .background(backgroundColor, RoundedCornerShape(12.dp))
            .border(2.dp, borderColor, RoundedCornerShape(12.dp))
            .clickable { onSelect() }
            .padding(12.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            // Iconic representation placeholder
            Surface(
                modifier = Modifier.size(64.dp),
                shape = RoundedCornerShape(32.dp),
                color = borderColor.copy(alpha = 0.2f),
                border = BorderStroke(1.dp, borderColor)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        text = setName.take(1), 
                        style = MaterialTheme.typography.headlineMedium, 
                        color = borderColor
                    )
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = setName.replace("_", " "),
                style = MaterialTheme.typography.labelMedium,
                color = if (isSelected) Color.White else Color.Gray
            )
        }
    }
}
