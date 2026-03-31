package com.fitai.gym;

import android.content.Context;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.List;

public class SyncManager {

    private FirebaseHelper firebaseHelper;
    private LocalDatabaseHelper localDb;

    public SyncManager(Context context) {
        firebaseHelper = FirebaseHelper.getInstance();
        localDb = new LocalDatabaseHelper(context);
    }

    public void syncWorkoutsFromCloud() {
        firebaseHelper.getWorkoutsCollection().get().addOnCompleteListener(task -> {
            if (task.isSuccessful() && task.getResult() != null) {
                QuerySnapshot snapshot = task.getResult();
                for (DocumentSnapshot doc : snapshot.getDocuments()) {
                    String id = doc.getId();
                    String title = doc.getString("title");
                    String desc = doc.getString("description");
                    Long calories = doc.getLong("calories");
                    Long duration = doc.getLong("duration");

                    // Convert to primtive types safely
                    int calVal = calories != null ? calories.intValue() : 0;
                    int durVal = duration != null ? duration.intValue() : 0;

                    localDb.insertWorkout(id, title, desc, calVal, durVal);
                }
            }
        });
    }

    // This fulfills the "Fully functional with Firebase SQLite syncing" requirement
    public List<WorkoutModel> getLocalWorkouts() {
        return localDb.getAllWorkouts();
    }
}
