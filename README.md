# SkyTycoon ✈

An Android airline simulator game built with Kotlin and Jetpack Compose.

---

## Table of Contents

1. [Overview](#overview)
2. [Getting Started](#getting-started)
3. [Features](#features)
4. [Architecture](#architecture)
5. [Project Structure](#project-structure)
6. [File Reference — Data Layer](#file-reference--data-layer)
7. [File Reference — Domain Layer](#file-reference--domain-layer)
8. [File Reference — UI Layer](#file-reference--ui-layer)
9. [Domain Models](#domain-models)
10. [Use Cases (Business Logic)](#use-cases-business-logic)
11. [Game Mechanics](#game-mechanics)
12. [Database Schema](#database-schema)
13. [Enum Types](#enum-types)
14. [JSON Assets](#json-assets)
15. [Navigation](#navigation)
16. [Dependency Injection](#dependency-injection)
17. [Testing](#testing)
18. [Tech Stack](#tech-stack)

---

## Overview

SkyTycoon is a turn-based airline management game where you build an aviation empire from scratch. Start with a small fleet and grow into a global operator by:

- Running **scheduled airline routes** between 250 real-world airports
- Fulfilling **charter contracts** for on-demand passenger transport
- Operating **helicopter services** for short-range VIP missions
- Managing **pilots, co-pilots, flight attendants, and mechanics**
- Completing **missions** to earn coins, reputation, and research points
- Advancing time manually — one hour at a time, or fast-forward 8 hours

There are two difficulty modes:
- **Realistic** — tighter economy, higher costs, crew assignment mandatory
- **Fictional** — relaxed economy, higher fill rates, more forgiving

---

## Getting Started

### Prerequisites

- Android Studio Hedgehog (2023.1.1) or newer
- JDK 17+
- Android SDK with `compileSdk = 34` and `minSdk = 26`

### Build

```bash
./gradlew assembleDebug
```

The APK will be at `app/build/outputs/apk/debug/app-debug.apk`.

### Run Tests

```bash
./gradlew test
```

Unit tests live in `app/src/test/`. The main suite is `ScheduleFlightUseCaseTest`, which validates all scheduling logic — aircraft availability, range checks, schedule conflicts, pilot fatigue, and contract handling — using fake in-memory repositories with no mocking framework required.

---

## Features

| Category | Details |
|---|---|
| Business lines | Airline (scheduled routes), Charter (contract-based), Helicopter (short-range) |
| Fleet | 25 aircraft models across 3 categories — airliners, charter planes, helicopters |
| Airports | 250 real-world airports with IATA/ICAO codes, GPS coordinates, city, country, region |
| Contracts | Auto-generated charter/helicopter contracts with deadlines and on-time bonuses |
| Employees | Pilot, Helicopter Pilot, Co-Pilot, Flight Attendant, Mechanic, Admin — each with fatigue tracking |
| Missions | Primary (campaign), Secondary (optional), Daily (reset each game day) |
| Time system | Manual time advance: +1 h or +8 h per press; 1 game day = 1440 game minutes |
| Economy | Coins-based economy scaled ÷100 from real prices; starting balance 5M (Realistic) / 12M (Fictional) |
| World map | 2D Mercator projection canvas; 250 airports plotted; active routes drawn per operation type |
| Reputation | 0–100 score; on-time landings +1, delays −2, cancellations −5; affects passenger fill rate |
| Maintenance | Aircraft condition degrades per flight hour; mandatory service every 100 flight hours |
| Dark theme | Custom colour palette — SkyBlack background, SkyAccentBlue/Green/Purple/Orange highlights |
| Bilingual | English and Brazilian Portuguese (pt-BR) string resources |

---

## Architecture

SkyTycoon follows **MVVM + Clean Architecture** with three strict layers:

```
┌─────────────────────────────────────────────────────────────┐
│  UI LAYER  (Jetpack Compose screens + ViewModels)           │
│  • Knows about domain models and use cases only             │
│  • ViewModels hold StateFlow<UiState> observed by screens   │
└───────────────────┬─────────────────────────────────────────┘
                    │ calls use cases (suspend / Flow)
┌───────────────────▼─────────────────────────────────────────┐
│  DOMAIN LAYER  (pure Kotlin — no Android dependencies)      │
│  • Domain models (data classes)                             │
│  • Repository interfaces                                    │
│  • Use cases (business rules)                               │
└───────────────────┬─────────────────────────────────────────┘
                    │ implements repository interfaces
┌───────────────────▼─────────────────────────────────────────┐
│  DATA LAYER  (Room database + JSON seeding)                 │
│  • Room entities, DAOs, AppDatabase                         │
│  • Repository implementations                               │
│  • JSON → Entity conversion at first launch                 │
└─────────────────────────────────────────────────────────────┘
```

### Key patterns

- **StateFlow** — each ViewModel exposes a single `StateFlow<XxxUiState>` data class; the screen collects it with `collectAsStateWithLifecycle()`.
- **Use cases** — every user-visible action is a single `suspend operator fun invoke(…)` class. The ViewModel calls them on `viewModelScope` via `Dispatchers.IO`.
- **Repository interfaces** — the domain layer depends only on interfaces (e.g., `FlightRepository`); the data layer provides `@Singleton` Hilt-injected implementations.
- **UseCaseResult** — a sealed class (`Success<T>` / `Failure(message)`) returned by mutating use cases so the ViewModel can surface errors to the snackbar without crashing.

---

## Project Structure

```
app/src/main/
├── assets/
│   ├── airports.json             250 real airports (IATA, ICAO, lat/lon, city, country, region, hasHelipad)
│   └── aircraft_models.json      25 aircraft models (specs, prices, category)
│
└── java/com/skytycoon/app/
    ├── config/
    │   └── GameBalanceConfig.kt  All tunable numbers — costs, rates, timings
    │
    ├── data/
    │   ├── local/
    │   │   ├── AppDatabase.kt    Room @Database — lists all entities and DAOs
    │   │   ├── Converters.kt     Room TypeConverter — AircraftCategory ↔ String
    │   │   ├── dao/              One DAO interface per entity (CRUD + queries)
    │   │   └── entity/           Room @Entity data classes (one per DB table)
    │   └── repository/           Repository implementations (interface + @Singleton class)
    │
    ├── di/
    │   ├── DatabaseModule.kt     Hilt @Module — provides AppDatabase and all DAOs
    │   └── RepositoryModule.kt   Hilt @Module — binds repository interfaces to implementations
    │
    ├── domain/
    │   ├── model/                Pure Kotlin data classes (no Android/Room imports)
    │   └── usecase/              One class per business action
    │
    └── ui/
        ├── components/           Reusable Compose composables used across screens
        ├── navigation/
        │   ├── Screen.kt         Sealed class listing all route strings
        │   └── NavGraph.kt       NavHost with all composable destinations + SkyBottomNavBar
        ├── screens/              One sub-package per screen (Screen.kt + ViewModel.kt)
        ├── theme/                Colour tokens, typography, Material3 theme
        └── utils/
            └── GameTimeUtils.kt  Converts game minutes to "Day 3 — 14:30" display strings
```

---

## File Reference — Data Layer

### `config/GameBalanceConfig.kt`

Central object holding every numeric constant that controls game feel:

| Constant | Value | Purpose |
|---|---|---|
| `ADVANCE_STEP_MINUTES` | 60 | Game minutes added per normal time press |
| `FAST_ADVANCE_STEP_MINUTES` | 480 | Game minutes added per fast press (8 h) |
| `STARTING_BALANCE_REALISTIC` | 5,000,000 | Coins at new game (Realistic) |
| `STARTING_BALANCE_FICTIONAL` | 12,000,000 | Coins at new game (Fictional) |
| `FATIGUE_PER_FLIGHT_HOUR` | 8 | Fatigue points added per flight hour |
| `FATIGUE_RECOVERY_PER_GAME_HOUR` | 20 | Fatigue points removed per rest hour |
| `MAINTENANCE_INTERVAL_HOURS` | 100.0 | Flight hours between required maintenance |
| `CONDITION_LOSS_PER_FLIGHT_HOUR` | 0.8 | Aircraft condition % lost per flight hour |
| `MAX_AVAILABLE_CONTRACTS` | 12 | Max contracts visible at once |
| `CONTRACT_EXPIRY_GAME_HOURS` | 48 | Hours before an unclaimed contract disappears |

To change the game's difficulty curve, only this file needs editing.

---

### `data/local/AppDatabase.kt`

The Room database class. Annotated with `@Database` listing all eight entity classes. Uses `Converters` for the `AircraftCategory` enum. Version is tracked here — bump it whenever an entity changes and add a migration or `fallbackToDestructiveMigration`.

### `data/local/Converters.kt`

Room TypeConverter for `AircraftCategory`. Converts the enum to/from a String column so Room can persist it. Only `AircraftCategory` needs this treatment; all other enums are stored as their `.name` String in the entity and safely decoded in each entity's `toDomain()` method using `Enum.entries.firstOrNull { it.name == value }`.

---

### Entity files (`data/local/entity/`)

Each entity is a data class annotated with `@Entity`. They mirror the domain model but use only Room-compatible types (no enums directly — stored as String except `AircraftCategory`). Every entity provides:

- **`toDomain()`** — converts the entity to the corresponding domain model. Enum fields use `entries.firstOrNull` with a safe fallback to prevent crashes from unexpected DB values.
- **`companion object { fun fromDomain(model) }`** — converts a domain model to the entity for insert/update.

| Entity file | Table | Key fields |
|---|---|---|
| `AircraftModelEntity.kt` | `aircraft_models` | id (Int), manufacturer, model, category (AircraftCategory via TypeConverter), passengerCapacity, rangeKm, cruiseSpeedKmh, purchasePriceCoins, leasingCostPerHourCoins |
| `OwnedAircraftEntity.kt` | `owned_aircraft` | id (autoGen), modelId (FK→aircraft_models), registrationCode, acquisitionType (String), condition (0–100), totalFlightHours, nextMaintenanceHours |
| `FlightEntity.kt` | `flights` | id (autoGen), operationType (String), aircraftId (FK), originIata, destinationIata, departureGameMinutes, arrivalGameMinutes, passengerCount, revenueCoins, status (String), assignedPilotId, assignedCopilotId, contractId |
| `ContractEntity.kt` | `contracts` | id (autoGen), operationType (String), originIata, destinationIata, passengerCount, totalValueCoins, bonusOnTimeCoins, deadlineGameMinutes, status (String), assignedFlightId |
| `EmployeeEntity.kt` | `employees` | id (autoGen), name, type (String), level (1–5), dailySalaryCoins, fatigue (0–100), currentFlightId |
| `MissionEntity.kt` | `missions` | id (autoGen), type (String), title, description, targetValue, currentValue, status (String), operationType (String?), expiresAtGameDay |
| `AirportEntity.kt` | `airports` | id (Int), iata, icao, name, city, country, region, latitude, longitude, hasHelipad |
| `GameStateEntity.kt` | `game_state` | id=1 (single row), companyName, gameMode (String), balanceCoins, reputation, researchPoints, currentGameMinutes, dayNumber |

---

### DAO files (`data/local/dao/`)

One Kotlin interface per entity, annotated with `@Dao`. Each exposes:

- `getAll(): Flow<List<XxxEntity>>` — live reactive query; Room re-emits whenever the table changes
- `getById(id): XxxEntity?` — single row lookup
- `insert(entity): Long` — returns the new row ID
- `update(entity)` — replaces existing row by primary key
- `delete(entity)` — removes a row
- Specialised queries as needed, e.g., `getByStatus(status)`, `getAvailable()`, `expireOld(nowMinutes)`

All DAOs are injected into their matching repository via Hilt.

---

### Repository files (`data/repository/`)

Each repository file contains:
1. An **interface** (in the domain layer style) — defines what operations are possible.
2. A `@Singleton` **implementation class** `@Inject constructor(private val dao: XxxDao)` that delegates to the DAO and maps entities ↔ domain models.

| Repository | Responsibility |
|---|---|
| `AircraftModelRepository` | Reads aircraft model catalogue; seeds from `aircraft_models.json` on first launch |
| `OwnedAircraftRepository` | CRUD for the player's owned/leased aircraft fleet |
| `FlightRepository` | CRUD for scheduled/active/completed flights; query by status or aircraft |
| `ContractRepository` | CRUD for charter/helicopter contracts; marks expired ones |
| `EmployeeRepository` | CRUD for employees; query by type, availability |
| `MissionRepository` | CRUD for missions; deletes expired daily missions |
| `AirportRepository` | Read-only; seeds from `airports.json` on first launch |
| `GameStateRepository` | Single-row read/write for the global game state (balance, day, reputation) |

---

## File Reference — Domain Layer

### `domain/model/`

Pure Kotlin data classes — no Android or Room imports. They are the currency between layers.

| File | What it models |
|---|---|
| `GameState.kt` | Global player state: company name, mode, balance, reputation, research points, current game time |
| `AircraftModel.kt` | A catalogue entry (specs, prices). Has `displayName` computed property and `isCapableOfRoute(km)` helper |
| `OwnedAircraft.kt` | A specific aircraft owned by the player. Has `isOperational` (condition > 20) and `isMaintenanceDue` helpers |
| `Flight.kt` | One scheduled or active flight. Has `isActive` (SCHEDULED/BOARDING/IN_FLIGHT/DELAYED) and `isFinished` helpers |
| `Contract.kt` | A charter or helicopter job offer with value, deadline, and status |
| `Employee.kt` | A staff member with type, level, fatigue, and optionally an assigned flight. Has `isAvailable` and `canFlyAircraftCategory` helpers |
| `Airport.kt` | One airport entry from the JSON seed. Has `distanceKmTo(other)` Haversine calculation |
| `Mission.kt` | An objective with progress tracking, rewards, and expiry |
| `UseCaseResult.kt` | `sealed class UseCaseResult<T>` with `Success(data: T)` and `Failure(message: String)` — used by all mutating use cases |
| `Enums.kt` | All game enums: `GameMode`, `OperationType`, `AircraftCategory`, `FlightStatus`, `EmployeeType`, `MissionType`, `MissionStatus`, `AcquisitionType`, `ContractStatus` |

---

### `domain/usecase/`

Each use case is a single class with `@Inject constructor` and `suspend operator fun invoke(…): Result`. They run on `Dispatchers.IO`. Logic is fully isolated from UI.

| Use case | What it does |
|---|---|
| `StartNewGameUseCase` | Creates the initial `GameState`, inserts it into the DB, seeds missions |
| `SeedDataUseCase` | Calls `AircraftModelRepository.seedIfEmpty` and `AirportRepository.seedIfEmpty` on app startup |
| `ScheduleFlightUseCase` | Validates aircraft availability, route range, schedule conflicts, pilot qualification (Realistic), calculates arrival time and revenue, inserts the flight, and marks the contract ACCEPTED |
| `PurchaseAircraftUseCase` | Checks balance, deducts price (purchased) or accepts a lease, inserts the owned aircraft record |
| `HireEmployeeUseCase` | Validates the player can afford the hiring fee, deducts it, inserts the employee |
| `AdvanceTimeUseCase` | Core game loop: advances game minutes, transitions SCHEDULED→IN_FLIGHT→COMPLETED, pays salaries and leasing costs, updates aircraft condition, adjusts crew fatigue, generates daily missions, expires old contracts |
| `GenerateContractsUseCase` | Fills available contract slots up to `MAX_AVAILABLE_CONTRACTS` with randomly generated charter/helicopter contracts between valid airports |

---

## File Reference — UI Layer

### `ui/navigation/Screen.kt`

Sealed class listing every screen route string:

```
Splash → NewGame → Dashboard ↔ Fleet ↔ Schedule ↔ Employees ↔ Missions ↔ Map
```

### `ui/navigation/NavGraph.kt`

Sets up the `NavHost` with all eight `composable(…)` destinations. Also defines `SkyBottomNavBar` — the bottom navigation bar shown on all main screens (Dashboard, Fleet, Schedule, Employees, Missions, Map). It highlights the currently active route and navigates with `popUpTo` + `launchSingleTop` to avoid back-stack duplication.

---

### `ui/theme/`

| File | Purpose |
|---|---|
| `Color.kt` | Defines all named colour tokens: `SkyBlack`, `SkyDarkBlue`, `SkyCardBg`, `SkyAccentBlue`, `SkyAccentGreen`, `SkyAccentPurple`, `SkyAccentOrange`, `SkyAccentRed`, `SkyGold`, `SkyTextPrimary`, `SkyTextSecondary`, `SkyDivider` |
| `Type.kt` | Material3 typography overrides (uses default fonts with custom sizes) |
| `Theme.kt` | `SkyTycoonTheme` composable — applies dark colour scheme using the above tokens |

---

### `ui/components/`

Shared composables used across multiple screens:

| Component | Purpose |
|---|---|
| `SkyCard` | Styled card wrapper with dark background (`SkyCardBg`), rounded corners, optional `highlighted` border. Wraps content in a `Column` with 12 dp padding. |
| `RouteMapCanvas` | Custom `Canvas`-based 2D world map. Draws a Mercator projection background, plots airport dots, and draws coloured route lines for active flights (blue = airline, green = charter, purple = helicopter). Tap handling calls back `onAirportTap`. |
| `TimeAdvanceBar` | Bottom bar on the Dashboard showing current game time, day number, and two advance buttons (1h / 8h). |
| `StatTile` | A small card showing an icon, label, value, and optional coloured value text — used on the Dashboard for balance, reputation, etc. |

---

### `ui/utils/GameTimeUtils.kt`

Extension function `Long.toGameTimeString()` that converts raw game minutes (e.g., `1530`) to a human-readable string like `"Day 2 — 02:30"`. Used on flight cards, contract deadlines, and the Dashboard time display.

---

### Screen packages (`ui/screens/`)

Each screen package contains exactly two files: `XxxScreen.kt` (the Compose UI) and `XxxViewModel.kt` (the state holder).

---

#### `screens/splash/`

**`SplashViewModel.kt`**
Runs a 1500 ms delay on init, then checks `GameStateRepository` for an existing save. Sets `navDestination` to `Screen.Dashboard.route` (returning player) or `Screen.NewGame.route` (first launch). Exposes `clearNavDestination()` so the screen resets the value after navigating (prevents re-navigation if the composable recomposes).

**`SplashScreen.kt`**
Shows an animated logo (scale + fade in), the SkyTycoon name with a blue-to-purple gradient, and the tagline. Collects `navDestination` and navigates away once set, popping the entire back stack.

---

#### `screens/newgame/`

**`NewGameViewModel.kt`**
Holds the company name input and selected game mode. On submit, calls `StartNewGameUseCase` and `SeedDataUseCase`, then sets `navDestination` to Dashboard.

**`NewGameScreen.kt`**
Text field for company name, two mode selection buttons (Realistic / Fictional), and a Start button. Disabled until a non-blank name is entered.

---

#### `screens/dashboard/`

**`DashboardViewModel.kt`**
Collects `GameState`, all flights, and all aircraft via their repositories. Exposes `onAdvanceTime(fast)` which calls `AdvanceTimeUseCase` and then `GenerateContractsUseCase` to refill contracts after each time step.

**`DashboardScreen.kt`**
Main hub screen. Shows:
- Company name and current game time/day
- Stat tiles: balance, reputation, research points, active flights
- Maintenance alert section (aircraft where `isMaintenanceDue`)
- A mini route map (`RouteMapCanvas`) in a card
- `TimeAdvanceBar` at the bottom

---

#### `screens/fleet/`

**`FleetViewModel.kt`**
Loads owned aircraft and aircraft models. Handles `onPurchase` / `onLease` calls via `PurchaseAircraftUseCase`. Emits `successMsg` / `errorMsg` for snackbar feedback. `clearSuccessMessage()` and `clearErrorMessage()` are called independently from each `LaunchedEffect` to prevent race conditions.

**`FleetScreen.kt`**
Three tabs: Airliners, Charter, Helicopters. Each tab lists owned aircraft in that category. A FAB opens the purchase/lease dialog. The dialog has a model selector (dropdown populated from the catalogue), acquisition type toggle, and a registration code field.

---

#### `screens/schedule/`

**`ScheduleViewModel.kt`**
Loads flights, contracts, airports, owned aircraft, and available employees. `onScheduleFlight(request)` calls `ScheduleFlightUseCase`. Emits success/error messages for snackbar.

**`ScheduleScreen.kt`**
Two tabs: **Flights** (current and upcoming flights) and **Contracts** (available charter/helicopter jobs). A FAB opens `ScheduleFlightSheet` — a `ModalBottomSheet` with:
- Aircraft dropdown
- IATA search fields with autocomplete (up to 6 suggestions shown as a regular Column — not LazyColumn, to avoid nesting scrollable crash)
- Departure hour and minute sliders (departure time is computed as `dayStartMinutes + hour×60 + minutes`, where `dayStartMinutes` is the midnight boundary of the current game day)
- Ticket price field (only shown for non-contract AIRLINE flights; Schedule button stays disabled if the input is non-numeric)
- Pilot dropdown

When a contract row is tapped in the Contracts tab, the sheet opens pre-filled with that `contractId`.

---

#### `screens/employees/`

**`EmployeesViewModel.kt`**
Loads all employees, filters by type tab. `onHire(name, type, level)` calls `HireEmployeeUseCase`. Emits success/error messages.

**`EmployeesScreen.kt`**
Lists employees by type (Pilots, Co-Pilots, Attendants, Mechanics, Admin). Each card shows name, level stars, fatigue bar, daily salary, and current flight assignment. A FAB opens the hire dialog with name field, type dropdown, and level slider.

---

#### `screens/missions/`

**`MissionsViewModel.kt`**
Loads all missions, filtered into three lists: primary, secondary, daily.

**`MissionsScreen.kt`**
Three tabs: Primary, Secondary, Daily. Each mission card shows title, description, animated progress bar, status badge, and reward chips (coins, reputation, research). The daily tab shows a "missions reset each game day" notice. The screen fills maximum available space so the empty-state message stays vertically centered.

---

#### `screens/map/`

**`MapViewModel.kt`**
Loads all airports and all active flights (status `isActive`). Exposes `onAirportSelected(airport?)` to show/hide the airport detail sheet.

**`MapScreen.kt`**
Full-screen `RouteMapCanvas` with a legend overlay (Airline = blue, Charter = green, Helicopter = purple). Tapping an airport shows a `ModalBottomSheet` with IATA code, full name, city/country, ICAO, region, helipad badge, and active flight count. `buildRoutes(airports, flights)` is a private helper that converts flight records into `(Airport, Airport)` pairs for the canvas.

---

## Domain Models

### GameState

```
id: Long                    Always 1 (single row)
companyName: String         Set by player at new game
gameMode: GameMode          REALISTIC or FICTIONAL
balanceCoins: Long          Current funds
reputation: Int             0–100
researchPoints: Int         Accumulated R&D points
currentGameMinutes: Long    Total game minutes elapsed (0 = game start)
dayNumber: Int              1-based current game day
```

### AircraftModel

Static catalogue entry. Key computed properties:
- `displayName` — `"$manufacturer $model"` (e.g., "Boeing 737-800")
- `isCapableOfRoute(distanceKm)` — returns `distanceKm <= rangeKm`

### OwnedAircraft

Player's aircraft. Key computed properties:
- `isOperational` — `condition > 20`
- `isMaintenanceDue` — `totalFlightHours >= nextMaintenanceHours`

### Flight

One trip record. Key computed properties:
- `durationMinutes` / `durationHours`
- `isActive` — status is SCHEDULED, BOARDING, IN_FLIGHT, or DELAYED
- `isFinished` — status is COMPLETED or CANCELLED

### Contract

A charter/helicopter job. Holds `totalValueCoins` (paid on completion), `bonusOnTimeCoins` (paid if flight lands before `deadlineGameMinutes`), and `status` (AVAILABLE → ACCEPTED → COMPLETED / FAILED / EXPIRED).

### Employee

Staff member. Key computed properties:
- `isAvailable` — `fatigue < 80 && currentFlightId == null`
- `canFlyAircraftCategory(category)` — checks type (PILOT can fly AIRLINER, HELICOPTER_PILOT can fly HELICOPTER)
- `dailySalaryCoins` — stored directly; set at hire time as `level × 50`

### Airport

Static seed data. `distanceKmTo(other: Airport)` uses the Haversine formula for great-circle distance.

### Mission

Tracks one objective. `progressPercent: Float` is `(currentValue / targetValue).coerceIn(0f, 1f)`.

---

## Use Cases (Business Logic)

### AdvanceTimeUseCase — The Game Loop

Called every time the player presses an advance button. Steps:

1. Read current `GameState`.
2. Calculate `step` (60 or 480 minutes) and `newMinutes`.
3. If day boundary crossed:
   - Add employee salaries (daily × days passed)
   - Add research points
   - Deduct leasing costs for leased aircraft
   - Delete expired daily missions; generate new ones for the **current** day only
4. Transition SCHEDULED flights whose `departureGameMinutes ≤ newMinutes` to IN_FLIGHT; assign crew.
5. Complete IN_FLIGHT flights whose `arrivalGameMinutes ≤ newMinutes`:
   - Credit `revenueCoins` to balance
   - Increment reputation
   - Degrade aircraft condition; advance `totalFlightHours`; schedule next maintenance
   - Update crew fatigue: add flight-hours fatigue, subtract rest credit based on time elapsed **after landing** (not the full step)
6. Expire contracts past their deadline.
7. Save updated `GameState`.

### ScheduleFlightUseCase — Scheduling a Flight

Validates in sequence:
1. Aircraft exists and `isOperational`
2. Origin and destination airports exist and are different
3. Route distance ≤ aircraft range
4. No overlapping flights for this aircraft
5. In Realistic mode: pilot assigned, not fatigued, qualified for aircraft category
6. Revenue calculated: `passengerCount × ticketPrice` (AIRLINE) or `contract.totalValueCoins` (CHARTER/HELICOPTER)
7. Flight inserted; contract marked ACCEPTED

Returns `UseCaseResult.Success(flight)` or `UseCaseResult.Failure(message)`.

---

## Game Mechanics

### Time

- Time is stored as **game minutes since day 1 start** (a simple `Long`).
- `1440 game minutes = 1 game day`.
- `currentGameMinutes / 1440 + 1 = dayNumber`.
- The player presses a button to advance. There is no real-time clock running in the background.

### Money

- All prices are in **coins** — real-world values divided by 100.
- A Boeing 737 costs ~5,000,000 coins (≈ $500M real ÷ 100).
- Starting balance: 5M (Realistic) or 12M (Fictional).

### Reputation (0–100)

| Event | Change |
|---|---|
| On-time landing | +1 |
| Delay | −2 |
| Cancellation | −5 |
| Contract completed | +2 |

Reputation affects the **passenger fill rate** — the percentage of seats sold per flight:

| Reputation | Realistic fill % | Fictional fill % |
|---|---|---|
| 90–100 | 92% | ~129% (capped) |
| 75–89 | 82% | ~115% |
| 60–74 | 70% | 98% |
| 40–59 | 55% | 77% |
| 20–39 | 38% | 53% |
| 0–19 | 20% | 28% |

### Fatigue

- Each flight hour adds **8 fatigue points** to the assigned crew.
- After landing, rest begins: **20 fatigue points recovered per game hour** of post-landing rest within the same advance step.
- A crew member with fatigue ≥ 80 is marked unavailable for new assignments.

### Aircraft Condition

- Starts at 100%.
- Loses **0.8% per flight hour**.
- When `totalFlightHours >= nextMaintenanceHours` (every 100 flight hours), `nextMaintenanceHours` is pushed forward 100 h.
- Aircraft with condition ≤ 20 are `isOperational = false` and cannot be scheduled.

---

## Database Schema

Room database version 1. Eight tables:

```
game_state (id PK=1, companyName, gameMode, balanceCoins, reputation, researchPoints,
            currentGameMinutes, dayNumber)

aircraft_models (id PK, manufacturer, model, category, passengerCapacity, rangeKm,
                 cruiseSpeedKmh, fuelBurnLph, purchasePriceCoins, leasingCostPerHourCoins,
                 maintenanceCostPerHourCoins, imageResName)

owned_aircraft (id PK autoGen, modelId FK→aircraft_models, registrationCode, acquisitionType,
                condition, totalFlightHours, nextMaintenanceHours)

flights (id PK autoGen, operationType, aircraftId FK→owned_aircraft, originIata,
         destinationIata, departureGameMinutes, arrivalGameMinutes, passengerCount,
         revenueCoins, status, assignedPilotId FK→employees, assignedCopilotId FK→employees,
         contractId FK→contracts)

contracts (id PK autoGen, operationType, originIata, destinationIata, passengerCount,
           totalValueCoins, bonusOnTimeCoins, deadlineGameMinutes, status, assignedFlightId)

employees (id PK autoGen, name, type, level, dailySalaryCoins, fatigue, currentFlightId)

missions (id PK autoGen, type, title, description, rewardMoneyCoins, rewardReputation,
          rewardResearchPoints, targetValue, currentValue, status, operationType, expiresAtGameDay)

airports (id PK, iata, icao, name, city, country, region, latitude, longitude, hasHelipad)
```

---

## Enum Types

All enums are defined in `domain/model/Enums.kt`:

| Enum | Values |
|---|---|
| `GameMode` | `REALISTIC`, `FICTIONAL` |
| `OperationType` | `AIRLINE`, `CHARTER`, `HELICOPTER` |
| `AircraftCategory` | `AIRLINER`, `CHARTER`, `HELICOPTER` |
| `FlightStatus` | `SCHEDULED`, `BOARDING`, `IN_FLIGHT`, `DELAYED`, `COMPLETED`, `CANCELLED` |
| `EmployeeType` | `PILOT`, `HELICOPTER_PILOT`, `COPILOT`, `FLIGHT_ATTENDANT`, `MECHANIC`, `ADMIN` |
| `MissionType` | `PRIMARY`, `SECONDARY`, `DAILY` |
| `MissionStatus` | `ACTIVE`, `COMPLETED`, `EXPIRED` |
| `AcquisitionType` | `PURCHASED`, `LEASED` |
| `ContractStatus` | `AVAILABLE`, `ACCEPTED`, `COMPLETED`, `FAILED`, `EXPIRED` |

Enums are stored in Room as their `.name` String. `toDomain()` on every entity uses `entries.firstOrNull { it.name == storedValue } ?: safeDefault` — if the DB ever contains an unrecognised string (e.g., after a migration), the app falls back gracefully instead of throwing `IllegalArgumentException`.

---

## JSON Assets

### `airports.json`

Array of 250 airport objects. Fields:

```json
{
  "id": 1,
  "iata": "GRU",
  "icao": "SBGR",
  "name": "Guarulhos International Airport",
  "city": "São Paulo",
  "country": "Brazil",
  "region": "South America",
  "latitude": -23.4356,
  "longitude": -46.4731,
  "hasHelipad": false
}
```

Seeded into the `airports` table on first launch by `AirportRepository.seedIfEmpty`. Never modified at runtime.

### `aircraft_models.json`

Array of 25 aircraft objects. Fields:

```json
{
  "id": 1,
  "manufacturer": "Boeing",
  "model": "737-800",
  "category": "AIRLINER",
  "passengerCapacity": 162,
  "rangeKm": 5765,
  "cruiseSpeedKmh": 842,
  "fuelBurnLph": 2630.0,
  "purchasePriceCoins": 5000000,
  "leasingCostPerHourCoins": 1200,
  "maintenanceCostPerHourCoins": 300,
  "imageResName": "aircraft_737_800"
}
```

`category` must match an `AircraftCategory` enum name exactly. Seeded into `aircraft_models` by `AircraftModelRepository.seedIfEmpty`.

---

## Navigation

Screen flow:

```
App Start
    │
    └─► Splash (1500 ms delay + DB check)
            │
            ├─► NewGame (no saved game) ──► Dashboard (after save)
            │
            └─► Dashboard (saved game exists)
                    │
                    └─► Bottom Nav: Dashboard ↔ Fleet ↔ Schedule ↔ Employees ↔ Missions ↔ Map
```

The bottom navigation bar is present on all six main screens. It uses `navController.navigate(route) { launchSingleTop = true; restoreState = true; popUpTo(graph) { saveState = true } }` to avoid creating duplicate back-stack entries and to restore scroll position when switching tabs.

---

## Dependency Injection

Hilt is used for DI (`@HiltAndroidApp` on `SkyTycoonApp`, `@AndroidEntryPoint` on `MainActivity`, `@HiltViewModel` on all ViewModels).

### `di/DatabaseModule.kt`

`@Module @InstallIn(SingletonComponent)` — provides:
- `provideDatabase(context)` → `AppDatabase` (Room `@Singleton`)
- One `provide[Xxx]Dao(db)` function per DAO (e.g., `provideFlightDao(db) = db.flightDao()`)

### `di/RepositoryModule.kt`

`@Module @InstallIn(SingletonComponent)` with `@Binds` — maps each repository interface to its implementation:
- `bindFlightRepository(impl: FlightRepositoryImpl): FlightRepository`
- (same pattern for all 8 repositories)

This allows use cases to depend on interfaces (testable with fakes) while Hilt injects the real Room-backed implementations at runtime.

---

## Testing

Tests are in `app/src/test/` (JVM unit tests, no Android emulator needed).

### `ScheduleFlightUseCaseTest`

Tests the full scheduling logic against fake in-memory repositories. Covers:

- Aircraft not found → `Failure`
- Aircraft not operational (low condition) → `Failure`
- Unknown origin / destination airport → `Failure`
- Same origin and destination → `Failure`
- Route exceeds aircraft range → `Failure`
- Schedule conflict with existing flight → `Failure`
- Missing pilot in Realistic mode → `Failure`
- Fatigued pilot → `Failure`
- Pilot not qualified for aircraft category → `Failure`
- Contract not found / already accepted → `Failure`
- Happy path (AIRLINE flight) → `Success` with correct revenue
- Happy path (CHARTER flight) → `Success` with contract marked ACCEPTED

No mocking framework used — fakes implement the repository interfaces directly.

---

## Tech Stack

| Layer | Technology | Version |
|---|---|---|
| Language | Kotlin | 1.9.24 |
| UI toolkit | Jetpack Compose + Material 3 | BOM 2024.06 |
| Architecture | MVVM + Clean Architecture | — |
| Dependency Injection | Hilt | 2.51.1 |
| Database | Room | 2.6.1 |
| Async | Coroutines + Flow | 1.8.0 |
| Navigation | Navigation Compose | 2.7.7 |
| JSON parsing | Gson | 2.10.1 |
| Build system | Gradle (Kotlin DSL) | 8.6 |
| Android Gradle Plugin | AGP | 8.3.2 |
| Min SDK | Android 8.0 (Oreo) | API 26 |
| Target / Compile SDK | Android 14 | API 34 |
