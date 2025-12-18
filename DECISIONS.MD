Decisions Log
2025‑12‑17
Choosing the Neon Aesthetic

The provided concept art includes both toxic‑slime and neon‑city variants of the Peg board. Given that the existing Compose code (in CyberpunkTheme.kt and PegBoard.kt) already follows a neon, cyberpunk aesthetic, I decided to implement the UI to align with the neon imagery rather than the slime versions. This keeps the implementation consistent with the current colour palette (cyan, magenta and dark tones) and avoids introducing assets that are not present in the repository.

Integrating PegBoard

The PegBoard composable defined in ui/PegBoard.kt contained a full implementation of the board drawing, jump animation and particle effects. However, GameScreen.kt was still referencing an outdated stub accepting board, selectedPos and validJumps. I chose to unify the API by modifying GameScreen to pass the entire GameUiState and GameViewModel into the new PegBoard so that it could access the jump history and consume the last move. The old stub has been renamed to LegacyPegBoard and retained only as a reference.

Extending GameOverOverlay

To match the scoreboard shown in the concept art, I expanded the GameOverOverlay parameters. It now takes the current score, remaining time and active game mode. Depending on the mode, it displays either the score and timer (for timed games) or the pegs left and move count (for classic/endless/AI games). This change required additional formatting for the timer into an mm:ss string.

Handling Combos

The timed mode rewards quick successive jumps with combo multipliers. To visually communicate this to players, I added a large “COMBO xN!” overlay in the top‑left corner of the board whenever comboCounter > 1. The text uses the secondary neon colour to stand out against the dark background.

Particle System Implementation

The particle system was converted from a SnapshotStateList to a regular mutable state list with explicit lifetime handling. This decision simplifies the logic and ensures compatibility across Compose versions. Each particle now contains its own velocity vector and lifetime value, and the system updates every ~16 ms.

Documentation Structure

The original project lacked the required docs/TASKS.md, docs/WORKLOG.md and docs/DECISIONS.md files. I created these to satisfy the project guidelines. Future work should continue logging tasks, work and design choices in these files to maintain traceability.

2025‑12‑17 (Evening Session)
Adapting the Colour Palette to the Concept Art

After reviewing the provided images again, I realised that the existing cyan/magenta palette did not fully reflect the toxic‑green and electric‑blue tones seen in the concept art. To bridge this gap without introducing new assets, I redefined the colour constants in CyberpunkTheme.kt to use Toxic Green for the primary accent, Electric Blue for secondary highlights and Cyber Pink for peg selection/error states. Dark surfaces now use a rusted iron grey to simulate the corroded boards. This decision preserves the existing theme infrastructure while aligning the in‑app colours with the reference art.

Reimagining the Game Screen Layout

The original design displayed a full‑screen board with a temporary overlay for game‑over states. The concept art clearly shows a dedicated scoreboard to the right of the triangular board, with persistent stats and combo messages. I therefore reorganised GameScreen into a horizontal layout: the PegBoard occupies the left, and a ScoreboardPanel on the right continuously displays mode information, score/time or pegs/moves, combo counters, and final results. This panel also hosts the replay and menu buttons upon game completion. Control buttons (undo, achievements, menu) were moved to a bottom row for better ergonomics. This arrangement emphasises the scoreboard and makes the UI closer to the art.

Selecting the Menu Structure

The concept art shows two different menu layouts: one for unlocking new levels (Neon Bio‑Lab and Steamy Jungle) and another for unlocking Endless Mode/ad‑free play. To honour both, I redesigned ModeSelector to first present a header and a “Unlock New Level!” section with two level buttons. Below that, the “Go Further!” section shows an Endless Mode button, which is locked if the user isn’t premium. A line of text explains that purchasing will remove ads and unlock Endless Mode. Finally, a high‑scores button sits at the bottom. This structure allows easy extension when actual level themes are implemented and matches the look of the provided images.

Use of Placeholder Icons

In the absence of vector assets for the bio‑lab and jungle levels, I opted to use simple Unicode symbols (DNA and leaf emojis) in the ModeSelector. They are placeholders to illustrate where future icons should appear. Once assets become available, these should be replaced with vector drawables or Composable images. This decision was necessary to deliver a complete UI without delaying on missing resources.

