package com.fitai.gym;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.firestore.DocumentSnapshot;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class NutritionActivity extends AppCompatActivity {

    private TextView tvHeaderTitle;
    private RecyclerView rvPopular;
    private List<FoodModel> popularList = new ArrayList<>();
    private FoodAdapter adapter;

    private TextView tvRecName1, tvRecCal1, tvRecName2, tvRecCal2;
    private View cardRec1, cardRec2;
    private ImageView ivRec1, ivRec2;

    private FoodModel recMeal1, recMeal2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_nutrition);

        tvHeaderTitle = findViewById(R.id.tvHeaderTitle);
        rvPopular = findViewById(R.id.rvPopular);

        tvRecName1 = findViewById(R.id.tvFoodName); // Wait, let's search if these recommendation titles have IDs or if we can bind them
        // Let's look at R.id references in activity_nutrition:
        // Pancake view button is btnViewPancake, bread view button is btnViewBread.
        // Let's bind directly to btnViewPancake and btnViewBread click listeners!

        findViewById(R.id.ivBack).setOnClickListener(v -> finish());

        String mealType = getIntent().getStringExtra("meal_type");
        if (mealType == null) mealType = "Breakfast";
        tvHeaderTitle.setText(mealType);

        rvPopular.setLayoutManager(new LinearLayoutManager(this));
        adapter = new FoodAdapter(this, popularList);
        rvPopular.setAdapter(adapter);

        setupBottomNav();
        fetchMeals(mealType);
    }

    private void setupBottomNav() {
        BottomNavigationView bottomNav = findViewById(R.id.bottomNav);
        bottomNav.setSelectedItemId(R.id.nav_meals);
        bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_home) {
                Intent intent = new Intent(this, MainActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                startActivity(intent);
                overridePendingTransition(0, 0); finish(); return true;
            } else if (id == R.id.nav_activity) {
                Intent intent = new Intent(this, WorkoutTrackerActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                startActivity(intent);
                overridePendingTransition(0, 0); finish(); return true;
            } else if (id == R.id.nav_meals) {
                return true;
            } else if (id == R.id.nav_camera) {
                Intent intent = new Intent(this, ProgressPhotoActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                startActivity(intent);
                overridePendingTransition(0, 0); finish(); return true;
            } else if (id == R.id.nav_profile) {
                Intent intent = new Intent(this, ProfileActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                startActivity(intent);
                overridePendingTransition(0, 0); finish(); return true;
            }
            return false;
        });
    }

    private void fetchMeals(String mealType) {
        FirebaseHelper.getInstance().getUsersCollection().document("admin").collection("meals")
            .whereEqualTo("mealType", mealType)
            .get()
            .addOnSuccessListener(snapshot -> {
                boolean needsReseed = (snapshot == null || snapshot.isEmpty() || snapshot.size() < 10);

                // Collect stale doc refs (those missing "Step 1:" in instructions)
                List<com.google.firebase.firestore.DocumentReference> staleRefs = new ArrayList<>();
                if (!needsReseed && snapshot != null) {
                    for (DocumentSnapshot doc : snapshot.getDocuments()) {
                        String instr = doc.getString("instructions");
                        if (instr == null || !instr.contains("Step 1:")) {
                            needsReseed = true;
                        }
                        staleRefs.add(doc.getReference());
                    }
                }

                if (needsReseed) {
                    // Seed fresh data FIRST — delete stale refs only after all writes succeed
                    final List<com.google.firebase.firestore.DocumentReference> toDelete = new ArrayList<>(staleRefs);
                    seedDefaultMealsAndClean(mealType, toDelete);
                } else {
                    popularList.clear();
                    for (DocumentSnapshot doc : snapshot.getDocuments()) {
                        FoodModel meal = doc.toObject(FoodModel.class);
                        if (meal != null) {
                            meal.setId(doc.getId());
                            popularList.add(meal);
                        }
                    }
                    adapter.notifyDataSetChanged();
                    bindRecommendations();
                }
            })
            .addOnFailureListener(e -> {
                Toast.makeText(this, "Failed to load meals: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            });
    }

    // Overload — called when there are no old docs to clean up
    private void seedDefaultMeals(String mealType) {
        seedDefaultMeals(mealType, new ArrayList<>());
    }

    private void seedDefaultMeals(String mealType,
            List<com.google.firebase.firestore.DocumentReference> staleToDelete) {
        List<FoodModel> defaults = new ArrayList<>();
        if ("Breakfast".equalsIgnoreCase(mealType)) {
            defaults.add(new FoodModel("Honey Pancake", "Easy | 30mins | 180kCal", "Breakfast",
                    "Step 1: Whisk 1 cup flour, 1 tsp baking powder, and 1 egg in a bowl. Step 2: Slowly pour 1 cup milk, mixing until smooth. Step 3: Mix in 2 tbsp raw honey. Step 4: Melt butter in a pan over medium heat. Step 5: Cook 1/4 cup batter portions until bubbly, flip and brown. Step 6: Serve topped with extra honey.",
                    "Flour, Milk, Honey, Egg, Baking Powder", "pancake_1"));
            defaults.add(new FoodModel("Canai Bread", "Medium | 20mins | 230kCal", "Breakfast",
                    "Step 1: Knead 2 cups flour, salt, water, and 1 tbsp butter into dough. Step 2: Let it rest for 30 minutes. Step 3: Divide into balls, oil them, stretch thin, and coil. Step 4: Flatten coils slightly. Step 5: Fry in a hot pan for 3 minutes per side until flaky. Step 6: Slap gently to fluff and serve hot.",
                    "Flour, Butter, Water, Salt", "chicken"));
            defaults.add(new FoodModel("Healthy Coffee", "Easy | 5mins | 60kCal", "Breakfast",
                    "Step 1: Grind premium dark roast coffee beans. Step 2: Brew in a French press with boiling water. Step 3: Warm 1/4 cup almond milk. Step 4: Pour coffee into a mug. Step 5: Stir in warm milk and 1 tsp stevia. Step 6: Sprinkle cinnamon on top and serve.",
                    "Coffee Beans, Almond Milk, Stevia", "coffee"));
            defaults.add(new FoodModel("Oatmeal Oats", "Easy | 10mins | 150kCal", "Breakfast",
                    "Step 1: Combine 1/2 cup rolled oats and 1 cup almond milk in a pot. Step 2: Simmer over medium heat for 6 minutes, stirring occasionally. Step 3: Transfer to a bowl. Step 4: Arrange fresh washed blueberries and raspberries on top. Step 5: Scatter 1 tbsp chia seeds. Step 6: Serve warm.",
                    "Oats, Almond Milk, Berries, Chia Seeds", "oatmeal"));
            defaults.add(new FoodModel("Avocado Toast", "Easy | 8mins | 210kCal", "Breakfast",
                    "Step 1: Toast 2 slices of whole wheat bread. Step 2: Mash 1 ripe avocado in a bowl. Step 3: Season with salt, pepper, and 1 tsp lemon juice. Step 4: Spread the avocado paste evenly onto the warm toasts. Step 5: Sprinkle with red chili flakes. Step 6: Slice diagonally and enjoy.",
                    "Whole wheat bread, Avocado, Lemon, Salt, Pepper", "salad"));
            defaults.add(new FoodModel("Egg Omelette", "Easy | 12mins | 140kCal", "Breakfast",
                    "Step 1: Whisk 3 eggs with salt and black pepper. Step 2: Sauté 1/2 cup spinach and sliced mushrooms in 1 tbsp melted butter for 2 minutes. Step 3: Pour the eggs over the vegetables. Step 4: Cook over medium-low heat for 3 minutes. Step 5: Fold the omelette in half. Step 6: Serve hot.",
                    "Eggs, Baby Spinach, Mushrooms, Butter", "salad"));
            defaults.add(new FoodModel("Berry Smoothie", "Easy | 5mins | 120kCal", "Breakfast",
                    "Step 1: Wash 1 cup mixed berries. Step 2: Place berries, 1/2 cup greek yogurt, and 1/2 cup milk in a blender. Step 3: Add 1 tbsp honey and 1/2 cup ice. Step 4: Blend on high speed for 1 minute. Step 5: Stir and blend for 30 more seconds until creamy. Step 6: Pour into a glass and serve.",
                    "Berries, Greek Yogurt, Honey, Skim Milk", "orange"));
            defaults.add(new FoodModel("Chia Pudding", "Easy | 10mins | 110kCal", "Breakfast",
                    "Step 1: Whisk 3 tbsp chia seeds, 1 cup almond milk, and 1 tbsp honey in a jar. Step 2: Let rest for 10 minutes. Step 3: Whisk again to break clumps. Step 4: Cover and refrigerate for at least 4 hours. Step 5: Scoop pudding into a glass. Step 6: Garnish with sliced strawberries.",
                    "Chia Seeds, Almond Milk, Raw Honey", "oatmeal"));
            defaults.add(new FoodModel("Protein Waffles", "Medium | 20mins | 290kCal", "Breakfast",
                    "Step 1: Heat a waffle iron and coat with non-stick spray. Step 2: Mix 1 scoop whey protein, 1/2 cup oat flour, and 1/2 tsp baking powder. Step 3: Whisk in 1 egg and 1/2 cup milk. Step 4: Pour batter onto iron. Step 5: Bake for 4 minutes until crispy. Step 6: Serve warm.",
                    "Whey Protein, Flour, Eggs, Almond Milk", "pancake_1"));
            defaults.add(new FoodModel("Greek Yogurt Bowl", "Easy | 6mins | 130kCal", "Breakfast",
                    "Step 1: Scoop 1 cup greek yogurt into a clean bowl. Step 2: Arrange 1/4 cup granola along one side. Step 3: Place 1/4 cup chopped walnuts in the center. Step 4: Drizzle 2 tbsp raw honey in a zigzag pattern. Step 5: Garnish with fresh mint. Step 6: Mix gently and eat cold.",
                    "Greek Yogurt, Walnuts, Honey", "glass_of_milk"));
        } else if ("Lunch".equalsIgnoreCase(mealType)) {
            defaults.add(new FoodModel("Chicken Steak", "Medium | 25mins | 420kCal", "Lunch",
                    "Step 1: Pound chicken breast to an even thickness. Step 2: Rub with minced garlic, rosemary, salt, pepper, and olive oil. Step 3: Marinate for 15 minutes. Step 4: Heat a grill pan over medium-high heat. Step 5: Grill chicken for 5 minutes per side. Step 6: Rest 3 minutes, then slice.",
                    "Chicken Breast, Garlic, Olive Oil, Rosemary, Salt", "chicken"));
            defaults.add(new FoodModel("Salmon Nigiri", "Medium | 20mins | 120kCal", "Lunch",
                    "Step 1: Cook sushi rice and season with rice vinegar. Let cool. Step 2: Slice fresh salmon fillet into bite-sized rectangular strips. Step 3: Wet hands with water. Step 4: Mold a small block of rice in your palm. Step 5: Dab wasabi on salmon and press onto the rice. Step 6: Serve with soy sauce.",
                    "Sushi Rice, Fresh Salmon, Wasabi, Soy Sauce", "nigiri"));
            defaults.add(new FoodModel("Fresh Organic Milk", "Easy | 2mins | 150kCal", "Lunch",
                    "Step 1: Take cold organic milk from the refrigerator. Step 2: Get a clean, tall drinking glass. Step 3: Pour the milk steadily into the glass. Step 4: Place the milk carton back in the fridge immediately. Step 5: Serve chilled. Step 6: Enjoy alongside your lunch meal.",
                    "Grass-fed Organic Milk", "glass_of_milk"));
            defaults.add(new FoodModel("Beef Tacos", "Medium | 15mins | 350kCal", "Lunch",
                    "Step 1: Sauté ground beef in a skillet until browned, drain fat. Step 2: Stir in taco seasoning and 1/4 cup water, simmer for 5 minutes. Step 3: Warm taco shells in the oven. Step 4: Spoon seasoned beef into shells. Step 5: Add lettuce, cheese, and tomatoes. Step 6: Serve hot.",
                    "Lean Ground Beef, Taco Shells, Lettuce, Shredded Cheese", "chicken"));
            defaults.add(new FoodModel("Tuna Salad Wrap", "Easy | 10mins | 280kCal", "Lunch",
                    "Step 1: Drain canned tuna completely. Step 2: In a bowl, mix tuna, light mayo, diced tomatoes, salt, and pepper. Step 3: Warm a flour tortilla in a pan. Step 4: Spread the tuna salad mixture in the center. Step 5: Top with shredded lettuce. Step 6: Fold sides, roll tightly, and serve.",
                    "Canned Tuna, Light Mayo, Whole wheat tortilla, Lettuce", "salad"));
            defaults.add(new FoodModel("Quinoa Salad", "Easy | 12mins | 220kCal", "Lunch",
                    "Step 1: Boil 1/2 cup quinoa in 1 cup water for 15 minutes, let cool. Step 2: Chop cucumber and tomatoes. Step 3: Combine cooled quinoa and vegetables in a salad bowl. Step 4: Drizzle with olive oil and fresh lemon juice. Step 5: Season with salt and pepper. Step 6: Toss well and serve.",
                    "Quinoa, Cucumber, Tomato, Olive oil", "salad"));
            defaults.add(new FoodModel("Turkey Sandwich", "Easy | 8mins | 310kCal", "Lunch",
                    "Step 1: Lay two slices of whole wheat bread on a plate. Step 2: Spread mayo on both slices. Step 3: Arrange lettuce leaves and tomato slices on one piece. Step 4: Layer folded turkey slices on top. Step 5: Place the other bread slice on top. Step 6: Press gently, slice in half, and serve.",
                    "Smoked Turkey Slices, Whole wheat bread, Lettuce, Mayo", "chicken"));
            defaults.add(new FoodModel("Brown Rice & Chicken", "Medium | 30mins | 450kCal", "Lunch",
                    "Step 1: Boil brown rice in water for 35 minutes until tender. Step 2: Cut chicken breast into small cubes. Step 3: Sauté chicken in olive oil with salt and oregano for 8 minutes. Step 4: Drain and set aside. Step 5: Bowl up the brown rice. Step 6: Top with chicken and serve.",
                    "Brown Rice, Chicken breast, Olive oil, Salt", "chicken"));
            defaults.add(new FoodModel("Sushi Bowl", "Medium | 22mins | 330kCal", "Lunch",
                    "Step 1: Scoop cooked seasoned rice into a bowl. Step 2: Shred crab sticks using a fork. Step 3: Dice cucumber and avocado. Step 4: Arrange crab, cucumber, and avocado in separate sections over the rice. Step 5: Drizzle with soy sauce. Step 6: Sprinkle sesame seeds on top.",
                    "Sushi Rice, Crab sticks, Avocado, Cucumber", "nigiri"));
            defaults.add(new FoodModel("Pasta Carbonara", "Hard | 25mins | 520kCal", "Lunch",
                    "Step 1: Boil spaghetti, reserving 1/4 cup pasta water. Step 2: Whisk egg yolk, parmesan cheese, and black pepper. Step 3: Fry chopped bacon until crispy. Step 4: Toss hot pasta into the bacon pan off heat. Step 5: Add egg mixture and pasta water quickly to emulsify. Step 6: Serve hot.",
                    "Pasta, Egg Yolk, Parmesan Cheese, Smoked Bacon", "chicken"));
        } else if ("Snacks".equalsIgnoreCase(mealType)) {
            defaults.add(new FoodModel("Blueberry Pancake", "Easy | 15mins | 200kCal", "Snacks",
                    "Step 1: Blend rolled oats, milk, stevia, and baking powder. Step 2: Fold in fresh blueberries. Step 3: Pour small portions onto a heated non-stick pan. Step 4: Cook until bubbles surface, then flip. Step 5: Cook the other side for 2 minutes. Step 6: Serve warm.",
                    "Blueberry, Oats, Almond Milk, Stevia", "pancake_1"));
            defaults.add(new FoodModel("Juicy Orange", "Easy | 2mins | 80kCal", "Snacks",
                    "Step 1: Wash the fresh orange thoroughly. Step 2: Slice the orange into quarters on a cutting board. Step 3: Peel the outer skin off each quarter. Step 4: Separate individual segments. Step 5: Place them in a snack bowl. Step 6: Eat fresh or serve chilled.",
                    "Fresh Orange", "orange"));
            defaults.add(new FoodModel("Warm Apple Pie", "Hard | 45mins | 310kCal", "Snacks",
                    "Step 1: Core and slice apples into thin wedges. Step 2: Toss with cinnamon and brown sugar. Step 3: Lay pastry dough in a dish. Step 4: Arrange apples inside. Step 5: Fold dough edges, brush with egg wash. Step 6: Bake at 375F for 30 minutes until golden.",
                    "Apples, Cinnamon, Pastry Dough, Brown Sugar", "apple_pie"));
            defaults.add(new FoodModel("Almonds & Nuts", "Easy | 2mins | 160kCal", "Snacks",
                    "Step 1: Measure 1/4 cup raw almonds. Step 2: Measure 1/4 cup raw walnuts. Step 3: Toast nuts in a dry pan over low heat for 3 minutes, stirring constantly. Step 4: Remove from heat. Step 5: Let them cool to room temperature. Step 6: Serve in a small snack bowl.",
                    "Raw Almonds, Roasted Walnuts", "orange"));
            defaults.add(new FoodModel("Protein Bar", "Easy | 1mins | 210kCal", "Snacks",
                    "Step 1: Mix whey protein, oats, and honey into a thick dough. Step 2: Press dough flat into a lined square baking dish. Step 3: Drizzle melted dark chocolate over the surface. Step 4: Freeze for 30 minutes. Step 5: Cut into rectangular bars. Step 6: Keep refrigerated.",
                    "Whey Protein, Oats, Chocolate Coating", "apple_pie"));
            defaults.add(new FoodModel("Hummus & Carrots", "Easy | 5mins | 130kCal", "Snacks",
                    "Step 1: Wash and peel fresh carrots. Step 2: Slice into 3-inch long dipping sticks. Step 3: Scoop garlic hummus into a serving dish. Step 4: Arrange carrot sticks on a plate around the hummus. Step 5: Keep chilled until serving. Step 6: Dip and enjoy.",
                    "Carrots, Hummus", "salad"));
            defaults.add(new FoodModel("Rice Cakes", "Easy | 3mins | 90kCal", "Snacks",
                    "Step 1: Lay two organic rice cakes on a plate. Step 2: Scoop 1 tbsp creamy almond butter. Step 3: Spread it evenly across the surface of the first cake. Step 4: Repeat for the second cake. Step 5: Garnish with sliced banana if desired. Step 6: Serve immediately.",
                    "Rice Cakes, Almond butter", "oatmeal"));
            defaults.add(new FoodModel("Mixed Berries", "Easy | 2mins | 70kCal", "Snacks",
                    "Step 1: Place fresh raspberries and blackberries in a colander. Step 2: Wash under cold running water. Step 3: Pat dry gently with a paper towel. Step 4: Discard stem remnants. Step 5: Place in a clean glass bowl. Step 6: Serve cold.",
                    "Raspberries, Blackberries", "orange"));
            defaults.add(new FoodModel("Dark Chocolate", "Easy | 1mins | 140kCal", "Snacks",
                    "Step 1: Retrieve organic dark chocolate bar. Step 2: Break off exactly 4 squares. Step 3: Place on a serving plate. Step 4: Store remaining chocolate in a cool cabinet. Step 5: Let sit to reach room temperature. Step 6: Enjoy slowly to savor the flavor.",
                    "70% Cocoa Dark Chocolate", "coffee"));
            defaults.add(new FoodModel("Banana & Peanut Butter", "Easy | 4mins | 220kCal", "Snacks",
                    "Step 1: Peel a ripe banana. Step 2: Slice the banana into thin round disks. Step 3: Arrange slices on a plate. Step 4: Scoop 2 tbsp peanut butter into a small dish in the center. Step 5: Dip slices into the peanut butter. Step 6: Serve immediately.",
                    "Banana, Peanut butter", "orange"));
        } else { // Dinner
            defaults.add(new FoodModel("Summer Salad", "Easy | 10mins | 140kCal", "Dinner",
                    "Step 1: Wash romaine lettuce, tomatoes, and cucumber. Step 2: Chop the lettuce and tomatoes, slice the cucumber. Step 3: Combine in a salad bowl. Step 4: Whisk olive oil, lemon juice, and salt in a cup. Step 5: Pour dressing over salad. Step 6: Toss gently and serve.",
                    "Lettuce, Tomatoes, Cucumber, Olive Oil, Lemon", "salad"));
            defaults.add(new FoodModel("Organic Oatmeal", "Easy | 8mins | 180kCal", "Dinner",
                    "Step 1: Add 1/2 cup steel-cut oats and 1 cup almond milk to a pot. Step 2: Cook over medium heat, bringing to a simmer. Step 3: Stir for 8 minutes until creamy. Step 4: Pour into a bowl. Step 5: Top with fresh blueberries. Step 6: Dust with cinnamon and serve.",
                    "Oats, Almond Milk, Berries", "oatmeal"));
            defaults.add(new FoodModel("Grilled Salmon", "Medium | 20mins | 380kCal", "Dinner",
                    "Step 1: Pat salmon fillet dry. Step 2: Rub with minced garlic, lemon juice, olive oil, salt, and pepper. Step 3: Heat a grill pan. Step 4: Cook skin-side down for 5 minutes. Step 5: Flip and cook for 3 minutes. Step 6: Serve hot garnished with lemon wedges.",
                    "Salmon fillet, Garlic, Lemon, Olive oil", "nigiri"));
            defaults.add(new FoodModel("Chicken Caesar Salad", "Medium | 15mins | 290kCal", "Dinner",
                    "Step 1: Grill chicken breast and slice into strips. Step 2: Chop romaine lettuce. Step 3: Toss lettuce with light Caesar dressing in a large bowl. Step 4: Place grilled chicken strips on top. Step 5: Scatter croutons. Step 6: Garnish with shaved parmesan.",
                    "Chicken Breast, Romaine, Caesar dressing, Croutons", "salad"));
            defaults.add(new FoodModel("Vegetable Soup", "Easy | 25mins | 110kCal", "Dinner",
                    "Step 1: Dice carrots, celery, and potatoes. Step 2: Bring vegetable stock to a boil in a pot. Step 3: Add diced vegetables. Step 4: Simmer covered for 20 minutes until tender. Step 5: Season with salt and pepper. Step 6: Ladle hot soup into bowls and serve.",
                    "Carrots, Celery, Potatoes, Vegetable stock", "salad"));
            defaults.add(new FoodModel("Baked Sweet Potato", "Easy | 35mins | 160kCal", "Dinner",
                    "Step 1: Preheat oven to 400F. Step 2: Scrub sweet potato and poke holes with a fork. Step 3: Bake on a sheet for 35 minutes until tender. Step 4: Slice open down the center. Step 5: Insert a pat of butter. Step 6: Sprinkle cinnamon on top and serve hot.",
                    "Sweet potato, Cinnamon", "salad"));
            defaults.add(new FoodModel("Tofu Stir Fry", "Medium | 18mins | 240kCal", "Dinner",
                    "Step 1: Press firm tofu to drain water, cut into cubes. Step 2: Sauté tofu cubes in olive oil until golden on all sides. Step 3: Add minced garlic and broccoli florets, stir-frying for 3 minutes. Step 4: Pour in soy sauce. Step 5: Toss well. Step 6: Serve hot.",
                    "Organic Tofu, Broccoli, Soy sauce", "salad"));
            defaults.add(new FoodModel("Beef Broccoli", "Medium | 22mins | 390kCal", "Dinner",
                    "Step 1: Slice lean beef into thin strips. Step 2: Sauté minced garlic and beef in oil on high heat for 3 minutes. Step 3: Add broccoli florets and 2 tbsp water, cover and steam for 2 minutes. Step 4: Pour in oyster sauce. Step 5: Toss to coat. Step 6: Serve hot.",
                    "Lean Beef, Broccoli, Oyster sauce, Garlic", "chicken"));
            defaults.add(new FoodModel("Shrimp Pasta", "Hard | 25mins | 410kCal", "Dinner",
                    "Step 1: Boil penne pasta in salted water, drain. Step 2: Sauté minced garlic and raw shrimp in butter for 3 minutes until pink. Step 3: Add cooked pasta to the skillet. Step 4: Season with salt and black pepper. Step 5: Toss to combine. Step 6: Serve hot.",
                    "Tender Shrimp, Penne pasta, Butter, Garlic", "chicken"));
            defaults.add(new FoodModel("Lentil Dahl", "Medium | 30mins | 270kCal", "Dinner",
                    "Step 1: Rinse red lentils. Step 2: Sauté diced onion and garlic in a pot. Step 3: Add cumin, turmeric, and lentils. Step 4: Pour in water and bring to a boil. Step 5: Cover and simmer on low for 20 minutes until creamy. Step 6: Serve hot with coriander.",
                    "Red lentils, Turmeric, Cumin, Garlic, Onions", "salad"));
        }

        // Save to Firestore
        // Use a counter — only delete old docs after ALL new writes succeed
        final int[] successCount = {0};
        final int total = defaults.size();
        for (FoodModel meal : defaults) {
            FirebaseHelper.getInstance().getUsersCollection().document("admin").collection("meals")
                .add(meal)
                .addOnSuccessListener(ref -> {
                    successCount[0]++;
                    if (successCount[0] == total) {
                        // All new docs written — now safe to delete old stale refs
                        for (com.google.firebase.firestore.DocumentReference ref2 : staleToDelete) {
                            ref2.delete();
                        }
                        // Show fresh data
                        popularList.clear();
                        popularList.addAll(defaults);
                        adapter.notifyDataSetChanged();
                        bindRecommendations();
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Seed failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
        }
        // Show local data immediately while Firestore writes are in-flight
        popularList.clear();
        popularList.addAll(defaults);
        adapter.notifyDataSetChanged();
        bindRecommendations();
    }

    // Called by fetchMeals — seeds new docs, then deletes stale ones only after success
    private void seedDefaultMealsAndClean(String mealType,
            List<com.google.firebase.firestore.DocumentReference> staleToDelete) {
        seedDefaultMeals(mealType, staleToDelete);
    }

    private void bindRecommendations() {
        if (popularList.size() >= 1) {
            recMeal1 = popularList.get(0);
            TextView tvRecName1 = findViewById(R.id.tvRecName1);
            TextView tvRecDetails1 = findViewById(R.id.tvRecDetails1);
            ImageView ivRecImage1 = findViewById(R.id.ivRecImage1);

            if (tvRecName1 != null) tvRecName1.setText(recMeal1.getName());
            if (tvRecDetails1 != null) tvRecDetails1.setText(recMeal1.getCalories());
            if (ivRecImage1 != null) {
                ImageLoaderHelper.loadImage(this, ivRecImage1, recMeal1.getImageName(), R.drawable.pancake_1);
            }
            findViewById(R.id.btnViewPancake).setOnClickListener(v -> openMealDetails(recMeal1));
        }
        if (popularList.size() >= 2) {
            recMeal2 = popularList.get(1);
            TextView tvRecName2 = findViewById(R.id.tvRecName2);
            TextView tvRecDetails2 = findViewById(R.id.tvRecDetails2);
            ImageView ivRecImage2 = findViewById(R.id.ivRecImage2);

            if (tvRecName2 != null) tvRecName2.setText(recMeal2.getName());
            if (tvRecDetails2 != null) tvRecDetails2.setText(recMeal2.getCalories());
            if (ivRecImage2 != null) {
                ImageLoaderHelper.loadImage(this, ivRecImage2, recMeal2.getImageName(), R.drawable.pancake_1);
            }
            findViewById(R.id.btnViewBread).setOnClickListener(v -> openMealDetails(recMeal2));
        }
    }

    private void openMealDetails(FoodModel meal) {
        Intent intent = new Intent(this, MealDetailActivity.class);
        intent.putExtra("meal_type", meal.getMealType());
        intent.putExtra("meal_id", meal.getId());
        intent.putExtra("meal_name", meal.getName());
        intent.putExtra("meal_calories", meal.getCalories());
        intent.putExtra("meal_instructions", meal.getInstructions());
        intent.putExtra("meal_ingredients", meal.getIngredients());
        intent.putExtra("meal_image_name", meal.getImageName());
        startActivity(intent);
    }
}
