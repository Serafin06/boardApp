# BoardApp 🎲

**BoardApp** is an Android application for organizing board game meetups. It helps manage players, track attendance, and record game picks in a fair FIFO order.

---

## ✅ Current Features

### 👥 Player Management
- Add players with a name.
- Mark players as allowed to pick a game (`canChooseGame`).
- Assign a **queue position** when the player is allowed to pick games.

### 📅 Session Creation
- Create a board game session with a selected date.
- Select which players are attending.
- Store each session locally using Room Database.

### 📜 Game History
- Track who picked which game during previous sessions.
- View all game picks using a dedicated `GameHistoryActivity`.

---

## 🔄 Coming Soon
- **Game Picker Queue View**: A screen that shows the current game picking queue (FIFO).
- **Game Selection Screen**: Where the player at the front of the queue picks a game.
- **Automatic Queue Rotation** after each pick.
- **Firebase integration** for syncing across devices (future).
- **Improved UI/UX** and support for editing/deleting players and sessions.

---

## 🛠️ Tech Stack

- Kotlin + Android Jetpack
- Room (local database)
- LiveData + ViewModel
- RecyclerView for displaying lists
- MVVM architecture

---

## 🚀 Getting Started

1. Clone the repository:
   ```bash
   git clone https://github.com/Serafin06/boardApp.git
   cd boardApp
Open the project in Android Studio.

Build and run the app on an emulator or physical device.

📂 Project Structure (Key Files)
MainActivity – Entry point with navigation to features

Player.kt, Session.kt, GamePick.kt – Data classes for Room

PlayerListAdapter, GamePickListAdapter – Adapters for RecyclerView

GameHistoryActivity – Displays game history

GameQueueEntry.kt – Data class for queue system

(Planned) QueueActivity – Upcoming screen to show picking order

📸 Screenshots (Coming Soon)
👤 Author
Made with ❤️ by Serafin06

Feel free to contribute or open an issue if you have ideas or questions!
