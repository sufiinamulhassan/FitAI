/*
 * AdminAddWorkoutActivity allows admins to create and upload new workout routines with exercise sequences.
 */
package com.fitai.gym;

import android.app.AlertDialog;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.content.Intent;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.chip.Chip;
import com.google.firebase.firestore.DocumentSnapshot;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AdminAddWorkoutActivity extends AppCompatActivity {

    private EditText etPlanTitle, etPlanDesc, etTotalDays, etCalories;
    private Chip chipBeginner, chipIntermediate, chipAdvanced;
    private RecyclerView rvDays;
    private String selectedBase64Image = null;
    private ImageView ivWorkoutCover;

    private final androidx.activity.result.ActivityResultLauncher<Intent> pickImageLauncher =
        registerForActivityResult(new androidx.activity.result.contract.ActivityResultContracts.StartActivityForResult(), result -> {
            if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                android.net.Uri imageUri = result.getData().getData();
                if (imageUri != null) {
                    processPickedImage(imageUri);
                }
            }
        });

    private void processPickedImage(android.net.Uri uri) {
        try {
            java.io.InputStream is = getContentResolver().openInputStream(uri);
            android.graphics.Bitmap bitmap = android.graphics.BitmapFactory.decodeStream(is);
            if (bitmap != null) {
                int maxDim = 350;
                int srcWidth = bitmap.getWidth();
                int srcHeight = bitmap.getHeight();
                int dstWidth = srcWidth;
                int dstHeight = srcHeight;
                if (srcWidth > maxDim || srcHeight > maxDim) {
                    if (srcWidth > srcHeight) {
                        dstWidth = maxDim;
                        dstHeight = (srcHeight * maxDim) / srcWidth;
                    } else {
                        dstHeight = maxDim;
                        dstWidth = (srcWidth * maxDim) / srcHeight;
                    }
                }
                android.graphics.Bitmap resized = android.graphics.Bitmap.createScaledBitmap(bitmap, dstWidth, dstHeight, true);
                java.io.ByteArrayOutputStream baos = new java.io.ByteArrayOutputStream();
                resized.compress(android.graphics.Bitmap.CompressFormat.JPEG, 75, baos);
                byte[] bytes = baos.toByteArray();
                selectedBase64Image = "base64:" + android.util.Base64.encodeToString(bytes, android.util.Base64.NO_WRAP);
                if (ivWorkoutCover != null) {
                    ivWorkoutCover.setImageBitmap(resized);
                }
            }
        } catch (Exception e) {
            Toast.makeText(this, "Failed to process image: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }
    private List<DayPlan> dayPlans = new ArrayList<>();
    private DayAdapter dayAdapter;
    private DialogExerciseAdapter dialogExerciseAdapter;
    private FirebaseHelper fbHelper;
    private String editingPlanId = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_add_workout);

        fbHelper = FirebaseHelper.getInstance();

        etPlanTitle = findViewById(R.id.etPlanTitle);
        etPlanDesc = findViewById(R.id.etPlanDesc);
        etTotalDays = findViewById(R.id.etTotalDays);
        etCalories = findViewById(R.id.etCalories);
        chipBeginner = findViewById(R.id.chipBeginner);
        chipIntermediate = findViewById(R.id.chipIntermediate);
        chipAdvanced = findViewById(R.id.chipAdvanced);
        rvDays = findViewById(R.id.rvDays);

        dayAdapter = new DayAdapter();
        rvDays.setLayoutManager(new LinearLayoutManager(this));
        rvDays.setNestedScrollingEnabled(false);
        rvDays.setAdapter(dayAdapter);

        ivWorkoutCover = findViewById(R.id.ivWorkoutCover);
        findViewById(R.id.btnUploadWorkoutCover).setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
            intent.setType("image/*");
            pickImageLauncher.launch(intent);
        });

        findViewById(R.id.btnBack).setOnClickListener(v -> finish());
        findViewById(R.id.btnAddDay).setOnClickListener(v -> showAddDayDialog());
        findViewById(R.id.btnPublish).setOnClickListener(v -> publishPlan());

        
        editingPlanId = getIntent().getStringExtra("plan_id");
        if (editingPlanId != null) {
            TextView btnPublish = findViewById(R.id.btnPublish);
            btnPublish.setText("Update Plan");
            loadPlanForEditing(editingPlanId);
        }
    }

    private void loadPlanForEditing(String planId) {
        fbHelper.getWorkoutPlansCollection().document(planId).get()
            .addOnSuccessListener(snapshot -> {
                if (snapshot != null && snapshot.exists()) {
                    etPlanTitle.setText(snapshot.getString("title"));
                    etPlanDesc.setText(snapshot.getString("description"));
                    Long days = snapshot.getLong("totalDays");
                    Long cals = snapshot.getLong("calories");
                    if (days != null) etTotalDays.setText(String.valueOf(days));
                    if (cals != null) etCalories.setText(String.valueOf(cals));

                    
                    String coverImg = snapshot.getString("imageRes");
                    if (coverImg != null) {
                        selectedBase64Image = coverImg;
                        ImageLoaderHelper.loadImage(this, ivWorkoutCover, selectedBase64Image, R.drawable.workout_1);
                    }

                    List<String> difficulty = (List<String>) snapshot.get("difficulty");
                    if (difficulty != null) {
                        chipBeginner.setChecked(difficulty.contains("beginner"));
                        chipIntermediate.setChecked(difficulty.contains("intermediate"));
                        chipAdvanced.setChecked(difficulty.contains("advanced"));
                    }

                    
                    fbHelper.getDaysCollection(planId).orderBy("dayNumber", com.google.firebase.firestore.Query.Direction.ASCENDING).get()
                        .addOnSuccessListener(daySnapshots -> {
                            dayPlans.clear();
                            for (DocumentSnapshot doc : daySnapshots.getDocuments()) {
                                DayPlan day = new DayPlan();
                                Long num = doc.getLong("dayNumber");
                                day.setDayNumber(num != null ? num.intValue() : 1);
                                day.setDayTitle(doc.getString("dayTitle"));
                                Boolean rest = doc.getBoolean("restDay");
                                day.setRestDay(rest != null ? rest : false);

                                List<Map<String, Object>> exList = (List<Map<String, Object>>) doc.get("exercises");
                                List<Exercise> exercises = new ArrayList<>();
                                if (exList != null) {
                                    for (Map<String, Object> exMap : exList) {
                                        String name = (String) exMap.get("name");
                                        String reps = (String) exMap.get("reps");
                                        int sets = ((Long) exMap.get("sets")).intValue();
                                        int duration = ((Long) exMap.get("duration")).intValue();
                                        int restTime = ((Long) exMap.get("restTime")).intValue();
                                        String imageRes = (String) exMap.get("imageRes");
                                        String instructions = (String) exMap.get("instructions");
                                        exercises.add(new Exercise(name, reps, sets, duration, restTime, imageRes, instructions));
                                    }
                                }
                                day.setExercises(exercises);
                                dayPlans.add(day);
                            }
                            dayAdapter.notifyDataSetChanged();
                        });
                }
            });
    }

    private void showAddDayDialog() {
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_add_day, null);
        EditText etDayTitle = dialogView.findViewById(R.id.etDayTitle);
        int nextDay = dayPlans.size() + 1;
        etDayTitle.setText("Day " + nextDay);

        new AlertDialog.Builder(this)
            .setTitle("Add Day " + nextDay)
            .setView(dialogView)
            .setPositiveButton("Add", (d, w) -> {
                String title = etDayTitle.getText().toString().trim();
                if (title.isEmpty()) title = "Day " + nextDay;
                DayPlan day = new DayPlan(nextDay, title, false, new ArrayList<>());
                dayPlans.add(day);
                dayAdapter.notifyItemInserted(dayPlans.size() - 1);
            })
            .setNegativeButton("Cancel", null)
            .show();
    }

    

    private void showManageDayDialog(final int dayIndex) {
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_manage_day, null);
        EditText etDayTitleField = dialogView.findViewById(R.id.etManageDayTitle);
        androidx.recyclerview.widget.RecyclerView rvEx = dialogView.findViewById(R.id.rvManageDayExercises);
        android.widget.Button btnAddEx = dialogView.findViewById(R.id.btnAddExInDialog);

        final DayPlan day = dayPlans.get(dayIndex);
        etDayTitleField.setText(day.getDayTitle());

        rvEx.setLayoutManager(new LinearLayoutManager(this));
        dialogExerciseAdapter = new DialogExerciseAdapter(dayIndex, day.getExercises());
        rvEx.setAdapter(dialogExerciseAdapter);

        btnAddEx.setOnClickListener(v -> showAddEditExerciseDialog(dayIndex, -1));

        new AlertDialog.Builder(this)
            .setTitle("Manage " + day.getDayTitle())
            .setView(dialogView)
            .setPositiveButton("Done", (d, w) -> {
                String newTitle = etDayTitleField.getText().toString().trim();
                if (!newTitle.isEmpty()) day.setDayTitle(newTitle);
                dayAdapter.notifyItemChanged(dayIndex);
                dialogExerciseAdapter = null;
            })
            .setNegativeButton("Cancel", (d, w) -> dialogExerciseAdapter = null)
            .setOnCancelListener(dialog -> dialogExerciseAdapter = null)
            .show();
    }

    private void showAddEditExerciseDialog(final int dayIndex, final int exerciseIndex) {
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_add_exercise, null);
        EditText etName = dialogView.findViewById(R.id.etExName);
        EditText etReps = dialogView.findViewById(R.id.etExReps);
        EditText etSets = dialogView.findViewById(R.id.etExSets);
        EditText etDuration = dialogView.findViewById(R.id.etExDuration);
        EditText etRest = dialogView.findViewById(R.id.etExRest);
        EditText etInstructions = dialogView.findViewById(R.id.etExInstructions);

        final boolean isEdit = exerciseIndex >= 0;
        final List<Exercise> exerciseList = dayPlans.get(dayIndex).getExercises();

        if (isEdit) {
            Exercise ex = exerciseList.get(exerciseIndex);
            etName.setText(ex.getName());
            etReps.setText(ex.getReps());
            etSets.setText(String.valueOf(ex.getSets()));
            etDuration.setText(String.valueOf(ex.getDuration()));
            etRest.setText(String.valueOf(ex.getRestTime()));
            etInstructions.setText(ex.getInstructions());
        }

        new AlertDialog.Builder(this)
            .setTitle(isEdit ? "Edit Exercise" : "Add Exercise")
            .setView(dialogView)
            .setPositiveButton(isEdit ? "Update" : "Add", (d, w) -> {
                String name = etName.getText().toString().trim();
                String reps = etReps.getText().toString().trim();
                int sets = parseIntSafe(etSets.getText().toString(), 3);
                int duration = parseIntSafe(etDuration.getText().toString(), 30);
                int rest = parseIntSafe(etRest.getText().toString(), 15);
                String instructions = etInstructions.getText().toString().trim();

                if (name.isEmpty()) {
                    Toast.makeText(this, "Exercise name is required", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (isEdit) {
                    Exercise ex = exerciseList.get(exerciseIndex);
                    ex.setName(name);
                    ex.setReps(reps.isEmpty() ? "x10" : reps);
                    ex.setSets(sets);
                    ex.setDuration(duration);
                    ex.setRestTime(rest);
                    ex.setInstructions(instructions);
                } else {
                    exerciseList.add(new Exercise(name, reps.isEmpty() ? "x10" : reps,
                            sets, duration, rest, "workout_1", instructions));
                }

                dayAdapter.notifyItemChanged(dayIndex);
                if (dialogExerciseAdapter != null) dialogExerciseAdapter.notifyDataSetChanged();
            })
            .setNegativeButton("Cancel", null)
            .show();
    }

    private void publishPlan() {
        String title = etPlanTitle.getText().toString().trim();
        String desc = etPlanDesc.getText().toString().trim();
        String daysStr = etTotalDays.getText().toString().trim();
        String calStr = etCalories.getText().toString().trim();

        if (title.isEmpty() || desc.isEmpty()) {
            Toast.makeText(this, "Title and description are required", Toast.LENGTH_SHORT).show();
            return;
        }

        if (dayPlans.isEmpty()) {
            Toast.makeText(this, "Add at least one day with exercises", Toast.LENGTH_SHORT).show();
            return;
        }

        
        List<String> difficulty = new ArrayList<>();
        if (chipBeginner.isChecked()) difficulty.add("beginner");
        if (chipIntermediate.isChecked()) difficulty.add("intermediate");
        if (chipAdvanced.isChecked()) difficulty.add("advanced");
        if (difficulty.isEmpty()) difficulty.add("beginner");

        int totalDays = parseIntSafe(daysStr, dayPlans.size());
        int calories = parseIntSafe(calStr, 200);

        
        Map<String, Object> planData = new HashMap<>();
        planData.put("title", title);
        planData.put("description", desc);
        planData.put("imageRes", selectedBase64Image != null ? selectedBase64Image : "workout_1");
        planData.put("difficulty", difficulty);
        planData.put("totalDays", totalDays);
        planData.put("calories", calories);

        if (editingPlanId == null) {
            planData.put("createdAt", System.currentTimeMillis());
            fbHelper.getWorkoutPlansCollection().add(planData)
                .addOnSuccessListener(docRef -> {
                    String planId = docRef.getId();
                    
                    saveDays(planId, 0);
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
        } else {
            fbHelper.getWorkoutPlansCollection().document(editingPlanId).update(planData)
                .addOnSuccessListener(v -> {
                    
                    deleteExistingDaysAndSave(editingPlanId);
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Error updating plan: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
        }
    }

    private void deleteExistingDaysAndSave(String planId) {
        fbHelper.getDaysCollection(planId).get()
            .addOnSuccessListener(snapshot -> {
                if (snapshot == null || snapshot.isEmpty()) {
                    saveDays(planId, 0);
                    return;
                }
                final int total = snapshot.size();
                final int[] count = {0};
                for (com.google.firebase.firestore.DocumentSnapshot doc : snapshot.getDocuments()) {
                    doc.getReference().delete().addOnCompleteListener(task -> {
                        synchronized (count) {
                            count[0]++;
                            if (count[0] == total) saveDays(planId, 0);
                        }
                    });
                }
            })
            .addOnFailureListener(e -> saveDays(planId, 0));
    }

    private void saveDays(String planId, int index) {
        if (index >= dayPlans.size()) {
            Toast.makeText(this, "✅ Workout Plan Published!", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        DayPlan day = dayPlans.get(index);
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

        fbHelper.getDayDocument(planId, day.getDayNumber()).set(dayData)
            .addOnSuccessListener(v -> saveDays(planId, index + 1))
            .addOnFailureListener(e -> {
                Toast.makeText(this, "Error saving day " + day.getDayNumber() + ": " + e.getMessage(),
                        Toast.LENGTH_SHORT).show();
            });
    }

    private int parseIntSafe(String s, int fallback) {
        try { return Integer.parseInt(s.trim()); }
        catch (Exception e) { return fallback; }
    }

    

    private class DayAdapter extends RecyclerView.Adapter<DayAdapter.VH> {

        @Override
        public VH onCreateViewHolder(ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_admin_day, parent, false);
            return new VH(v);
        }

        @Override
        public void onBindViewHolder(VH holder, int position) {
            DayPlan day = dayPlans.get(position);
            holder.tvDayNumber.setText(String.valueOf(day.getDayNumber()));
            holder.tvDayTitle.setText(day.getDayTitle());
            int exCount = day.getExercises() != null ? day.getExercises().size() : 0;
            holder.tvExerciseCount.setText(exCount + " exercise" + (exCount != 1 ? "s" : ""));

            holder.btnEditDay.setOnClickListener(v -> showManageDayDialog(position));
            holder.btnDeleteDay.setOnClickListener(v -> {
                dayPlans.remove(position);
                for (int i = 0; i < dayPlans.size(); i++) {
                    dayPlans.get(i).setDayNumber(i + 1);
                }
                notifyDataSetChanged();
            });

            holder.itemView.setOnClickListener(v -> showManageDayDialog(position));
        }

        @Override
        public int getItemCount() { return dayPlans.size(); }

        class VH extends RecyclerView.ViewHolder {
            TextView tvDayNumber, tvDayTitle, tvExerciseCount;
            View btnEditDay, btnDeleteDay;

            VH(View v) {
                super(v);
                tvDayNumber = v.findViewById(R.id.tvDayNumber);
                tvDayTitle = v.findViewById(R.id.tvDayTitle);
                tvExerciseCount = v.findViewById(R.id.tvExerciseCount);
                btnEditDay = v.findViewById(R.id.btnEditDay);
                btnDeleteDay = v.findViewById(R.id.btnDeleteDay);
            }
        }
    }

    

    private class DialogExerciseAdapter extends RecyclerView.Adapter<DialogExerciseAdapter.VH> {
        private final int dayIndex;
        private final List<Exercise> exercises;

        DialogExerciseAdapter(int dayIndex, List<Exercise> exercises) {
            this.dayIndex = dayIndex;
            this.exercises = exercises;
        }

        @Override
        public VH onCreateViewHolder(ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_admin_exercise, parent, false);
            return new VH(v);
        }

        @Override
        public void onBindViewHolder(@NonNull VH holder, int position) {
            Exercise ex = exercises.get(position);
            holder.tvExName.setText(ex.getName());
            holder.tvExDetail.setText(ex.getReps() + " · " + ex.getSets() + " sets · " + ex.getDuration() + "s");
            ImageLoaderHelper.loadImage(holder.itemView.getContext(), holder.ivExImg,
                    ex.getImageRes(), R.drawable.workout_1);

            holder.btnRemove.setOnClickListener(v -> {
                exercises.remove(holder.getAdapterPosition());
                notifyDataSetChanged();
                dayAdapter.notifyItemChanged(dayIndex);
            });

            holder.itemView.setOnClickListener(v -> showAddEditExerciseDialog(dayIndex, holder.getAdapterPosition()));
        }

        @Override
        public int getItemCount() {
            return exercises != null ? exercises.size() : 0;
        }

        class VH extends RecyclerView.ViewHolder {
            ImageView ivExImg, btnRemove;
            TextView tvExName, tvExDetail;

            VH(View v) {
                super(v);
                ivExImg = v.findViewById(R.id.ivExerciseImg);
                tvExName = v.findViewById(R.id.tvExName);
                tvExDetail = v.findViewById(R.id.tvExDetail);
                btnRemove = v.findViewById(R.id.btnRemoveExercise);
            }
        }
    }
}
