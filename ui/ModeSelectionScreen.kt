@Composable
fun ModeSelectionScreen(onStartGame: (GameMode) -> Unit) {
    var selectedMode by remember { mutableStateOf(GameMode.CLASSIC) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(VoidBlack) // Our custom deep black
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "SELECT PROTOCOL",
            style = MaterialTheme.typography.headlineLarge,
            color = Color.White,
            modifier = Modifier.padding(bottom = 48.dp)
        )

        // --- CLASSIC MODE CARD ---
        ModeCard(
            title = "CLASSIC",
            description = "The Original 15-Hole Trial. Pure Logic. No Hazards.",
            color = ElectricBlue,
            isSelected = selectedMode == GameMode.CLASSIC,
            onClick = { selectedMode = GameMode.CLASSIC }
        )

        Spacer(modifier = Modifier.height(24.dp))

        // --- CHAOS MODE CARD ---
        ModeCard(
            title = "CHAOS",
            description = "Ooze Slides. Contamination. Titan Bosses. Pure Mayhem.",
            color = ToxicGreen,
            isSelected = selectedMode == GameMode.CHAOS,
            onClick = { selectedMode = GameMode.CHAOS }
        )

        Spacer(modifier = Modifier.height(64.dp))

        // --- START BUTTON ---
        Button(
            onClick = { onStartGame(selectedMode) },
            modifier = Modifier
                .fillMaxWidth()
                .height(60.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = if (selectedMode == GameMode.CHAOS) ToxicGreen else ElectricBlue
            ),
            shape = RoundedCornerShape(8.dp)
        ) {
            Text(
                text = "INITIALIZE",
                style = MaterialTheme.typography.titleLarge,
                color = VoidBlack
            )
        }
    }
}

@Composable
fun ModeCard(
    title: String,
    description: String,
    color: Color,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val borderGlow = if (isSelected) 4.dp else 1.dp
    val alpha = if (isSelected) 1f else 0.5f

    Surface(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        color = RustedIron.copy(alpha = alpha),
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(borderGlow, color)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text(text = title, color = color, style = MaterialTheme.typography.headlineSmall)
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = description, color = Color.White.copy(alpha = 0.7f))
        }
    }
}
