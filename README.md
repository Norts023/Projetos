# SkyTycoon ✈

Um simulador de companhia aérea para Android desenvolvido com Kotlin e Jetpack Compose.

---

## Índice

1. [Visão Geral](#visão-geral)
2. [Como Começar](#como-começar)
3. [Funcionalidades](#funcionalidades)
4. [Arquitetura](#arquitetura)
5. [Estrutura do Projeto](#estrutura-do-projeto)
6. [Referência de Arquivos — Camada de Dados](#referência-de-arquivos--camada-de-dados)
7. [Referência de Arquivos — Camada de Domínio](#referência-de-arquivos--camada-de-domínio)
8. [Referência de Arquivos — Camada de UI](#referência-de-arquivos--camada-de-ui)
9. [Modelos de Domínio](#modelos-de-domínio)
10. [Casos de Uso (Regras de Negócio)](#casos-de-uso-regras-de-negócio)
11. [Mecânicas do Jogo](#mecânicas-do-jogo)
12. [Esquema do Banco de Dados](#esquema-do-banco-de-dados)
13. [Tipos Enum](#tipos-enum)
14. [Assets JSON](#assets-json)
15. [Navegação](#navegação)
16. [Injeção de Dependências](#injeção-de-dependências)
17. [Testes](#testes)
18. [Stack Tecnológica](#stack-tecnológica)

---

## Visão Geral

SkyTycoon é um jogo de gerenciamento de companhia aérea por turnos onde você constrói um império da aviação do zero. Comece com uma frota pequena e cresça até se tornar um operador global:

- Operando **rotas aéreas regulares** entre 250 aeroportos reais do mundo
- Cumprindo **contratos charter** para transporte de passageiros sob demanda
- Operando **serviços de helicóptero** para missões VIP de curta distância
- Gerenciando **pilotos, copilotos, comissários e mecânicos**
- Completando **missões** para ganhar moedas, reputação e pontos de pesquisa
- Avançando o tempo manualmente — uma hora por vez ou avanço rápido de 8 horas

Existem dois modos de dificuldade:
- **Realista** — economia mais apertada, custos mais altos, atribuição de tripulação obrigatória
- **Ficcional** — economia mais relaxada, maiores taxas de ocupação, mais tolerante

---

## Como Começar

### Pré-requisitos

- Android Studio Hedgehog (2023.1.1) ou mais recente
- JDK 17+
- Android SDK com `compileSdk = 34` e `minSdk = 26`

### Compilar

```bash
./gradlew assembleDebug
```

O APK ficará em `app/build/outputs/apk/debug/app-debug.apk`.

### Executar Testes

```bash
./gradlew test
```

Os testes unitários estão em `app/src/test/`. A suíte principal é `ScheduleFlightUseCaseTest`, que valida toda a lógica de agendamento — disponibilidade de aeronave, verificações de alcance, conflitos de horário, fadiga do piloto e tratamento de contratos — usando repositórios falsos em memória sem necessidade de framework de mock.

---

## Funcionalidades

| Categoria | Detalhes |
|---|---|
| Linhas de negócio | Aérea (rotas regulares), Charter (baseado em contrato), Helicóptero (curta distância) |
| Frota | 25 modelos de aeronaves em 3 categorias — aviões de linha, aviões charter, helicópteros |
| Aeroportos | 250 aeroportos reais com códigos IATA/ICAO, coordenadas GPS, cidade, país, região |
| Contratos | Contratos charter/helicóptero gerados automaticamente com prazos e bônus por pontualidade |
| Funcionários | Piloto, Piloto de Helicóptero, Copiloto, Comissário, Mecânico, Admin — cada um com rastreamento de fadiga |
| Missões | Primárias (campanha), Secundárias (opcionais), Diárias (resetam a cada dia de jogo) |
| Sistema de tempo | Avanço manual: +1h ou +8h por pressão; 1 dia de jogo = 1440 minutos de jogo |
| Economia | Economia baseada em moedas escalada ÷100 dos preços reais; saldo inicial 5M (Realista) / 12M (Ficcional) |
| Mapa mundial | Canvas de projeção Mercator 2D; 250 aeroportos plotados; rotas ativas desenhadas por tipo de operação |
| Reputação | Pontuação 0–100; pousos no horário +1, atrasos −2, cancelamentos −5; afeta a taxa de ocupação |
| Manutenção | A condição da aeronave degrada por hora de voo; manutenção obrigatória a cada 100 horas de voo |
| Tema escuro | Paleta de cores personalizada — fundo SkyBlack, destaques SkyAccentBlue/Green/Purple/Orange |
| Bilíngue | Recursos de string em inglês e português brasileiro (pt-rBR) |

---

## Arquitetura

SkyTycoon segue **MVVM + Clean Architecture** com três camadas estritas:

```
┌─────────────────────────────────────────────────────────────┐
│  CAMADA DE UI  (telas Jetpack Compose + ViewModels)         │
│  • Conhece apenas modelos de domínio e casos de uso         │
│  • ViewModels mantêm StateFlow<UiState> observado pelas     │
│    telas                                                    │
└───────────────────┬─────────────────────────────────────────┘
                    │ chama casos de uso (suspend / Flow)
┌───────────────────▼─────────────────────────────────────────┐
│  CAMADA DE DOMÍNIO  (Kotlin puro — sem dependências Android) │
│  • Modelos de domínio (data classes)                        │
│  • Interfaces de repositório                                │
│  • Casos de uso (regras de negócio)                         │
└───────────────────┬─────────────────────────────────────────┘
                    │ implementa interfaces de repositório
┌───────────────────▼─────────────────────────────────────────┐
│  CAMADA DE DADOS  (banco Room + seed JSON)                  │
│  • Entidades Room, DAOs, AppDatabase                        │
│  • Implementações de repositório                            │
│  • Conversão JSON → Entity na primeira inicialização        │
└─────────────────────────────────────────────────────────────┘
```

### Padrões principais

- **StateFlow** — cada ViewModel expõe um único `StateFlow<XxxUiState>`; a tela o coleta com `collectAsStateWithLifecycle()`.
- **Casos de uso** — cada ação visível ao usuário é uma classe com `suspend operator fun invoke(…)`. O ViewModel as chama no `viewModelScope` via `Dispatchers.IO`.
- **Interfaces de repositório** — a camada de domínio depende apenas de interfaces (ex.: `FlightRepository`); a camada de dados fornece implementações injetadas pelo Hilt com `@Singleton`.
- **UseCaseResult** — sealed class (`Success<T>` / `Failure(message)`) retornada por casos de uso mutantes para que o ViewModel possa exibir erros no snackbar sem travar.

---

## Estrutura do Projeto

```
app/src/main/
├── assets/
│   ├── airports.json             250 aeroportos reais (IATA, ICAO, lat/lon, cidade, país, região, hasHelipad)
│   └── aircraft_models.json      25 modelos de aeronaves (especificações, preços, categoria)
│
└── java/com/skytycoon/app/
    ├── config/
    │   └── GameBalanceConfig.kt  Todos os números ajustáveis — custos, taxas, tempos
    │
    ├── data/
    │   ├── local/
    │   │   ├── AppDatabase.kt    Room @Database — lista todas as entidades e DAOs
    │   │   ├── Converters.kt     TypeConverter do Room — AircraftCategory ↔ String
    │   │   ├── dao/              Uma interface DAO por entidade (CRUD + consultas)
    │   │   └── entity/           Data classes @Entity do Room (uma por tabela do BD)
    │   └── repository/           Implementações de repositório (interface + classe @Singleton)
    │
    ├── di/
    │   ├── DatabaseModule.kt     Hilt @Module — fornece AppDatabase e todos os DAOs
    │   └── RepositoryModule.kt   Hilt @Module — vincula interfaces a implementações
    │
    ├── domain/
    │   ├── model/                Data classes Kotlin puras (sem imports Android/Room)
    │   └── usecase/              Uma classe por ação de negócio
    │
    └── ui/
        ├── components/           Composables reutilizáveis usados em múltiplas telas
        ├── navigation/
        │   ├── Screen.kt         Sealed class listando todas as strings de rota
        │   └── NavGraph.kt       NavHost com todos os destinos composable + SkyBottomNavBar
        ├── screens/              Um sub-pacote por tela (Screen.kt + ViewModel.kt)
        ├── theme/                Tokens de cor, tipografia, tema Material3
        └── utils/
            └── GameTimeUtils.kt  Converte minutos de jogo para strings como "Dia 3 — 14:30"
```

---

## Referência de Arquivos — Camada de Dados

### `config/GameBalanceConfig.kt`

Objeto central com todas as constantes numéricas que controlam a sensação do jogo:

| Constante | Valor | Propósito |
|---|---|---|
| `ADVANCE_STEP_MINUTES` | 60 | Minutos de jogo adicionados por pressão normal |
| `FAST_ADVANCE_STEP_MINUTES` | 480 | Minutos de jogo adicionados por pressão rápida (8h) |
| `STARTING_BALANCE_REALISTIC` | 5.000.000 | Moedas no início (Realista) |
| `STARTING_BALANCE_FICTIONAL` | 12.000.000 | Moedas no início (Ficcional) |
| `FATIGUE_PER_FLIGHT_HOUR` | 8 | Pontos de fadiga adicionados por hora de voo |
| `FATIGUE_RECOVERY_PER_GAME_HOUR` | 20 | Pontos de fadiga recuperados por hora de descanso |
| `MAINTENANCE_INTERVAL_HOURS` | 100.0 | Horas de voo entre manutenções obrigatórias |
| `CONDITION_LOSS_PER_FLIGHT_HOUR` | 0.8 | % de condição da aeronave perdida por hora de voo |
| `MAX_AVAILABLE_CONTRACTS` | 12 | Máximo de contratos visíveis ao mesmo tempo |
| `CONTRACT_EXPIRY_GAME_HOURS` | 48 | Horas antes de um contrato não aceito desaparecer |

Para alterar a curva de dificuldade do jogo, apenas este arquivo precisa ser editado.

---

### `data/local/AppDatabase.kt`

A classe do banco de dados Room. Anotada com `@Database` listando todas as oito classes de entidade. Usa `Converters` para o enum `AircraftCategory`. A versão é rastreada aqui — incremente-a sempre que uma entidade mudar e adicione uma migração ou use `fallbackToDestructiveMigration`.

### `data/local/Converters.kt`

TypeConverter do Room para `AircraftCategory`. Converte o enum de/para uma coluna String para que o Room possa persistí-lo. Apenas `AircraftCategory` precisa desse tratamento; todos os outros enums são armazenados como String `.name` na entidade e decodificados com segurança em cada método `toDomain()` usando `Enum.entries.firstOrNull { it.name == value }`.

---

### Arquivos de Entidade (`data/local/entity/`)

Cada entidade é uma data class anotada com `@Entity`. Elas espelham o modelo de domínio mas usam apenas tipos compatíveis com Room (sem enums diretamente — armazenados como String, exceto `AircraftCategory`). Cada entidade fornece:

- **`toDomain()`** — converte a entidade para o modelo de domínio correspondente. Campos enum usam `entries.firstOrNull` com fallback seguro para evitar travamentos com valores inesperados no BD.
- **`companion object { fun fromDomain(model) }`** — converte um modelo de domínio para a entidade para insert/update.

| Arquivo de Entidade | Tabela | Campos principais |
|---|---|---|
| `AircraftModelEntity.kt` | `aircraft_models` | id (Int), manufacturer, model, category (AircraftCategory via TypeConverter), passengerCapacity, rangeKm, cruiseSpeedKmh, purchasePriceCoins, leasingCostPerHourCoins |
| `OwnedAircraftEntity.kt` | `owned_aircraft` | id (autoGen), modelId (FK→aircraft_models), registrationCode, acquisitionType (String), condition (0–100), totalFlightHours, nextMaintenanceHours |
| `FlightEntity.kt` | `flights` | id (autoGen), operationType (String), aircraftId (FK), originIata, destinationIata, departureGameMinutes, arrivalGameMinutes, passengerCount, revenueCoins, status (String), assignedPilotId, assignedCopilotId, contractId |
| `ContractEntity.kt` | `contracts` | id (autoGen), operationType (String), originIata, destinationIata, passengerCount, totalValueCoins, bonusOnTimeCoins, deadlineGameMinutes, status (String), assignedFlightId |
| `EmployeeEntity.kt` | `employees` | id (autoGen), name, type (String), level (1–5), dailySalaryCoins, fatigue (0–100), currentFlightId |
| `MissionEntity.kt` | `missions` | id (autoGen), type (String), title, description, targetValue, currentValue, status (String), operationType (String?), expiresAtGameDay |
| `AirportEntity.kt` | `airports` | id (Int), iata, icao, name, city, country, region, latitude, longitude, hasHelipad |
| `GameStateEntity.kt` | `game_state` | id=1 (linha única), companyName, gameMode (String), balanceCoins, reputation, researchPoints, currentGameMinutes, dayNumber |

---

### Arquivos DAO (`data/local/dao/`)

Uma interface Kotlin por entidade, anotada com `@Dao`. Cada uma expõe:

- `getAll(): Flow<List<XxxEntity>>` — consulta reativa ao vivo; Room re-emite sempre que a tabela muda
- `getById(id): XxxEntity?` — busca de linha única
- `insert(entity): Long` — retorna o novo ID da linha
- `update(entity)` — substitui linha existente pela chave primária
- `delete(entity)` — remove uma linha
- Consultas especializadas conforme necessário, ex.: `getByStatus(status)`, `getAvailable()`, `expireOld(nowMinutes)`

Todos os DAOs são injetados em seus repositórios correspondentes via Hilt.

---

### Arquivos de Repositório (`data/repository/`)

Cada arquivo de repositório contém:
1. Uma **interface** (estilo camada de domínio) — define quais operações são possíveis.
2. Uma **classe de implementação** `@Singleton` com `@Inject constructor(private val dao: XxxDao)` que delega ao DAO e mapeia entidades ↔ modelos de domínio.

| Repositório | Responsabilidade |
|---|---|
| `AircraftModelRepository` | Lê o catálogo de modelos de aeronaves; seed de `aircraft_models.json` na primeira inicialização |
| `OwnedAircraftRepository` | CRUD para a frota de aeronaves compradas/alugadas pelo jogador |
| `FlightRepository` | CRUD para voos agendados/ativos/concluídos; consulta por status ou aeronave |
| `ContractRepository` | CRUD para contratos charter/helicóptero; marca expirados |
| `EmployeeRepository` | CRUD para funcionários; consulta por tipo, disponibilidade |
| `MissionRepository` | CRUD para missões; exclui missões diárias expiradas |
| `AirportRepository` | Somente leitura; seed de `airports.json` na primeira inicialização |
| `GameStateRepository` | Leitura/gravação de linha única para o estado global do jogo (saldo, dia, reputação) |

---

## Referência de Arquivos — Camada de Domínio

### `domain/model/`

Data classes Kotlin puras — sem imports Android ou Room. São a moeda de troca entre as camadas.

| Arquivo | O que modela |
|---|---|
| `GameState.kt` | Estado global do jogador: nome da empresa, modo, saldo, reputação, pontos de pesquisa, tempo atual do jogo |
| `AircraftModel.kt` | Uma entrada do catálogo (especificações, preços). Tem propriedade computada `displayName` e helper `isCapableOfRoute(km)` |
| `OwnedAircraft.kt` | Uma aeronave específica do jogador. Tem helpers `isOperational` (condição > 20) e `isMaintenanceDue` |
| `Flight.kt` | Um registro de viagem agendada ou ativa. Tem helpers `isActive` (SCHEDULED/BOARDING/IN_FLIGHT/DELAYED) e `isFinished` |
| `Contract.kt` | Uma oferta de trabalho charter ou helicóptero com valor, prazo e status |
| `Employee.kt` | Um membro da equipe com tipo, nível, fadiga e voo atribuído opcionalmente. Tem helpers `isAvailable` e `canFlyAircraftCategory` |
| `Airport.kt` | Uma entrada de aeroporto do JSON seed. Tem cálculo Haversine `distanceKmTo(other)` |
| `Mission.kt` | Um objetivo com rastreamento de progresso, recompensas e expiração |
| `UseCaseResult.kt` | `sealed class UseCaseResult<T>` com `Success(data: T)` e `Failure(message: String)` — usado por todos os casos de uso mutantes |
| `Enums.kt` | Todos os enums do jogo: `GameMode`, `OperationType`, `AircraftCategory`, `FlightStatus`, `EmployeeType`, `MissionType`, `MissionStatus`, `AcquisitionType`, `ContractStatus` |

---

### `domain/usecase/`

Cada caso de uso é uma classe com `@Inject constructor` e `suspend operator fun invoke(…): Result`. Executam em `Dispatchers.IO`. A lógica é completamente isolada da UI.

| Caso de uso | O que faz |
|---|---|
| `StartNewGameUseCase` | Cria o `GameState` inicial, insere no BD, seed de missões |
| `SeedDataUseCase` | Chama `AircraftModelRepository.seedIfEmpty` e `AirportRepository.seedIfEmpty` na inicialização |
| `ScheduleFlightUseCase` | Valida disponibilidade da aeronave, alcance da rota, conflitos de horário, qualificação do piloto (Realista), calcula tempo de chegada e receita, insere o voo e marca o contrato como ACCEPTED |
| `PurchaseAircraftUseCase` | Verifica saldo, deduz preço (compra) ou aceita aluguel, insere o registro da aeronave |
| `HireEmployeeUseCase` | Valida se o jogador pode pagar a taxa de contratação, deduz e insere o funcionário |
| `AdvanceTimeUseCase` | Loop principal do jogo: avança minutos, transiciona SCHEDULED→IN_FLIGHT→COMPLETED, paga salários e custos de aluguel, atualiza condição da aeronave, ajusta fadiga da tripulação, gera missões diárias, expira contratos antigos |
| `GenerateContractsUseCase` | Preenche slots de contratos disponíveis até `MAX_AVAILABLE_CONTRACTS` com contratos charter/helicóptero gerados aleatoriamente entre aeroportos válidos |

---

## Referência de Arquivos — Camada de UI

### `ui/navigation/Screen.kt`

Sealed class listando todas as strings de rota de cada tela:

```
Splash → NewGame → Dashboard ↔ Fleet ↔ Schedule ↔ Employees ↔ Missions ↔ Map
```

### `ui/navigation/NavGraph.kt`

Configura o `NavHost` com todos os oito destinos `composable(…)`. Também define `SkyBottomNavBar` — a barra de navegação inferior exibida em todas as telas principais (Dashboard, Fleet, Schedule, Employees, Missions, Map). Destaca a rota ativa e navega com `popUpTo` + `launchSingleTop` para evitar duplicação no back stack.

---

### `ui/theme/`

| Arquivo | Propósito |
|---|---|
| `Color.kt` | Define todos os tokens de cor nomeados: `SkyBlack`, `SkyDarkBlue`, `SkyCardBg`, `SkyAccentBlue`, `SkyAccentGreen`, `SkyAccentPurple`, `SkyAccentOrange`, `SkyAccentRed`, `SkyGold`, `SkyTextPrimary`, `SkyTextSecondary`, `SkyDivider` |
| `Type.kt` | Sobrescritas de tipografia Material3 (usa fontes padrão com tamanhos personalizados) |
| `Theme.kt` | Composable `SkyTycoonTheme` — aplica esquema de cores escuro usando os tokens acima |

---

### `ui/components/`

Composables compartilhados usados em múltiplas telas:

| Componente | Propósito |
|---|---|
| `SkyCard` | Wrapper de card estilizado com fundo escuro (`SkyCardBg`), cantos arredondados, borda `highlighted` opcional. Envolve conteúdo em `Column` com 12 dp de padding. |
| `RouteMapCanvas` | Mapa mundial 2D baseado em `Canvas`. Desenha fundo de projeção Mercator, plota pontos de aeroportos e linhas de rota coloridas para voos ativos (azul = aéreo, verde = charter, roxo = helicóptero). Toque chama `onAirportTap`. |
| `TimeAdvanceBar` | Barra inferior no Dashboard mostrando tempo atual do jogo, número do dia e dois botões de avanço (1h / 8h). |
| `StatTile` | Um card pequeno mostrando ícone, rótulo, valor e texto de valor colorido opcional — usado no Dashboard para saldo, reputação, etc. |

---

### `ui/utils/GameTimeUtils.kt`

Função de extensão `Long.toGameTimeString()` que converte minutos brutos de jogo (ex.: `1530`) para uma string legível como `"Dia 2 — 02:30"`. Usado em cards de voo, prazos de contrato e no display de tempo do Dashboard.

---

### Pacotes de tela (`ui/screens/`)

Cada pacote de tela contém exatamente dois arquivos: `XxxScreen.kt` (a UI Compose) e `XxxViewModel.kt` (o detentor de estado).

---

#### `screens/splash/`

**`SplashViewModel.kt`**
Executa um delay de 1500 ms na inicialização, então verifica `GameStateRepository` por um save existente. Define `navDestination` para `Screen.Dashboard.route` (jogador retornando) ou `Screen.NewGame.route` (primeira inicialização). Expõe `clearNavDestination()` para que a tela redefina o valor após navegar (evita re-navegação se o composable recompor).

**`SplashScreen.kt`**
Exibe um logo animado (escala + fade in), o nome SkyTycoon com gradiente azul-para-roxo e o tagline. Coleta `navDestination` e navega ao ser definido, removendo todo o back stack.

---

#### `screens/newgame/`

**`NewGameViewModel.kt`**
Mantém o input do nome da empresa e o modo de jogo selecionado. Ao submeter, chama `StartNewGameUseCase` e `SeedDataUseCase`, então define `navDestination` para o Dashboard.

**`NewGameScreen.kt`**
Campo de texto para nome da empresa, dois botões de seleção de modo (Realista / Ficcional) e botão Iniciar. Desabilitado até que um nome não-vazio seja inserido.

---

#### `screens/dashboard/`

**`DashboardViewModel.kt`**
Coleta `GameState`, todos os voos e todas as aeronaves via seus repositórios. Expõe `onAdvanceTime(fast)` que chama `AdvanceTimeUseCase` e depois `GenerateContractsUseCase` para recarregar contratos após cada passo de tempo.

**`DashboardScreen.kt`**
Tela principal. Exibe:
- Nome da empresa e tempo/dia atual do jogo
- Tiles de stat: saldo, reputação, pontos de pesquisa, voos ativos
- Seção de alertas de manutenção (aeronaves com `isMaintenanceDue`)
- Um mini mapa de rotas (`RouteMapCanvas`) em um card
- `TimeAdvanceBar` na parte inferior

---

#### `screens/fleet/`

**`FleetViewModel.kt`**
Carrega aeronaves próprias e modelos de aeronaves. Gerencia chamadas `onPurchase` / `onLease` via `PurchaseAircraftUseCase`. Emite `successMsg` / `errorMsg` para feedback no snackbar. `clearSuccessMessage()` e `clearErrorMessage()` são chamados independentemente de cada `LaunchedEffect` para evitar condições de corrida.

**`FleetScreen.kt`**
Três abas: Aviões de Linha, Charter, Helicópteros. Cada aba lista as aeronaves próprias naquela categoria. Um FAB abre o diálogo de compra/aluguel. O diálogo tem seletor de modelo (dropdown populado do catálogo), toggle de tipo de aquisição e campo de código de registro.

---

#### `screens/schedule/`

**`ScheduleViewModel.kt`**
Carrega voos, contratos, aeroportos, aeronaves próprias e funcionários disponíveis. `onScheduleFlight(request)` chama `ScheduleFlightUseCase`. Emite mensagens de sucesso/erro para snackbar.

**`ScheduleScreen.kt`**
Duas abas: **Voos** (voos atuais e futuros) e **Contratos** (trabalhos charter/helicóptero disponíveis). Um FAB abre `ScheduleFlightSheet` — um `ModalBottomSheet` com:
- Dropdown de aeronave
- Campos de busca IATA com autocompletar (até 6 sugestões mostradas como Column normal — não LazyColumn, para evitar crash de scroll aninhado)
- Sliders de hora e minuto de partida (tempo de partida calculado como `dayStartMinutes + hora×60 + minutos`, onde `dayStartMinutes` é o limite de meia-noite do dia atual do jogo)
- Campo de preço do bilhete (mostrado apenas para voos AÉREOS sem contrato; botão Agendar permanece desabilitado se o input não for numérico)
- Dropdown de piloto

Quando uma linha de contrato é tocada na aba Contratos, o sheet abre pré-preenchido com aquele `contractId`.

---

#### `screens/employees/`

**`EmployeesViewModel.kt`**
Carrega todos os funcionários, filtra por aba de tipo. `onHire(name, type, level)` chama `HireEmployeeUseCase`. Emite mensagens de sucesso/erro.

**`EmployeesScreen.kt`**
Lista funcionários por tipo (Pilotos, Copilotos, Comissários, Mecânicos, Admin). Cada card mostra nome, estrelas de nível, barra de fadiga, salário diário e atribuição de voo atual. Um FAB abre o diálogo de contratação com campo de nome, dropdown de tipo e slider de nível.

---

#### `screens/missions/`

**`MissionsViewModel.kt`**
Carrega todas as missões, filtradas em três listas: primárias, secundárias, diárias.

**`MissionsScreen.kt`**
Três abas: Primárias, Secundárias, Diárias. Cada card de missão mostra título, descrição, barra de progresso animada, badge de status e chips de recompensa (moedas, reputação, pesquisa). A aba diária mostra um aviso "missões resetam a cada dia de jogo". A tela preenche o espaço máximo disponível para que a mensagem de estado vazio fique centralizada verticalmente.

---

#### `screens/map/`

**`MapViewModel.kt`**
Carrega todos os aeroportos e todos os voos ativos (status `isActive`). Expõe `onAirportSelected(airport?)` para mostrar/esconder o sheet de detalhes do aeroporto.

**`MapScreen.kt`**
`RouteMapCanvas` em tela cheia com overlay de legenda (Aéreo = azul, Charter = verde, Helicóptero = roxo). Tocar em um aeroporto mostra um `ModalBottomSheet` com código IATA, nome completo, cidade/país, ICAO, região, badge de helipad e contagem de voos ativos. `buildRoutes(airports, flights)` é um helper privado que converte registros de voo em pares `(Aeroporto, Aeroporto)` para o canvas.

---

## Modelos de Domínio

### GameState

```
id: Long                    Sempre 1 (linha única)
companyName: String         Definido pelo jogador no novo jogo
gameMode: GameMode          REALISTIC ou FICTIONAL
balanceCoins: Long          Fundos atuais
reputation: Int             0–100
researchPoints: Int         Pontos de P&D acumulados
currentGameMinutes: Long    Total de minutos de jogo decorridos (0 = início do jogo)
dayNumber: Int              Dia atual do jogo (base 1)
```

### AircraftModel

Entrada estática do catálogo. Propriedades computadas principais:
- `displayName` — `"$manufacturer $model"` (ex.: "Boeing 737-800")
- `isCapableOfRoute(distanceKm)` — retorna `distanceKm <= rangeKm`

### OwnedAircraft

Aeronave do jogador. Propriedades computadas principais:
- `isOperational` — `condition > 20`
- `isMaintenanceDue` — `totalFlightHours >= nextMaintenanceHours`

### Flight

Um registro de viagem. Propriedades computadas principais:
- `durationMinutes` / `durationHours`
- `isActive` — status é SCHEDULED, BOARDING, IN_FLIGHT ou DELAYED
- `isFinished` — status é COMPLETED ou CANCELLED

### Contract

Um trabalho charter/helicóptero. Contém `totalValueCoins` (pago na conclusão), `bonusOnTimeCoins` (pago se o voo pousar antes de `deadlineGameMinutes`), e `status` (AVAILABLE → ACCEPTED → COMPLETED / FAILED / EXPIRED).

### Employee

Membro da equipe. Propriedades computadas principais:
- `isAvailable` — `fatigue < 80 && currentFlightId == null`
- `canFlyAircraftCategory(category)` — verifica tipo (PILOT pode voar AIRLINER, HELICOPTER_PILOT pode voar HELICOPTER)
- `dailySalaryCoins` — armazenado diretamente; definido na contratação como `level × 50`

### Airport

Dados de seed estáticos. `distanceKmTo(other: Airport)` usa a fórmula de Haversine para distância de grande círculo.

### Mission

Rastreia um objetivo. `progressPercent: Float` é `(currentValue / targetValue).coerceIn(0f, 1f)`.

---

## Casos de Uso (Regras de Negócio)

### AdvanceTimeUseCase — O Loop do Jogo

Chamado toda vez que o jogador pressiona um botão de avanço. Passos:

1. Lê o `GameState` atual.
2. Calcula `step` (60 ou 480 minutos) e `newMinutes`.
3. Se o limite do dia for cruzado:
   - Adiciona salários dos funcionários (diário × dias passados)
   - Adiciona pontos de pesquisa
   - Deduz custos de aluguel para aeronaves alugadas
   - Exclui missões diárias expiradas; gera novas apenas para o **dia atual**
4. Transiciona voos SCHEDULED cujos `departureGameMinutes ≤ newMinutes` para IN_FLIGHT; atribui tripulação.
5. Conclui voos IN_FLIGHT cujos `arrivalGameMinutes ≤ newMinutes`:
   - Credita `revenueCoins` ao saldo
   - Incrementa reputação
   - Degrada condição da aeronave; avança `totalFlightHours`; agenda próxima manutenção
   - Atualiza fadiga da tripulação: adiciona fadiga das horas de voo, subtrai crédito de descanso baseado no tempo decorrido **após o pouso** (não o passo completo)
6. Expira contratos após seu prazo.
7. Salva o `GameState` atualizado.

### ScheduleFlightUseCase — Agendando um Voo

Valida em sequência:
1. Aeronave existe e `isOperational`
2. Aeroportos de origem e destino existem e são diferentes
3. Distância da rota ≤ alcance da aeronave
4. Sem voos sobrepostos para esta aeronave
5. No modo Realista: piloto atribuído, sem fadiga, qualificado para categoria da aeronave
6. Receita calculada: `passengerCount × ticketPrice` (AÉREO) ou `contract.totalValueCoins` (CHARTER/HELICÓPTERO)
7. Voo inserido; contrato marcado como ACCEPTED

Retorna `UseCaseResult.Success(flight)` ou `UseCaseResult.Failure(message)`.

---

## Mecânicas do Jogo

### Tempo

- O tempo é armazenado como **minutos de jogo desde o início do dia 1** (um `Long` simples).
- `1440 minutos de jogo = 1 dia de jogo`.
- `currentGameMinutes / 1440 + 1 = dayNumber`.
- O jogador pressiona um botão para avançar. Não há relógio em tempo real rodando em segundo plano.

### Dinheiro

- Todos os preços são em **moedas** — valores do mundo real divididos por 100.
- Um Boeing 737 custa ~5.000.000 moedas (≈ $500M real ÷ 100).
- Saldo inicial: 5M (Realista) ou 12M (Ficcional).

### Reputação (0–100)

| Evento | Mudança |
|---|---|
| Pouso no horário | +1 |
| Atraso | −2 |
| Cancelamento | −5 |
| Contrato concluído | +2 |

A reputação afeta a **taxa de ocupação** — a porcentagem de assentos vendidos por voo:

| Reputação | % ocupação Realista | % ocupação Ficcional |
|---|---|---|
| 90–100 | 92% | ~129% (limitado) |
| 75–89 | 82% | ~115% |
| 60–74 | 70% | 98% |
| 40–59 | 55% | 77% |
| 20–39 | 38% | 53% |
| 0–19 | 20% | 28% |

### Fadiga

- Cada hora de voo adiciona **8 pontos de fadiga** à tripulação atribuída.
- Após o pouso, o descanso começa: **20 pontos de fadiga recuperados por hora de jogo** de descanso pós-pouso dentro do mesmo passo de avanço.
- Um tripulante com fadiga ≥ 80 é marcado como indisponível para novas atribuições.

### Condição da Aeronave

- Começa em 100%.
- Perde **0,8% por hora de voo**.
- Quando `totalFlightHours >= nextMaintenanceHours` (a cada 100 horas de voo), `nextMaintenanceHours` avança 100h.
- Aeronaves com condição ≤ 20 têm `isOperational = false` e não podem ser agendadas.

---

## Esquema do Banco de Dados

Banco de dados Room versão 1. Oito tabelas:

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

## Tipos Enum

Todos os enums são definidos em `domain/model/Enums.kt`:

| Enum | Valores |
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

Os enums são armazenados no Room como String `.name`. `toDomain()` em cada entidade usa `entries.firstOrNull { it.name == storedValue } ?: safeDefault` — se o BD contiver uma string não reconhecida (ex.: após uma migração), o app usa o fallback graciosamente em vez de lançar `IllegalArgumentException`.

---

## Assets JSON

### `airports.json`

Array de 250 objetos de aeroporto. Campos:

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

Inserido na tabela `airports` na primeira inicialização por `AirportRepository.seedIfEmpty`. Nunca modificado em tempo de execução.

### `aircraft_models.json`

Array de 25 objetos de aeronave. Campos:

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

`category` deve corresponder exatamente a um nome do enum `AircraftCategory`. Inserido em `aircraft_models` por `AircraftModelRepository.seedIfEmpty`.

---

## Navegação

Fluxo de telas:

```
Início do App
    │
    └─► Splash (delay 1500 ms + verificação do BD)
            │
            ├─► NewGame (sem save) ──► Dashboard (após salvar)
            │
            └─► Dashboard (save existente)
                    │
                    └─► Nav inferior: Dashboard ↔ Fleet ↔ Schedule ↔ Employees ↔ Missions ↔ Map
```

A barra de navegação inferior está presente em todas as seis telas principais. Usa `navController.navigate(route) { launchSingleTop = true; restoreState = true; popUpTo(graph) { saveState = true } }` para evitar criar entradas duplicadas no back stack e restaurar a posição de scroll ao trocar de aba.

---

## Injeção de Dependências

Hilt é usado para DI (`@HiltAndroidApp` em `SkyTycoonApp`, `@AndroidEntryPoint` em `MainActivity`, `@HiltViewModel` em todos os ViewModels).

### `di/DatabaseModule.kt`

`@Module @InstallIn(SingletonComponent)` — fornece:
- `provideDatabase(context)` → `AppDatabase` (Room `@Singleton`)
- Uma função `provide[Xxx]Dao(db)` por DAO (ex.: `provideFlightDao(db) = db.flightDao()`)

### `di/RepositoryModule.kt`

`@Module @InstallIn(SingletonComponent)` com `@Binds` — mapeia cada interface de repositório para sua implementação:
- `bindFlightRepository(impl: FlightRepositoryImpl): FlightRepository`
- (mesmo padrão para todos os 8 repositórios)

Isso permite que casos de uso dependam de interfaces (testáveis com fakes) enquanto o Hilt injeta as implementações reais com backend Room em tempo de execução.

---

## Testes

Os testes estão em `app/src/test/` (testes unitários JVM, sem emulador Android necessário).

### `ScheduleFlightUseCaseTest`

Testa toda a lógica de agendamento contra repositórios falsos em memória. Cobre:

- Aeronave não encontrada → `Failure`
- Aeronave não operacional (baixa condição) → `Failure`
- Aeroporto de origem/destino desconhecido → `Failure`
- Mesma origem e destino → `Failure`
- Rota excede alcance da aeronave → `Failure`
- Conflito de horário com voo existente → `Failure`
- Piloto ausente no modo Realista → `Failure`
- Piloto com fadiga → `Failure`
- Piloto não qualificado para categoria da aeronave → `Failure`
- Contrato não encontrado / já aceito → `Failure`
- Caminho feliz (voo AÉREO) → `Success` com receita correta
- Caminho feliz (voo CHARTER) → `Success` com contrato marcado ACCEPTED

Nenhum framework de mock usado — os fakes implementam diretamente as interfaces de repositório.

---

## Stack Tecnológica

| Camada | Tecnologia | Versão |
|---|---|---|
| Linguagem | Kotlin | 1.9.24 |
| Toolkit de UI | Jetpack Compose + Material 3 | BOM 2024.06 |
| Arquitetura | MVVM + Clean Architecture | — |
| Injeção de Dependências | Hilt | 2.51.1 |
| Banco de Dados | Room | 2.6.1 |
| Assíncrono | Coroutines + Flow | 1.8.0 |
| Navegação | Navigation Compose | 2.7.7 |
| Parse JSON | Gson | 2.10.1 |
| Sistema de build | Gradle (Kotlin DSL) | 8.6 |
| Android Gradle Plugin | AGP | 8.3.2 |
| SDK mínimo | Android 8.0 (Oreo) | API 26 |
| SDK alvo / compilação | Android 14 | API 34 |
