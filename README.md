# Goal Bingo

A personal Android app for tracking a 5x5 yearly bingo card of goals
(checkbox goals like "run a sub-2hr half" and counter goals like
"100 wakeups before 7am").

## Stack

- Kotlin 1.9 + Jetpack Compose (Material 3)
- Room for local storage
- Compose Navigation
- Min SDK 26, target/compile SDK 34

The data layer is hidden behind a single `BingoRepository` interface so a
cloud-backed implementation can be dropped in later without touching the UI.

## First-time setup

You have Android Studio installed (Iguana 2023.2), an Android SDK at
`~/Library/Android/sdk`, and a Pixel 3a API 34 emulator already configured.

1. **Android Studio → File → Open…** → pick this directory.
2. Studio will create `local.properties` (pointing at your SDK) and offer to
   generate the Gradle wrapper. Accept both. First sync downloads dependencies
   and may take a few minutes.
3. Pick the `app` run configuration, pick the Pixel 3a emulator, hit Run.

If Studio doesn't offer to set up the wrapper, install Gradle once
(`brew install gradle`) and run `gradle wrapper` from the project root, then
re-sync in Studio. Studio's bundled Gradle is 8.2; the project pins 8.4
(needed by AGP 8.3) and the wrapper handles the download.

## What's here

```
app/src/main/java/com/kyleyarwood/goalbingo/
├── GoalBingoApplication.kt     // owns the ServiceLocator
├── MainActivity.kt             // sets the Compose content
├── data/
│   ├── Goal.kt                 // sealed: Checkbox + Counter (Streak goes here later)
│   ├── Square.kt
│   ├── BingoCard.kt
│   ├── BingoLines.kt           // row/col/diagonal detection
│   ├── BingoRepository.kt      // swap-in interface
│   └── local/                  // Room implementation
├── di/ServiceLocator.kt        // manual DI
└── ui/
    ├── theme/
    ├── card/                   // grid + bingo highlighting
    ├── detail/                 // edit + increment one square
    ├── setup/                  // list view of all 25 goals
    └── nav/BingoNavHost.kt
```

## Not built yet

- **Streak / challenge goals** ("no takeout for a month") — placeholder noted
  in `Goal.kt`. Will need a new sealed variant + daily check-in UI + a
  reminder mechanism (probably `WorkManager`).
- Multi-year history (currently shows the current calendar year only).
- Cloud sync.
- Export / import.
- Reminders / notifications.

## Run tests

```
./gradlew test
```

`BingoLinesTest` covers row, column, and diagonal detection.
