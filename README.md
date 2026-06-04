# FitAI 💪

A comprehensive Android fitness application for gym and home workouts, built with **Java + XML** and powered by **Firebase**.

![Language](https://img.shields.io/badge/language-Java-orange.svg)
[![Platform](https://img.shields.io/badge/platform-Android-brightgreen.svg)](https://developer.android.com/)
[![License](https://img.shields.io/badge/license-MIT-blue.svg)](./LICENSE.md)
![Visitors](https://visitor-badge.laobi.icu/badge?page_id=sufiinamulhassan.FitAI)

## 📖 Overview

**FitAI** is a mobile app designed to help users manage workouts, nutrition habits, progress tracking, and gym engagement in one place.

It includes:
- **User workflows** for workout execution, scheduling, meal planning, hydration/sleep/activity tracking, and profile progress.
- **Admin workflows** for user management, workout and meal management, and subscription-related operations.

## ✨ Core Features

### User Features
- Authentication and onboarding flow
- Profile setup with personal fitness data
- Workout plans with day-wise structure
- Exercise execution with timers and rest flow
- Workout completion tracking and progress screens
- Activity, sleep, and water-intake logging
- Nutrition and meal planning modules
- Progress photo tracking
- Notifications and reminders

### Admin Features
- Admin dashboard with quick statistics
- User CRUD operations
- Workout plan creation and management
- Meal management
- Subscription/member management
- Ability to inspect user progress

## 🧰 Tech Stack

- **Language:** Java
- **UI:** XML layouts (Android Views)
- **Architecture Style:** Activity-based native Android app
- **Database/Backend:** Firebase Authentication, Firestore, Firebase Storage
- **Local Storage:** SQLite (`LocalDatabaseHelper`)
- **Image Handling:** Glide + uCrop
- **Build System:** Gradle (Android)

## 📁 Project Structure

```text
FitAI/
├── app/
│   ├── src/main/java/com/fitai/gym/   # Java source files
│   ├── src/main/res/                  # XML layouts, drawables, values
│   └── build.gradle                   # App module dependencies/config
├── build.gradle                       # Root Gradle configuration
├── settings.gradle                    # Module settings
└── README.md
```

## 🚀 Setup & Installation

### 1) Prerequisites
- Android Studio (latest stable recommended)
- JDK 17
- Firebase project

### 2) Clone the repository
```bash
git clone https://github.com/sufiinamulhassan/FitAI.git
cd FitAI
```

### 3) Configure Firebase
This repository excludes `google-services.json` for security reasons.

1. Create/select a Firebase project at [Firebase Console](https://console.firebase.google.com/)
2. Register Android app package: `com.fitai.gym`
3. Download `google-services.json`
4. Place it at:
   - `app/google-services.json`

### 4) Enable Firebase services
- **Authentication** → Email/Password
- **Cloud Firestore**
- **Storage**

### 5) Run the app
- Open the project in Android Studio
- Sync Gradle
- Run on emulator/device

## 👑 Admin Access (Role-Based)

Admin functionality is role-gated.

To enable admin for a user:
1. Register/login with that account.
2. Open Firestore `users` collection.
3. Find that user document by UID.
4. Set `role` field to `admin`.
5. Restart app and open profile/admin sections.

## �� Build/Test Notes

This repository currently does not include a Gradle wrapper script (`gradlew`).
Use Android Studio (or local Gradle setup compatible with Android Gradle Plugin 8.2.2) for syncing and building.

## 📌 Roadmap Ideas

- Add screenshots/GIFs for major flows
- Add unit/instrumentation tests
- Add CI workflow with Android build/test checks
- Improve role/security rules documentation for Firebase

## 🤝 Contributing

Contributions are welcome through issues and pull requests.

## 📄 License

This project is licensed under the MIT License. See [LICENSE.md](./LICENSE.md).

---

## 👤 Author

**Made by Sufi Inam Ul Hassan**
