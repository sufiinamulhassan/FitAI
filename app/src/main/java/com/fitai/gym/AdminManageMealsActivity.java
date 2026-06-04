package com.fitai.gym;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import android.content.Intent;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AdminManageMealsActivity extends AppCompatActivity {

    private RecyclerView rvMeals;
    private FloatingActionButton fabAddMeal;
    private ImageView btnBack;
    private List<FoodModel> mealList = new ArrayList<>();
    private MealsAdapter adapter;
    private String selectedBase64Image = null;
    private ImageView ivDialogPreview = null;

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
                if (ivDialogPreview != null) {
                    ivDialogPreview.setImageBitmap(resized);
                }
            }
        } catch (Exception e) {
            Toast.makeText(this, "Failed to process image: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_manage_meals);

        btnBack = findViewById(R.id.btnBack);
        rvMeals = findViewById(R.id.rvAdminMeals);
        fabAddMeal = findViewById(R.id.fabAddMeal);

        btnBack.setOnClickListener(v -> finish());
        fabAddMeal.setOnClickListener(v -> showAddEditMealDialog(null));

        rvMeals.setLayoutManager(new LinearLayoutManager(this));
        adapter = new MealsAdapter();
        rvMeals.setAdapter(adapter);

        loadMeals();
    }

    private void loadMeals() {
        FirebaseHelper.getInstance().getUsersCollection().document("admin").collection("meals")
            .orderBy("createdAt", com.google.firebase.firestore.Query.Direction.DESCENDING)
            .get()
            .addOnSuccessListener(snapshot -> {
                mealList.clear();
                if (snapshot != null) {
                    for (DocumentSnapshot doc : snapshot.getDocuments()) {
                        FoodModel meal = doc.toObject(FoodModel.class);
                        if (meal != null) {
                            meal.setId(doc.getId());
                            mealList.add(meal);
                        }
                    }
                }
                adapter.notifyDataSetChanged();
            })
            .addOnFailureListener(e -> {
                Toast.makeText(this, "Failed to load meals: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            });
    }

    private void showAddEditMealDialog(FoodModel mealToEdit) {
        selectedBase64Image = null; // reset
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_add_edit_meal, null);
        EditText etName = dialogView.findViewById(R.id.etMealName);
        Spinner spCategory = dialogView.findViewById(R.id.spMealCategory);
        EditText etCalories = dialogView.findViewById(R.id.etMealCalories);
        Spinner spImage = dialogView.findViewById(R.id.spMealImage);
        EditText etIngredients = dialogView.findViewById(R.id.etMealIngredients);
        EditText etInstructions = dialogView.findViewById(R.id.etMealInstructions);
        ImageView ivMealDialogPreview = dialogView.findViewById(R.id.ivMealDialogPreview);
        android.widget.Button btnMealDialogUpload = dialogView.findViewById(R.id.btnMealDialogUpload);

        ivDialogPreview = ivMealDialogPreview;

        // Populate Categories
        String[] categories = {"Breakfast", "Lunch", "Snacks", "Dinner"};
        ArrayAdapter<String> catAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_dropdown_item, categories);
        spCategory.setAdapter(catAdapter);

        // Populate Images
        String[] images = {"pancake_1", "chicken", "nigiri", "salad", "apple_pie", "orange", "coffee", "glass_of_milk", "oatmeal"};
        ArrayAdapter<String> imgAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_dropdown_item, images);
        spImage.setAdapter(imgAdapter);

        btnMealDialogUpload.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
            intent.setType("image/*");
            pickImageLauncher.launch(intent);
        });

        // Steps Builder setup
        LinearLayout llStepsContainer = dialogView.findViewById(R.id.llRecipeStepsContainer);
        android.widget.Button btnAddStep = dialogView.findViewById(R.id.btnAddRecipeStep);
        List<EditText> stepEditTexts = new ArrayList<>();

        // Helper to update numbers
        Runnable updateStepNumbers = new Runnable() {
            @Override
            public void run() {
                for (int i = 0; i < llStepsContainer.getChildCount(); i++) {
                    View row = llStepsContainer.getChildAt(i);
                    TextView tvNum = row.findViewById(R.id.tvStepNumber);
                    if (tvNum != null) {
                        tvNum.setText("Step " + (i + 1) + ":");
                    }
                }
            }
        };

        // Helper to add step row
        class StepHelper {
            void addField(String text) {
                View stepRow = LayoutInflater.from(AdminManageMealsActivity.this)
                        .inflate(R.layout.item_admin_recipe_step, llStepsContainer, false);
                EditText etStepText = stepRow.findViewById(R.id.etStepText);
                View ivDelete = stepRow.findViewById(R.id.ivDeleteStep);

                if (text != null) {
                    etStepText.setText(text);
                }

                stepEditTexts.add(etStepText);
                ivDelete.setOnClickListener(v -> {
                    llStepsContainer.removeView(stepRow);
                    stepEditTexts.remove(etStepText);
                    updateStepNumbers.run();
                });

                llStepsContainer.addView(stepRow);
                updateStepNumbers.run();
            }
        }
        StepHelper stepHelper = new StepHelper();

        btnAddStep.setOnClickListener(v -> stepHelper.addField(""));

        if (mealToEdit != null) {
            etName.setText(mealToEdit.getName());
            etCalories.setText(mealToEdit.getCalories());
            etIngredients.setText(mealToEdit.getIngredients());
            etInstructions.setText(mealToEdit.getInstructions());

            String currentInstructions = mealToEdit.getInstructions();
            if (currentInstructions != null && currentInstructions.contains("Step 1:")) {
                String[] parts = currentInstructions.split("Step \\d+:");
                for (String part : parts) {
                    String trimmed = part.trim();
                    if (!trimmed.isEmpty()) {
                        if (trimmed.endsWith(".")) {
                            trimmed = trimmed.substring(0, trimmed.length() - 1);
                        }
                        stepHelper.addField(trimmed);
                    }
                }
            } else if (currentInstructions != null && !currentInstructions.isEmpty()) {
                stepHelper.addField(currentInstructions);
            }

            // Select Category
            for (int i = 0; i < categories.length; i++) {
                if (categories[i].equalsIgnoreCase(mealToEdit.getMealType())) {
                    spCategory.setSelection(i);
                    break;
                }
            }

            // Load cover preview
            if (mealToEdit.getImageName() != null && mealToEdit.getImageName().startsWith("base64:")) {
                selectedBase64Image = mealToEdit.getImageName();
                ImageLoaderHelper.loadImage(this, ivMealDialogPreview, selectedBase64Image, R.drawable.pancake_1);
            } else {
                // Select Image Spinner
                for (int i = 0; i < images.length; i++) {
                    if (images[i].equalsIgnoreCase(mealToEdit.getImageName())) {
                        spImage.setSelection(i);
                        break;
                    }
                }
                ImageLoaderHelper.loadImage(this, ivMealDialogPreview, mealToEdit.getImageName(), R.drawable.pancake_1);
            }
        }

        // Listener to change preview when spinner changes (only if no custom image is selected yet)
        spImage.setOnItemSelectedListener(new android.widget.AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(android.widget.AdapterView<?> parent, View view, int position, long id) {
                if (selectedBase64Image == null) {
                    String selectedPreset = images[position];
                    ImageLoaderHelper.loadImage(AdminManageMealsActivity.this, ivMealDialogPreview, selectedPreset, R.drawable.pancake_1);
                }
            }
            @Override
            public void onNothingSelected(android.widget.AdapterView<?> parent) {}
        });

        new AlertDialog.Builder(this)
            .setTitle(mealToEdit == null ? "Add New Meal" : "Edit Meal")
            .setView(dialogView)
            .setPositiveButton("Save", (d, w) -> {
                String name = etName.getText().toString().trim();
                String category = spCategory.getSelectedItem().toString();
                String cals = etCalories.getText().toString().trim();
                String img = (selectedBase64Image != null) ? selectedBase64Image : spImage.getSelectedItem().toString();
                String ingredients = etIngredients.getText().toString().trim();
                String instructions = etInstructions.getText().toString().trim();

                if (!stepEditTexts.isEmpty()) {
                    StringBuilder sb = new StringBuilder();
                    int num = 1;
                    for (EditText et : stepEditTexts) {
                        String stepTxt = et.getText().toString().trim();
                        if (!stepTxt.isEmpty()) {
                            if (sb.length() > 0) sb.append(" ");
                            sb.append("Step ").append(num).append(": ").append(stepTxt);
                            if (!stepTxt.endsWith(".")) sb.append(".");
                            num++;
                        }
                    }
                    if (sb.length() > 0) {
                        instructions = sb.toString();
                    }
                }

                if (name.isEmpty() || cals.isEmpty()) {
                    Toast.makeText(this, "Name and calories are required", Toast.LENGTH_SHORT).show();
                    return;
                }

                Map<String, Object> mealData = new HashMap<>();
                mealData.put("name", name);
                mealData.put("mealType", category);
                mealData.put("calories", cals);
                mealData.put("imageName", img);
                mealData.put("ingredients", ingredients);
                mealData.put("instructions", instructions);

                if (mealToEdit == null) {
                    mealData.put("createdAt", System.currentTimeMillis());
                    FirebaseHelper.getInstance().getUsersCollection().document("admin").collection("meals")
                        .add(mealData)
                        .addOnSuccessListener(ref -> {
                            Toast.makeText(this, "Meal added successfully!", Toast.LENGTH_SHORT).show();
                            loadMeals();
                        })
                        .addOnFailureListener(e -> {
                            Toast.makeText(this, "Failed to add meal: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        });
                } else {
                    FirebaseHelper.getInstance().getUsersCollection().document("admin").collection("meals")
                        .document(mealToEdit.getId())
                        .update(mealData)
                        .addOnSuccessListener(v -> {
                            Toast.makeText(this, "Meal updated successfully!", Toast.LENGTH_SHORT).show();
                            loadMeals();
                        })
                        .addOnFailureListener(e -> {
                            Toast.makeText(this, "Failed to update meal: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        });
                }
            })
            .setNegativeButton("Cancel", null)
            .show();
    }

    private void deleteMeal(FoodModel meal, int position) {
        new AlertDialog.Builder(this)
            .setTitle("Delete Meal")
            .setMessage("Are you sure you want to delete " + meal.getName() + "?")
            .setPositiveButton("Delete", (d, w) -> {
                FirebaseHelper.getInstance().getUsersCollection().document("admin").collection("meals")
                    .document(meal.getId())
                    .delete()
                    .addOnSuccessListener(v -> {
                        mealList.remove(position);
                        adapter.notifyItemRemoved(position);
                        adapter.notifyItemRangeChanged(position, mealList.size());
                        Toast.makeText(this, "Meal deleted", Toast.LENGTH_SHORT).show();
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(this, "Failed to delete: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
            })
            .setNegativeButton("Cancel", null)
            .show();
    }

    private class MealsAdapter extends RecyclerView.Adapter<MealsAdapter.VH> {

        @NonNull
        @Override
        public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_admin_meal, parent, false);
            return new VH(v);
        }

        @Override
        public void onBindViewHolder(@NonNull VH holder, int position) {
            FoodModel meal = mealList.get(position);
            holder.tvName.setText(meal.getName());
            holder.tvCategory.setText(meal.getMealType());
            holder.tvCalories.setText(meal.getCalories());
            ImageLoaderHelper.loadImage(holder.itemView.getContext(), holder.ivIcon, meal.getImageName(), R.drawable.pancake_1);

            holder.btnEdit.setOnClickListener(v -> showAddEditMealDialog(meal));
            holder.btnDelete.setOnClickListener(v -> deleteMeal(meal, position));
        }

        @Override
        public int getItemCount() {
            return mealList.size();
        }

        class VH extends RecyclerView.ViewHolder {
            ImageView ivIcon, btnEdit, btnDelete;
            TextView tvName, tvCategory, tvCalories;

            VH(View v) {
                super(v);
                ivIcon = v.findViewById(R.id.ivAdminMealIcon);
                tvName = v.findViewById(R.id.tvAdminMealName);
                tvCategory = v.findViewById(R.id.tvAdminMealCategory);
                tvCalories = v.findViewById(R.id.tvAdminMealCalories);
                btnEdit = v.findViewById(R.id.btnAdminEditMeal);
                btnDelete = v.findViewById(R.id.btnAdminDeleteMeal);
            }
        }
    }
}
