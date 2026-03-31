<h1 align="center">FitAI 💪</h1>

<p align="center">
  <strong>A Fully Functional Android Gym & Home Workout App Built with Java & Firebase</strong>
</p>

<p align="center">
  ![Language](https://img.shields.io/badge/language-Java-orange.svg)&nbsp;
  [![License](https://img.shields.io/badge/license-MIT-blue.svg)](./LICENSE.md)&nbsp;
  ![Visitors](https://visitor-badge.laobi.icu/badge?page_id=sufiinamulhassan.FitAI)
</p>

---

## 📖 Overview

**FitAI** is a comprehensive fitness and health management application. Designed with modern aesthetics based on the popular "Fitnest" Figma UI kit, this app serves dual functions:
1. **For Users:** A personalized Home Workout tracker, complete with exercise execution timers, rest intervals, BMI calculations, and real-time cloud-synced progress.
2. **For Admins:** A robust CMS (Content Management System) directly baked into the mobile app to allow Gym Owners or Trainers to manage their users (CRUD), view live stats, and globally publish multi-day workout routines via a dynamic Workout Builder.

---

## ✨ Key Features

### 👤 User Experience
*   **Dynamic Home Dashboard:** Tracks real-time sleep logs, water intake, and updates your BMI automatically. Features an interactive progress ring estimating your completion of the latest workout.
*   **Structured Workout Plans:** Browse workout methodologies (e.g., Fullbody, Lowerbody) tailored by the Admin and track your schedule using an intuitive 7-day grid system.
*   **Exercise Execution Engine:** 
    *   Responsive circular countdown timers for timed exercises.
    *   Set/Rep tracking with automatic "Rest Interstitial" overlays mimicking premium fitness apps.
    *   Skip/Pause/Play and temporal adjustments (`+10s` / `-10s`).
*   **Cloud Progress Saving:** Your total calories burned, elapsed time, and completed days are automatically synced to Firebase Firestore on workout completion.

### 🛡️ Admin Dashboard (`Role: "admin"`)
*   **Live Metrics:** Monitor the total user base and estimated generated revenue instantly.
*   **User CRUD Management:** Long-press any registered user to edit their demographics, health metrics, update their goals, or directly delete their account entirely.
*   **Complex Workout Builder:** Create massive, multi-day workout plans with precise parameters (Rounds, Reps, Countdown seconds, and Rest Interval seconds) all from a clean multi-dialog UI. Workouts published here instantly sync to all client apps globally.

---

## 🛠 Tech Stack

*   **Language:** Java
*   **Architecture:** XML / Model-View (Native Android)
*   **Backend:** Google Firebase (Authentication & Cloud Firestore)
*   **Local Caching:** Native Android SQLite (`LocalDatabaseHelper`)
*   **Design & Styling:** Custom Gradients, Edge-to-Edge designs, Poppins Typography suite, Material `CardViews`, and dynamic programmatically colored components.

---

## 🚀 Getting Started & Installation

To run this app locally and connect it to your own backend database, follow these steps exactly:

### 1. Clone the Repository
```bash
git clone https://github.com/YourUsername/FitAI.git
cd FitAI
```

### 2. Connect Your Own Firebase (Required)
This project uses Firebase for Authentication and Firestore. To prevent security vulnerabilities, my `google-services.json` is excluded via `.gitignore`. You must supply your own:

1. Go to the [Firebase Console](https://console.firebase.google.com/) and create a new project.
2. Register an Android app using the package name: `com.fitai.gym`.
3. Download the generated **`google-services.json`** file.
4. Move this file directly into the `/app/` directory of your cloned project.

### 3. Configure Firebase Services
Inside your Firebase Console, ensure you enable the following:
*   **Authentication:** Enable the "Email/Password" sign-in provider.
*   **Firestore Database:** Create your database and start in **Test Mode** (or implement your own security rules).
*   **(Optional)** Create base collections if you prefer manual population: `users`, `workout_plans`.

---

## 👑 Accessing the Admin Panel

The Admin panel is hidden from regular users. To access it for the first time:
1. Run the app on your emulator/device and register a brand new account.
2. Open your Firebase Console and navigate to the **Firestore Database** -> **`users`** collection.
3. Find the Document ID matching your newly registered account (UID).
4. Edit the `role` field value from `"user"` to **`"admin"`**.
5. Restart your app and navigate to your **Profile** tab to access the new Admin Hub!

---

## 📸 Screenshots
*(Add your screenshots here!)*
*   `<img src="link-to-home-dashboard" width="200" />`
*   `<img src="link-to-admin-panel" width="200" />`
*   `<img src="link-to-exercise-timer" width="200" />`

---

## 💡 Motivation & Acknowledgements
This project combines complex Android layout design with scalable cloud infrastructure. It proves that fully enclosed CMS systems can comfortably live alongside front-end user interfaces natively in Java.

**UI Inspiration:** [Fitnest Figma Kit]

---
*Created by [Your Name here]*
