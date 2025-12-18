Work Log
2025‑12‑17


Initial Inspection: Unzipped the PEGZ project and surveyed the structure. Recognised that the game is an Android Compose application. The PegBoard implementation in ui/PegBoard.kt provided a sophisticated, animated board with neon effects, while GameScreen.kt still referenced an outdated stub accepting primitive parameters. Also noted that particle effects used SnapshotStateList without appropriate imports, and spawnParticles accepted an incompatible type.


Refactoring PegBoard: Added missing imports (mutableStateOf, mutableStateListOf, getValue, setValue, delay) and converted the particle list to a mutableStateListOf so Compose recompositions respond to updates. Rewrote the particle update loop to remove expired particles and update positions consistently at ~60 fps. Updated spawnParticles to accept a generic MutableList<Particle> and to generate random directions using radians for correct trigonometric calculations.


Updating GameScreen: Reworked GameScreen to call the updated PegBoard by passing uiState and viewModel directly. Added a large combo counter overlay and replaced the previous placeholder board with the proper implementation. Integrated a revised GameOverOverlay that displays different information depending on the game mode (timed vs. classic). Added formatted time strings and showed score or pegs left accordingly. Added logic to reset game state on replay while retaining the current mode. Removed the stubbed PegBoard composable and replaced it with a LegacyPegBoard to avoid naming collisions.


Improving GameOverOverlay: Modified GameOverOverlay to accept new parameters (score, timeLeft, mode) and implemented logic to display the correct statistics. Adjusted colours, font sizes, spacing and alpha values to align with the neon aesthetic in the concept images. Added explanatory text for non‑premium users indicating that an ad will be shown.


Documentation: Created TASKS.md, WORKLOG.md, and DECISIONS.md files in the docs directory. Logged tasks completed on this date and summarised the changes made. The DECISIONS.md file captures high‑level design choices and assumptions.


How to Run


Open the project in Android Studio Iguana or later.


Ensure that the module level build.gradle.kts uses appropriate versions of the Compose BOM and dependencies (placeholders may need replacement with real version numbers).


Use the GameScreen composable as the entry point by running MainActivity. The neon Peg board will render with touch interactions, combo animations, and a game‑over overlay.


To test timed mode, select the TIMED option in ModeSelector (hook up ModeSelector to pass GameMode.TIMED and starting position). Jumps accumulate points; combos are displayed in the top‑left corner.


2025‑12‑17 (Evening Session)


Theme Rework: Tweaked the CyberpunkTheme colour palette to match the slime/neon concept art. Replaced the original cyan/magenta/red palette with Toxic Green for the primary accent, Electric Blue for secondary highlights, and Cyber Pink for selected pegs. Updated dark surfaces to use a rusted iron grey instead of pure black to evoke the ooze‑coated hardware in the reference images.


Game Screen Layout: Overhauled GameScreen to more faithfully reflect the concept art. The board and scoreboard are now arranged horizontally; the peg board sits on the left while a persistent scoreboard panel occupies the right. The scoreboard panel displays dynamic stats during play and switches to a results view (victory or game over) with replay and menu buttons. Combo messages, score/time counters, pegs/moves, and ad‑status text are contained within this panel. Control buttons (Undo, Achievements, Menu) now sit beneath the board for better ergonomics.


Scoreboard Panel: Introduced a new internal composable, ScoreboardPanel, within GameScreen. It computes a formatted time string, displays a large header (victory/game over/mode name), shows current stats or final results depending on game state, and includes replay and menu actions. Combo counters appear at the top for timed mode when multiple jumps occur. The panel’s styling uses the updated colour scheme and a semi‑transparent card to emulate the chalkboard look in the reference images.


Mode Selector Overhaul: Redesigned ModeSelector to mirror the lower panels from the concept art. The screen now features a large title (“PEGZ”), a section for unlocking new levels with two big buttons (“Neon Bio‑Lab” and “Steamy Jungle”), an Endless Mode section that toggles purchase flow or mode start depending on premium status, and a high‑scores button. Added a LevelButton helper composable and updated ModeButton usage accordingly. Placeholder icons (DNA/leaf emoji) stand in for proper vector assets.


Additional Clean‑up: Removed unused animation imports from GameScreen. Added missing import for GameMode. Updated ModeSelector leaf emoji to avoid surrogate pair issues. Documented design decisions and reasons in DECISIONS.md.

