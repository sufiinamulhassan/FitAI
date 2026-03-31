# FitAI (Fitnest) - Complete Developer & Architecture Guide

This guide outlines the architecture, data structures, and feature implementation for the FitAI application. It acts as a comprehensive roadmap of the application's transition into a professional-grade "Home Workout" platform featuring a full Admin CMS, dynamic workout flows, and cloud-synchronized progress tracking.

---

## 1. Project Architecture

The app follows a robust **Firestore-First** architecture designed for scalability, allowing an Admin to create complex workout routines that sync down to all users in real-time.

*   **Cloud Backend:** Google Firebase (Authentication + Cloud Firestore).
*   **Local Caching:** Native Android SQLite (`LocalDatabaseHelper`) and Firebase's built-in offline persistence.
*   **Design Language:** Strictly tied to the modern Figma "Fitnest" kit (Poppins font, vibrant purple/blue branded gradients, extensive use of Material CardViews, and pill-shaped interactive elements).

---

## 2. Firebase Structure & Data Models (Crucial)

To make the app fully functional, your Firebase Firestore must handle the following core collections:

### A. `users`
Stores user profile information, metrics, and role-based access.
*   **Document ID:** Auth UID
*   **Fields:** `uid`, `name`, `email`, `role`, `goal`, `height`, `weight`, `age`, `profilePicUrl`, `bmi`
*   **Note:** The `role` field determines access to the Admin Panel. Set a user's role to `"admin"` to unlock the dashboard.

### B. `workout_plans` (Admin Managed)
The primary collection holding all workout routines created by the Admin.
*   **Document ID:** Auto-generated plan ID
*   **Fields:** `title`, `level` (beginner, intermediate, advanced), `description`, `totalDays`, `imgRes`
*   **Sub-collection:** `days` (e.g., `workout_plans/{planId}/days/1`)
    *   **Fields:** `dayNumber`, `dayTitle`, `isRestDay`
    *   **Nested Array:** `exercises` (contains Maps of: `name`, `reps`, `sets`, `duration`, `restTime`, `imageRes`, `instructions`)

### C. `user_progress` (User Specific)
Tracks an individual user's real-time journey through any specific workout plan.
*   **Path:** `users/{uid}/user_progress/{planId}`
*   **Fields:** 
    *   `planId`, `planTitle`, `level`, `startDate`, `lastWorkoutDate`
    *   `totalCaloriesBurned`, `totalTimeSpent`
    *   `completedDays` (An Array of Integers representing which days the user finished: e.g., `[1, 2, 4]`)

---

## 3. Core Features & Flows

### A. Professional Admin Panel (`AdminDashboardActivity`)
The central hub for app managers, accessible securely from the Profile screen if `role == "admin"`.
*   **Real-time Stats:** Live counting of Total Users and simulated Revenue streams.
*   **User CRUD System:** 
    *   **Read:** Displays a dynamic `RecyclerView` of all app users using `AdminUserAdapter`.
    *   **Update/Edit:** Long-press/Tap a user to open an Edit Dialog. Modifies `name`, demographics, goals, or elevates their privileges (`role`) directly in Firestore.
    *   **Delete:** Safely removes a user's node from the database.
*   **Workout Builder System (`AdminAddWorkoutActivity`):** 
    *   A massive, structured builder. Admins can name a plan, select a difficulty (Chips), specify total days, and utilize a bottom sheet dialog (`dialog_add_day.xml`) to attach individual exercises.
    *   Admins set precise attributes: Sets, Reps, Countdown Duration (seconds), and Rest Times (seconds) for every exercise.
    *   *Saves recursively to Firestore* bridging the document limits.

### B. User Workout Experience Flow (The "Home Workout" approach)

1.  **Workout Tracker (`WorkoutTrackerActivity`):**
    *   Reads all active Admin-created plans from Firestore. Displays them dynamically in scrolling lists. 
2.  **Plan Detail (`WorkoutPlanDetailActivity`):**
    *   Shows a visual preview of the workout plan.
    *   Features a **7-column grid layout** tracking the user's progress. Completed days are highlighted gracefully, derived locally from the `UserProgress` Firestore documents.
3.  **Day Exercise List (`DayExerciseListActivity`):**
    *   Upon selecting a scheduled day from the plan, a list is queried bringing down that day's specific exercises. Calculates total estimated duration dynamically.
4.  **Exercise Execution Timer (`ExerciseExecutionActivity`):**
    *   The heart of the workout. 
    *   Visual countdown timer (`CountDownTimer`) mapped to a circular `ProgressBar`.
    *   Allows Pausing/Resuming, `+10s`/`-10s` timeline adjustments, and skipping.
    *   Features an automatic "Rest Overlay" popup executing between sets (using the Admin's configured `restTime`).
5.  **Completion (`WorkoutCompleteActivity`):**
    *   Triggered immediately when the final timer finishes.
    *   **Auto-Save:** Saves `totalCaloriesBurned`, `totalTimeSpent`, and appends the `dayNumber` to `completedDays` within Firestore.
    *   Celebration screen displaying metrics before cleanly navigating back to the Home Dashboard.

### C. Dynamic Home Dashboard (`MainActivity`)
*   **Live Metrics:** Pulls BMI metrics, sleep logs, and water intake stats.
*   **Active Plans Preview:** The "Latest Workouts" UI dynamically inflates cards referencing the user's active `user_progress` models. 
*   It automatically converts their `completedDays.size()` against standard 30-day expectations to generate an accurate progress ring directly on their home screen.

---

## 4. Setup & Integration Checklist

1. **Gradle Dependencies:** Ensure `com.google.firebase:firebase-firestore` and `com.google.firebase:firebase-auth` are included.
2. **Permissions:** Internet permission `<uses-permission android:name="android.permission.INTERNET" />` must be present in `AndroidManifest.xml`.
3. **Activities:** All new activities created (Workout Detail, Execution Timer, Admin Panels) must be registered in the Manifest under `<application>`.
4. **Google-Services.json:** Do not forget to link the package in your Firebase Console and replace the `google-services.json` file to enable auth and db read/writes.
5. **Admin Access:** To initially test the Admin Panel, create an account normally in the app. Then go to your Firebase Console -> `users` collection -> find your UID -> change the `role` string from `"user"` to `"admin"`. Restart the app.

---
**Built & Formatted by:** Antigravity (Advanced Agentic Developer Session)
**Last Updated:** March 2026
