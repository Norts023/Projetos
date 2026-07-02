# SkyTycoon — Game Design Document (v0.1)

## 1. Vision & Core Loop

**SkyTycoon** is a turn-based airline management sim where every "turn" is a manual time advance (+1h or +8h). The player runs a company with three business lines: a regular airline, an air taxi (charter), and helicopter operations.

**Core loop:**
```
Acquire Aircraft → Assign Crew → Schedule Flights / Accept Contracts
    → Advance Time → Collect Revenue → Pay Costs
    → Monitor Reputation → Expand Fleet → Unlock New Routes
```

**Win condition:** There is no hard end — the player grows their empire from a single charter flight to a global airline network.

---

## 2. Game Modes

| Feature | Realistic | Arcade |
|---|---|---|
| Starting Balance | 5,000,000¢ | 12,000,000¢ |
| Fuel Cost | 0.8¢/L (scaled) | 0.3¢/L |
| Crew Requirement | Mandatory per aircraft type | Optional |
| Demand Fill Rate | 1.0× | 1.4× |
| Maintenance Cost | 1.0× | 0.6× |
| Penalty for failures | Full | Halved |

Both modes use the same `GameBalanceConfig` — the mode just selects different multiplier values.

---

## 3. Economy Scale

**Scale factor: ÷100** from real-world prices.

| Aircraft | Real Price | In-Game (¢) | Starting Balance Coverage |
|---|---|---|---|
| Robinson R44 | $280k | 2,800¢ | ~1,785× |
| Cessna 172 | $380k | 3,800¢ | ~1,315× |
| King Air 350 | $7.5M | 75,000¢ | ~66× |
| A320-200 | $98M | 980,000¢ | ~5× |

**Progression tiers:**
- Days 1–10: Charter/helicopter ops with small aircraft (3,800–29,000¢)
- Days 10–30: Regional jets — ATR 72, E175 (270,000–400,000¢)
- Days 30+: Narrow-body airliners — A320, 737 (960,000–1,330,000¢)

---

## 4. Three Business Lines

### 4.1 Airline (Regular Routes)
- Fixed routes with scheduled departures
- Revenue = passengers × ticket price
- Demand = base fill rate (reputation) × seat capacity
- High capacity, high revenue, high complexity
- Requires: 2 pilots (Realistic), 1 flight attendant per 50 pax

### 4.2 Air Taxi / Charter
- Dynamically generated contracts: "Fly 5 pax from GRU→SDU by 18:00"
- Revenue = fixed contract value + 20% on-time bonus
- Low capacity (1–12 pax), fast turnover, lower barrier to entry
- Good for early game: buy a Cessna 172 (3,800¢) and start immediately
- Requires: 1 pilot

### 4.3 Helicopter
- Short routes (< 700 km), special contract types:
  - Panoramic tourism
  - VIP transfer
  - Airport shuttle (helipad → city center)
- Highest cost per seat-km, but fastest turnaround
- Requires: 1 helicopter pilot
- **Reputation effect:** Helicopter ops gain +2 rep per successful run (premium brand)

**Cross-line reputation effects:**
- Every on-time completion: +1 reputation
- Cancellation: -5 reputation
- Late arrival: -2 reputation
- Reputation unlocks better fill rates and higher-value contracts

---

## 5. Architecture

```
app/
├── config/          GameBalanceConfig (single source of truth for numbers)
├── di/              DatabaseModule, RepositoryModule (Hilt)
├── domain/
│   ├── model/       Pure Kotlin data classes + enums
│   └── usecase/     Business logic, no Android deps
├── data/
│   ├── local/       Room entities, DAOs, AppDatabase
│   └── repository/  Interface + Impl (with context for asset seeding)
└── ui/
    ├── theme/        Dark game theme (Color, Type, Theme)
    ├── navigation/   NavGraph, Screen sealed class
    ├── screens/      7 screens, each with ViewModel
    └── components/   SkyCard, StatTile, RouteMapCanvas, TimeAdvanceBar
```

**Key patterns:**
- Repository pattern: domain only touches interfaces
- Use cases: single-responsibility, injectable, testable
- ViewModels expose `StateFlow<UiState>` via `.stateIn()`
- Time is stored as `Long` (game minutes from 0), 1440 min = 1 game day

---

## 6. Data Model Overview

```
GameState (1 row) ─── balanceCoins, reputation, currentGameMinutes, dayNumber

OwnedAircraft ──────── modelId → AircraftModel (seed from assets)
                       condition, totalFlightHours, nextMaintenanceHours

Flight ─────────────── aircraftId, origin/destinationIata
                       departure/arrivalGameMinutes, status
                       contractId? (if charter/helicopter)

Contract ───────────── operationType, route, value, deadline, status

Employee ───────────── type, level, fatigue, currentFlightId?

Mission ────────────── type (PRIMARY/SECONDARY/DAILY), progress, rewards

Airport ────────────── seed from assets/airports.json (250 airports, offline)
AircraftModel ──────── seed from assets/aircraft_models.json (25 models)
```

**Seed versioning:** `AirportDao.count()` and `AircraftModelDao.count()` guard against re-seeding. Future schema upgrades use Room migrations. Seed data versioning via `assets/data_version.json` (planned v0.2).

---

## 7. Time System

**Manual advance** (no background service needed):
- `+1h` button: advance 60 game minutes
- `+8h` button: advance 480 game minutes  
- On each advance, `AdvanceTimeUseCase` runs synchronously:
  1. Update flight statuses (SCHEDULED → IN_FLIGHT → COMPLETED)
  2. Pay revenues and deduct costs
  3. Update aircraft condition and crew fatigue
  4. Check day rollover → pay salaries, generate dailies
  5. Expire old contracts

**Game time display:** `HH:MM Day N` — readable always-on clock.

---

## 8. Mission System

| Type | Count | Reset | Anti-abuse |
|---|---|---|---|
| Primary | 3 | Never | One-time completion |
| Secondary | ~10 | Never | Repeatable in v0.2 |
| Daily | 3 | Each game day | Persist `lastDailyResetDay` in GameState |

**Example missions per line:**

| Mission | Type | Op | Goal | Reward |
|---|---|---|---|---|
| First Flight | Primary | AIRLINE | Complete 1 airline flight | 10,000¢ + 5 rep |
| Charter Starter | Primary | CHARTER | Complete 3 charter contracts | 25,000¢ + 3 rep |
| Helicopter Hero | Primary | HELICOPTER | Complete 5 helicopter ops | 15,000¢ + 8 rep |
| Punctual | Secondary | Any | 10 on-time arrivals | 5,000¢ + 5 RP |
| Fleet Builder | Secondary | — | Own 3 aircraft | 20,000¢ |
| Daily Charter | Daily | CHARTER | 2 charters today | 2,000¢ |
| Heli Run | Daily | HELICOPTER | 1 helicopter flight today | 1,500¢ |

---

## 9. Map (2D Canvas)

- Mercator projection: `x = (lon + 180) / 360 * width`, `y = (1 - (lat + 90) / 180) * height`
- Airports: white dots, tappable with info popup
- Active routes: colored animated dashed lines (blue=AIRLINE, green=CHARTER, purple=HELICOPTER)
- No external map library — pure Compose `Canvas` / `DrawScope`
- Offline forever

---

## 10. Roadmap

### v0.1 — MVP (this release)
- [x] New game + mode selection
- [x] Fleet management (buy/lease all 3 categories)
- [x] Manual time advance
- [x] Airline + Charter + Helicopter scheduling
- [x] Dynamic contract generation
- [x] Employee hiring + fatigue
- [x] Mission system (3 types)
- [x] 2D world map
- [x] 250 real airports, 25 aircraft models

### v0.2 — Core Expansion
- [ ] Recurring flights (daily/weekly schedule)
- [ ] Research tree (unlock discounts, new routes, VIP tier)
- [ ] Weather events (delay chance modifier)
- [ ] Route demand analytics chart
- [ ] Leaderboard (offline high scores)
- [ ] More airports (1,000+) via downloadable pack

### v0.3 — Economy Depth
- [ ] Alliances / codeshares
- [ ] Aircraft upgrade system (refurbishment)
- [ ] Staff training (level up employees)
- [ ] Dynamic fuel pricing
- [ ] Competitor AI airlines

### v0.4 — Polish
- [ ] Achievements (Google Play Games)
- [ ] Sound effects + ambient audio
- [ ] Dark/light theme option
- [ ] Tablet layout
- [ ] Cloud save
