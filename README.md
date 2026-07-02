# SkyTycoon ✈

An Android airline simulator game built with Kotlin and Jetpack Compose.

## Overview

Build and manage your own aviation empire. Start with a small fleet and grow into a global airline by operating scheduled routes, charter flights, and helicopter services. Manage your crew, maintain your aircraft, and complete missions to earn rewards.

## Features

- **Three business lines**: Airline (scheduled routes), Charter (on-demand contracts), Helicopter (short-range operations)
- **Fleet management**: Purchase or lease 25 aircraft models across three categories — airliners, charter planes, and helicopters
- **Dynamic contracts**: Auto-generated contracts with deadlines and on-time bonuses
- **Employee system**: Hire pilots, co-pilots, flight attendants, and mechanics; track fatigue
- **Mission system**: Primary, Secondary, and Daily objectives with coin/reputation/research rewards
- **Manual time advance**: Step forward 1 hour or fast-forward 8 hours at a time
- **2D world map**: Mercator projection canvas with 250 real airports; tap any airport for details
- **Two game modes**: Realistic (harder economy) and Arcade (more forgiving)
- **Dark theme** with custom colour palette
- **Bilingual**: English and Brazilian Portuguese (pt-BR)

## Tech Stack

| Layer | Technology |
|---|---|
| Language | Kotlin 1.9 |
| UI | Jetpack Compose + Material 3 |
| Architecture | MVVM + Clean Architecture |
| DI | Hilt 2.51 |
| Database | Room 2.6 |
| Async | Coroutines + Flow |
| Navigation | Navigation Compose 2.7 |
| Min SDK | 26 (Android 8.0) |
| Target SDK | 34 (Android 14) |

## Project Structure

```
app/src/main/java/com/skytycoon/app/
├── config/          # Game balance constants
├── data/
│   ├── local/       # Room database, entities, DAOs
│   └── repository/  # Repository implementations
├── di/              # Hilt modules
├── domain/
│   ├── model/       # Domain models and enums
│   └── usecase/     # Business logic use cases
└── ui/
    ├── components/  # Shared Compose components
    ├── navigation/  # NavGraph and bottom nav
    ├── screens/     # One package per screen
    ├── theme/       # Colours, typography, theme
    └── utils/       # Game time formatting
```

## Screens

- **Splash** — animated logo, auto-navigates based on saved game state
- **New Game** — company name input and game mode selection
- **Dashboard** — key stats, maintenance alerts, and mini route map
- **Fleet** — owned aircraft by category, purchase/lease dialog
- **Schedule** — active flights and available contracts; schedule new flights via bottom sheet
- **Employees** — staff list with fatigue bars, hire dialog
- **Missions** — tabbed view of Primary / Secondary / Daily objectives
- **Map** — full-screen world map with active route overlay

## Data Assets

- `airports.json` — 250 real-world airports with IATA/ICAO codes, coordinates, and region
- `aircraft_models.json` — 25 aircraft models with performance specs and economy values

## Running Tests

```bash
./gradlew test
```

The unit test suite (`ScheduleFlightUseCaseTest`) covers the core flight scheduling logic using fake in-memory repositories — no mocking framework required.

## Building

```bash
./gradlew assembleDebug
```

Requires Android SDK with `compileSdk = 34` and `minSdk = 26`.
