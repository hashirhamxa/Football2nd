# Football App

A live football scores and results Android application, built with Java, Retrofit, Room, and Glide.

## Features

- 📺 **Live Matches** – Real-time scores for ongoing games
- 📅 **Past Results** – Recent finished match results, grouped by date
- 🗓️ **Upcoming Fixtures** – Matches in the next 7 days
- 🏆 **Tournaments** – Browse active leagues and competitions
- 📊 **League Standings** – Full table for any selected league
- 🔍 **Match Details** – Scorers, events, venue, and referee info

## Tech Stack

| Layer | Library |
|-------|---------|
| Network | Retrofit 2 + Gson Converter |
| HTTP Client | OkHttp 3 (with logging interceptor) |
| Local Cache | Room Persistence Library |
| Image Loading | Glide 4 |
| UI | AppCompat, ConstraintLayout, Material Components |
| Lifecycle | ViewModel + LiveData |

## Package Structure

```
bicodes.fifa.footballapp
├── api/                  # Retrofit service interfaces and client setup
├── data/                 # Room database, DAOs, and entity classes
├── model/                # Plain data model classes (JSON-mapped)
├── repository/           # Repositories mediating network and database
└── ui/
    ├── adapters/         # RecyclerView adapters
    ├── detail/           # Match detail screen
    ├── fragments/        # Live, Past, Upcoming, Tournaments fragments
    ├── main/             # MainActivity with bottom navigation
    └── standings/        # League standings screen
```

## Setup

1. Clone the repository.
2. Open in Android Studio (Giraffe or newer recommended).
3. Add your API base URL and key to `util/Config.java`.
4. Build and run on an emulator or physical device (API 23+).

## Build Requirements

- **Android SDK**: compileSdk 36, minSdk 23, targetSdk 36
- **Java**: 8 source/target (JDK 17+ recommended for the build toolchain)
- **Gradle**: 9.4.1 with Android Gradle Plugin 9.2.1

## License

This project is for demonstration purposes.
