/*
 * WorkoutSeeder handles populating Firestore with default workout plans and exercises during initial app setup.
 */
package com.fitai.gym;

import android.content.Context;
import android.util.Log;
import com.google.firebase.firestore.DocumentSnapshot;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class WorkoutSeeder {
    private static final String TAG = "WorkoutSeeder";

    public static void checkAndSeed(Context context, Runnable onComplete) {
        FirebaseHelper.getInstance().getWorkoutPlansCollection().get()
            .addOnSuccessListener(snapshot -> {
                boolean needsReseed = false;
                List<DocumentSnapshot> staleDocs = new ArrayList<>();

                if (snapshot == null || snapshot.isEmpty()) {
                    needsReseed = true;
                } else {
                    int defaultPlansFound = 0;
                    for (DocumentSnapshot doc : snapshot.getDocuments()) {
                        String title = doc.getString("title");
                        if ("Fullbody Workout".equals(title) || "Lowerbody Workout".equals(title) || "Abs Workout".equals(title)) {
                            defaultPlansFound++;
                            Long ver = doc.getLong("version");
                            if (ver == null || ver < 2) {
                                needsReseed = true;
                                staleDocs.add(doc);
                            }
                        }
                    }
                    if (defaultPlansFound < 3) {
                        needsReseed = true;
                    }
                }

                if (needsReseed) {
                    Log.d(TAG, "Workout plans need seeding. Cleaning stale docs and seeding defaults...");
                    cleanAndSeed(context, staleDocs, onComplete);
                } else {
                    Log.d(TAG, "Workout plans are up to date.");
                    if (onComplete != null) {
                        onComplete.run();
                    }
                }
            })
            .addOnFailureListener(e -> {
                Log.e(TAG, "Failed to check workout_plans collection: " + e.getMessage());
                if (onComplete != null) {
                    onComplete.run();
                }
            });
    }

    private static void cleanAndSeed(Context context, List<DocumentSnapshot> staleDocs, Runnable onComplete) {
        if (staleDocs == null || staleDocs.isEmpty()) {
            seedDefaults(context, onComplete);
            return;
        }

        final int totalStale = staleDocs.size();
        final int[] cleanedCount = {0};

        for (DocumentSnapshot doc : staleDocs) {
            String planId = doc.getId();
            
            deleteDaysAndPlan(planId, 1, 10, () -> {
                synchronized (cleanedCount) {
                    cleanedCount[0]++;
                    if (cleanedCount[0] == totalStale) {
                        Log.d(TAG, "All stale plans deleted. Seeding defaults...");
                        seedDefaults(context, onComplete);
                    }
                }
            });
        }
    }

    private static void deleteDaysAndPlan(String planId, int dayNum, int maxDays, Runnable onComplete) {
        if (dayNum > maxDays) {
            
            FirebaseHelper.getInstance().getWorkoutPlansCollection().document(planId).delete()
                .addOnCompleteListener(task -> {
                    if (onComplete != null) {
                        onComplete.run();
                    }
                });
            return;
        }

        FirebaseHelper.getInstance().getDayDocument(planId, dayNum).delete()
            .addOnCompleteListener(task -> {
                deleteDaysAndPlan(planId, dayNum + 1, maxDays, onComplete);
            });
    }

    private static void seedDefaults(Context context, Runnable onComplete) {
        List<WorkoutPlanData> defaults = getDefaults();
        final int totalPlans = defaults.size();
        final int[] successCount = {0};

        for (WorkoutPlanData data : defaults) {
            
            FirebaseHelper.getInstance().getWorkoutPlansCollection().add(data.plan)
                .addOnSuccessListener(docRef -> {
                    String planId = docRef.getId();
                    
                    saveDaysForPlan(planId, data.days, 0, () -> {
                        synchronized (successCount) {
                            successCount[0]++;
                            if (successCount[0] == totalPlans) {
                                Log.d(TAG, "All default workout plans successfully seeded!");
                                if (onComplete != null) {
                                    onComplete.run();
                                }
                            }
                        }
                    });
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Failed to seed workout plan: " + e.getMessage());
                    synchronized (successCount) {
                        successCount[0]++;
                        if (successCount[0] == totalPlans) {
                            if (onComplete != null) {
                                onComplete.run();
                            }
                        }
                    }
                });
        }
    }

    private static void saveDaysForPlan(String planId, List<DayPlan> days, int index, Runnable onComplete) {
        if (index >= days.size()) {
            if (onComplete != null) {
                onComplete.run();
            }
            return;
        }

        DayPlan day = days.get(index);
        Map<String, Object> dayData = new HashMap<>();
        dayData.put("dayNumber", day.getDayNumber());
        dayData.put("dayTitle", day.getDayTitle());
        dayData.put("restDay", day.isRestDay());

        List<Map<String, Object>> exerciseList = new ArrayList<>();
        for (Exercise ex : day.getExercises()) {
            Map<String, Object> exMap = new HashMap<>();
            exMap.put("name", ex.getName());
            exMap.put("reps", ex.getReps());
            exMap.put("sets", ex.getSets());
            exMap.put("duration", ex.getDuration());
            exMap.put("restTime", ex.getRestTime());
            exMap.put("imageRes", ex.getImageRes());
            exMap.put("instructions", ex.getInstructions());
            exerciseList.add(exMap);
        }
        dayData.put("exercises", exerciseList);

        FirebaseHelper.getInstance().getDayDocument(planId, day.getDayNumber()).set(dayData)
            .addOnSuccessListener(v -> saveDaysForPlan(planId, days, index + 1, onComplete))
            .addOnFailureListener(e -> {
                Log.e(TAG, "Failed to save day " + day.getDayNumber() + ": " + e.getMessage());
                saveDaysForPlan(planId, days, index + 1, onComplete);
            });
    }

    private static class WorkoutPlanData {
        WorkoutPlan plan;
        List<DayPlan> days;

        WorkoutPlanData(WorkoutPlan plan, List<DayPlan> days) {
            this.plan = plan;
            this.days = days;
        }
    }

    private static List<WorkoutPlanData> getDefaults() {
        List<WorkoutPlanData> list = new ArrayList<>();

        
        WorkoutPlan plan1 = new WorkoutPlan(
            "Fullbody Workout",
            "A comprehensive full body workout to build strength, boost endurance, and burn calories.",
            "workout_1",
            Arrays.asList("beginner"),
            3,
            320,
            2
        );
        List<DayPlan> days1 = new ArrayList<>();
        days1.add(new DayPlan(1, "Full Body Strength", false, Arrays.asList(
            new Exercise("Jumping Jacks", "x12", 3, 30, 15, "workout_1", "Jump with legs spread and hands touching overhead. Stand upright with feet together and arms at sides. Simultaneously jump to spread feet wide and raise arms above head, then return to start."),
            new Exercise("Squats", "x15", 3, 45, 15, "workout_1", "Lower your hips from a standing position and stand back up. Keep chest upright and knees behind toes."),
            new Exercise("Push-Ups", "x12", 3, 30, 15, "workout_1", "Start in a plank position. Lower your body until your chest almost touches the floor, keeping your elbows close to your torso. Push back up."),
            new Exercise("Plank Jacks", "x15", 3, 30, 15, "workout_1", "Start in a push-up position. Jump feet out and in, keeping core tight and hips level.")
        )));
        days1.add(new DayPlan(2, "Full Body Endurance", false, Arrays.asList(
            new Exercise("Mountain Climbers", "x20", 3, 30, 15, "workout_1", "Drive knees towards chest alternately from plank position, maintaining a flat back."),
            new Exercise("Incline Push-Ups", "x12", 3, 30, 15, "workout_1", "Place hands on an elevated surface like a bench or step. Lower chest towards the surface, then push back up."),
            new Exercise("Plank Hold", "30s", 3, 30, 15, "workout_1", "Hold a push-up position resting on your elbows. Keep your core tight and body straight from head to toe."),
            new Exercise("High Knees", "x30", 3, 30, 15, "workout_1", "Run in place, lifting knees high towards chest. Keep chest tall and pump arms.")
        )));
        days1.add(new DayPlan(3, "Active Recovery", false, Arrays.asList(
            new Exercise("Cobra Stretch", "20s", 3, 20, 15, "workout_1", "Lie face down, place hands under shoulders, and press your chest up off the ground. Relax your hips and shoulders."),
            new Exercise("Child's Pose", "30s", 3, 30, 15, "workout_1", "Kneel on the floor, sit back on your heels, and reach your arms forward on the floor. Lower your forehead to the ground."),
            new Exercise("Cat-Cow Stretch", "x10", 3, 30, 15, "workout_1", "Start on hands and knees. Inhale, arch back, look up (Cow). Exhale, round back, look down (Cat)."),
            new Exercise("Shoulder Stretch", "20s", 3, 20, 15, "workout_1", "Cross one arm over chest, pull close with other arm. Hold and repeat on other side.")
        )));
        list.add(new WorkoutPlanData(plan1, days1));

        
        WorkoutPlan plan2 = new WorkoutPlan(
            "Lowerbody Workout",
            "Target your glutes, hamstrings, and quads with this focused leg routine.",
            "workout_2",
            Arrays.asList("intermediate"),
            2,
            250,
            2
        );
        List<DayPlan> days2 = new ArrayList<>();
        days2.add(new DayPlan(1, "Glute & Leg Focus", false, Arrays.asList(
            new Exercise("Squats", "x15", 3, 45, 15, "workout_1", "Lower your hips from a standing position. Keep your weight in your heels and squeeze glutes at the top."),
            new Exercise("Lunges", "x12", 3, 40, 15, "workout_1", "Step forward with one leg, lowering hips until both knees are bent at a 90-degree angle. Push back to starting position."),
            new Exercise("Side Lunges", "x10", 3, 40, 15, "workout_1", "Step to side, bend knee, push back up. Keep other leg straight."),
            new Exercise("Glute Bridges", "x15", 3, 45, 15, "workout_1", "Lie on your back with knees bent and feet flat on the floor. Lift your hips towards the ceiling, squeezing your glutes.")
        )));
        days2.add(new DayPlan(2, "Calf & Hamstring Focus", false, Arrays.asList(
            new Exercise("Calf Raises", "x20", 3, 30, 15, "workout_1", "Stand with feet hip-width apart. Raise your heels off the floor, standing on your toes, then slowly lower down."),
            new Exercise("Donkey Kicks", "x15", 3, 30, 15, "workout_1", "On all fours, kick one heel up towards the ceiling, keeping knee bent at 90 degrees. Squeeze glute at top."),
            new Exercise("Fire Hydrants", "x15", 3, 30, 15, "workout_1", "On all fours, lift one knee out to the side, keeping hips level. Squeeze outer glute."),
            new Exercise("Wall Sit", "30s", 3, 30, 15, "workout_1", "Lean back against a wall, bend knees to 90 degrees, and hold the position. Keep back flat.")
        )));
        list.add(new WorkoutPlanData(plan2, days2));

        
        WorkoutPlan plan3 = new WorkoutPlan(
            "Abs Workout",
            "Core conditioning to strengthen your abdominal muscles and improve posture.",
            "workout_3",
            Arrays.asList("advanced"),
            2,
            180,
            2
        );
        List<DayPlan> days3 = new ArrayList<>();
        days3.add(new DayPlan(1, "Upper Abs Focus", false, Arrays.asList(
            new Exercise("Crunches", "x20", 3, 30, 15, "workout_1", "Lie on back, bend knees, place hands behind head. Lift upper body towards knees using abdominal muscles, then lower down."),
            new Exercise("Bicycle Crunches", "x20", 3, 30, 15, "workout_1", "Lie on back, twist elbow to opposite knee alternately, mimicking a pedaling motion."),
            new Exercise("Plank Hold", "30s", 3, 30, 15, "workout_1", "Hold a push-up position resting on your elbows. Keep body in a straight line."),
            new Exercise("V-Ups", "x10", 3, 40, 15, "workout_1", "Lie on back, lift torso and legs together to touch toes, forming a V shape.")
        )));
        days3.add(new DayPlan(2, "Lower Abs & Obliques", false, Arrays.asList(
            new Exercise("Leg Raises", "x12", 3, 40, 15, "workout_1", "Lie on back, keep hands under hips. Lift legs straight up to a 90-degree angle, then lower them slowly without touching the floor."),
            new Exercise("Russian Twists", "x20", 3, 30, 15, "workout_1", "Sit with knees bent, lean back slightly. Hold hands together and twist your torso side to side, touching the floor on each side."),
            new Exercise("Heel Touches", "x20", 3, 30, 15, "workout_1", "Lie on back, bend knees, lift shoulders slightly. Reach and touch left and right heels alternately."),
            new Exercise("Flutter Kicks", "30s", 3, 30, 15, "workout_1", "Lie on back, lift legs slightly off the floor, kick them up and down rapidly.")
        )));
        list.add(new WorkoutPlanData(plan3, days3));

        return list;
    }
}
