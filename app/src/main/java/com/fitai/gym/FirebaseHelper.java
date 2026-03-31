package com.fitai.gym;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

public class FirebaseHelper {
    private static FirebaseHelper instance;
    private FirebaseFirestore db;
    private FirebaseAuth auth;
    private FirebaseStorage storage;

    private FirebaseHelper() {
        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();
        storage = FirebaseStorage.getInstance();
    }

    public static synchronized FirebaseHelper getInstance() {
        if (instance == null) {
            instance = new FirebaseHelper();
        }
        return instance;
    }

    public FirebaseAuth getAuth() { return auth; }
    public FirebaseFirestore getDb() { return db; }

    // ── Collections ──────────────────────────────────────────

    public CollectionReference getUsersCollection() {
        return db.collection("users");
    }

    public CollectionReference getWorkoutsCollection() {
        return db.collection("workouts");
    }

    public CollectionReference getWorkoutPlansCollection() {
        return db.collection("workout_plans");
    }

    public CollectionReference getMealsCollection() {
        return db.collection("meals");
    }

    public CollectionReference getPaymentsCollection() {
        return db.collection("payments");
    }

    public StorageReference getStorageReference() {
        return storage.getReference();
    }

    public String getCurrentUserUid() {
        return auth.getCurrentUser() != null ? auth.getCurrentUser().getUid() : null;
    }

    // ── User Document Helpers ────────────────────────────────

    public DocumentReference getCurrentUserDoc() {
        String uid = getCurrentUserUid();
        if (uid == null) return null;
        return getUsersCollection().document(uid);
    }

    public CollectionReference getUserProgressCollection(String uid) {
        return getUsersCollection().document(uid).collection("workout_progress");
    }

    public CollectionReference getUserProgressCollection() {
        String uid = getCurrentUserUid();
        if (uid == null) return null;
        return getUsersCollection().document(uid).collection("workout_progress");
    }

    public CollectionReference getUserScheduleCollection(String uid) {
        return getUsersCollection().document(uid).collection("workout_schedule");
    }

    // ── Workout Plan Helpers ─────────────────────────────────

    public CollectionReference getDaysCollection(String planId) {
        return getWorkoutPlansCollection().document(planId).collection("days");
    }

    public DocumentReference getDayDocument(String planId, int dayNumber) {
        return getDaysCollection(planId).document(String.valueOf(dayNumber));
    }

    // ── Query Helpers ────────────────────────────────────────

    public Query getWorkoutPlansByDate() {
        return getWorkoutPlansCollection().orderBy("createdAt", Query.Direction.DESCENDING);
    }

    public Query getUsersByRole(String role) {
        return getUsersCollection().whereEqualTo("role", role);
    }
}
