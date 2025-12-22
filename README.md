# Pegz
## Build notes (important)
This repo originally contained broken / empty Gradle modules and missing Android project scaffolding.
It has been repaired into a **single-module Compose app** in `:app`.

If your clone is missing `gradle/wrapper/gradle-wrapper.jar` (not included in the original zip),
Android Studio will prompt to generate the Gradle wrapper automatically. Do that once, commit it,
and you're done.

The PEGZ art files used across the whole game:
- `app/src/main/res/drawable/pegz2.jpg` (gameplay background + piece sprites cropped at runtime)
- `app/src/main/res/drawable/pegz3.jpg` (home + how-to background)
- `app/src/main/res/drawable/pegz5.jpg` (mode select + results background)

