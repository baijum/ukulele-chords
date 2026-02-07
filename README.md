# Ukulele Companion

A free, offline, ad-free Android app for learning ukulele — chords, scales, music theory, composition tools, and more. Built with **Kotlin** and **Jetpack Compose**.

## Features

### Interactive Fretboard Explorer
Tap fret positions on a visual ukulele fretboard (standard GCEA tuning, frets 0–12) and the app instantly detects and displays the chord. Supports 9 chord types: Major, Minor, Dom7, Min7, Maj7, Diminished, Augmented, Sus2, and Sus4.

### Chord Library
Browse playable voicings for any chord. Select a root note, category (Triad, Seventh, Suspended, Extended), and chord type to see algorithmically generated voicings displayed as mini fretboard diagrams.

### Transpose
Shift chords up or down by semitones with +/- buttons. Shows the capo equivalent for easy reference.

### Strumming Patterns
A reference guide with 8 common ukulele strumming patterns — from beginner (All Downs, Island Strum) to intermediate (Calypso, Ska). Each pattern includes visual beat arrows, notation, description, and suggested tempo.

### Chord Progressions
Common chord progressions for any key, in both major and minor scales. Includes Pop, Classic Rock, 50s, Folk, Jazz ii-V-I, Reggae, and more. Tap any chord chip to jump to its voicings in the Chord Library.

### Scale Overlay
Highlight notes from any of 7 scales (Major, Minor, Pentatonic Major/Minor, Blues, Dorian, Mixolydian) directly on the fretboard. Root notes are shown with a distinct color.

### Favorites
Long-press any voicing in the Chord Library to save it. Access your saved voicings from the dedicated Favorites tab.

### Song Chord Sheets
Create a personal songbook with lyrics and inline chord markers (e.g., `Some[C]where over the [Em]rainbow`). Tap any chord name in a sheet to view its voicings.

### Chord of the Day Widget
A home screen widget that displays a new chord each day with its name, finger positions, and notes. Tapping the widget opens the app.

### Sound Playback
Hear chords played back using sine wave synthesis. Notes are strummed with a configurable delay between strings.

### Settings
- **Display**: Sharp/Flat note names, Light/Dark/System theme
- **Tuning**: High-G (standard) or Low-G
- **Fretboard**: Left-handed mode (mirrors the fretboard)
- **Sound**: Enable/disable, strum delay, note duration

## Philosophy

- **Free forever** — no ads, no in-app purchases
- **Fully offline** — no internet required, no analytics, no tracking
- **No login** — just open and play
- **Educational** — designed for beginners and learners

## Tech Stack

| Component | Technology |
|-----------|-----------|
| Language | Kotlin |
| UI | Jetpack Compose + Material3 |
| Architecture | ViewModel + StateFlow |
| Audio | Android AudioTrack (sine wave synthesis) |
| Widget | Jetpack Glance |
| Persistence | SharedPreferences |
| Min SDK | 26 (Android 8.0) |
| Target SDK | 35 |

## Project Structure

```
com.baijum.ukufretboard
├── audio/              # Sine wave tone generation
├── data/               # Notes, chords, scales, progressions, patterns, persistence
├── domain/             # Chord detection, transposition, note/chord models
├── ui/                 # Compose screens and components
├── viewmodel/          # UI state management (ViewModel + StateFlow)
└── widget/             # Chord of the Day home screen widget
```

## Building

### Prerequisites

- Android Studio (latest stable)
- JDK 11+

### Debug Build

```bash
./gradlew assembleDebug
```

The APK will be at `app/build/outputs/apk/debug/app-debug.apk`.

### Release Build

1. Create a `keystore.properties` file in the project root:

```properties
storeFile=path/to/your/keystore.jks
storePassword=your_store_password
keyAlias=your_key_alias
keyPassword=your_key_password
```

2. Build the release bundle:

```bash
./gradlew bundleRelease
```

The AAB will be at `app/build/outputs/bundle/release/app-release.aab`.

## Attribution

Audio samples are from the "Ukelele single notes, close-mic" pack by
[stomachache](https://freesound.org/people/stomachache/packs/8545/) on
Freesound.org, licensed under
[CC BY 3.0](https://creativecommons.org/licenses/by/3.0/).
See [ATTRIBUTION.md](ATTRIBUTION.md) for full details.

## License

This project is provided as-is for educational purposes.
