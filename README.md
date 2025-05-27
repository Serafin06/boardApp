# 🎲 BoardApp – Board Game Group Manager (Android + Room)

[![Made with Kotlin](https://img.shields.io/badge/Kotlin-Android-blueviolet.svg?logo=kotlin&logoColor=white)](https://kotlinlang.org/)
[![Room Database](https://img.shields.io/badge/Database-Room-informational)](https://developer.android.com/jetpack/androidx/releases/room)
[![License: MIT](https://img.shields.io/badge/License-MIT-green.svg)](https://opensource.org/licenses/MIT)

**BoardApp** is an Android application for managing board game player groups.  
Each player can add themselves to the group and, later, add their own games.  
This is just the first step toward a fully featured board game session manager.

---

## ✨ Features (Current MVP)

- ✅ Add players to the local database
- ✅ Display the current player list
- ✅ MVVM architecture
- ✅ Room for local persistence
- 🔜 Add board games per player
- 🔜 Game night registration & rotation logic
- 🔜 Game history and chooser tracking

---

## 🧱 Tech Stack

- **Kotlin**
- **Android Jetpack**
  - [Room](https://developer.android.com/training/data-storage/room)
  - [LiveData & ViewModel](https://developer.android.com/topic/libraries/architecture/viewmodel)
  - [RecyclerView](https://developer.android.com/guide/topics/ui/layout/recyclerview)
- MVVM Architecture
- UUID-based primary keys (sync-ready)

---

## 🔮 Planned Features

- Add board games per player with:
  - Min/max player count
  - Estimated game duration
- Game nights (event-based play sessions)
- Player rotation for choosing games
- Skipping absent players without losing queue position
- Game history (who chose which game and when)

---

## 🧩 Architecture Overview

```text
AppDatabase
   └── PlayerDao
         └── PlayerRepository
               └── PlayerViewModel
                     └── MainActivity (UI)
The app is structured to allow a future switch from Room to Firebase or PostgreSQL backend.

📦 How to Run
Clone the repository:

bash
Kopiuj
Edytuj
git clone https://github.com/Serafin06/boardapp.git
Open in Android Studio.

Build & run on device or emulator.

📁 Project Structure
pgsql
Kopiuj
Edytuj
com.example.boardapp
│
├── data/
│   ├── AppDatabase.kt
│   ├── Player.kt
│   ├── PlayerDao.kt
│   └── PlayerRepository.kt
│
├── ui/
│   ├── PlayerViewModel.kt
│   ├── PlayerListAdapter.kt
│   └── MainActivity.kt
│
└── res/layout/
    ├── activity_main.xml
    └── player_item.xml
🧭 Migration Ready
Uses Repository pattern (Room → Firebase swap ready)

Uses UUIDs instead of local auto-increment

Clean separation of concerns

📃 License
This project is licensed under the MIT License.

👨‍💻 Author
Developed by Serafin06
Contributions and ideas welcome – feel free to fork and improve!
