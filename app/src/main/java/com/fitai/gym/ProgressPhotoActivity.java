package com.fitai.gym;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.bumptech.glide.Glide;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.firestore.DocumentSnapshot;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ProgressPhotoActivity extends AppCompatActivity {

    private RecyclerView rvGallery;
    private List<ProgressPhotoItem> photoList = new ArrayList<>();
    private GalleryAdapter adapter;

    // Camera Result Launcher
    private final ActivityResultLauncher<Intent> cameraLauncher = registerForActivityResult(
        new ActivityResultContracts.StartActivityForResult(),
        result -> {
            if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                try {
                    Bitmap bitmap = (Bitmap) result.getData().getExtras().get("data");
                    if (bitmap != null) {
                        uploadPhoto(bitmap);
                    }
                } catch (Exception e) {
                    Toast.makeText(this, "Failed to capture image", Toast.LENGTH_SHORT).show();
                }
            }
        });

    // Gallery / Photo Picker Launcher
    private final ActivityResultLauncher<String> galleryLauncher = registerForActivityResult(
        new ActivityResultContracts.GetContent(),
        uri -> {
            if (uri != null) {
                try {
                    InputStream inputStream = getContentResolver().openInputStream(uri);
                    Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
                    if (bitmap != null) {
                        uploadPhoto(bitmap);
                    }
                } catch (Exception e) {
                    Toast.makeText(this, "Failed to load image from gallery", Toast.LENGTH_SHORT).show();
                }
            }
        });

    private BottomNavigationView bottomNav;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_progress_photo);

        rvGallery = findViewById(R.id.rvGallery);
        rvGallery.setLayoutManager(new GridLayoutManager(this, 3));
        adapter = new GalleryAdapter(photoList);
        rvGallery.setAdapter(adapter);

        bottomNav = findViewById(R.id.bottomNav);
        setupNavigation();

        findViewById(R.id.ivBack).setOnClickListener(v -> finish());
        findViewById(R.id.fabCamera).setOnClickListener(v -> showImageSourceSelector());

        fetchPhotos();
    }

    private void showImageSourceSelector() {
        String[] options = {"Take Photo", "Choose from Gallery"};
        new AlertDialog.Builder(this)
                .setTitle("Add Progress Photo")
                .setItems(options, (dialog, which) -> {
                    if (which == 0) {
                        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                        cameraLauncher.launch(intent);
                    } else {
                        galleryLauncher.launch("image/*");
                    }
                })
                .show();
    }

    private void setupNavigation() {
        bottomNav.setSelectedItemId(R.id.nav_camera);
        bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_home) {
                Intent intent = new Intent(this, MainActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                startActivity(intent);
                overridePendingTransition(0, 0); return true;
            } else if (id == R.id.nav_activity) {
                Intent intent = new Intent(this, WorkoutTrackerActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                startActivity(intent);
                overridePendingTransition(0, 0); return true;
            } else if (id == R.id.nav_meals) {
                Intent intent = new Intent(this, MealPlannerActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                startActivity(intent);
                overridePendingTransition(0, 0); return true;
            } else if (id == R.id.nav_camera) {
                return true;
            } else if (id == R.id.nav_profile) {
                Intent intent = new Intent(this, ProfileActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                startActivity(intent);
                overridePendingTransition(0, 0); return true;
            }
            return false;
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (bottomNav != null) {
            bottomNav.setSelectedItemId(R.id.nav_camera);
        }
    }

    private void uploadPhoto(Bitmap bitmap) {
        String uid = FirebaseHelper.getInstance().getCurrentUserUid();
        if (uid == null) return;

        long timestamp = System.currentTimeMillis();
        String date = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());

        Toast.makeText(this, "Optimizing and saving photo...", Toast.LENGTH_SHORT).show();

        // Standard Firestore backup with premium hybrid storage to avoid Storage issues
        String base64Image = ImageUtils.bitmapToBase64(bitmap, 480); // 480px is perfectly optimized and sharp

        ProgressPhotoItem item = new ProgressPhotoItem(base64Image, date, timestamp);

        FirebaseHelper.getInstance().getUsersCollection()
            .document(uid)
            .collection("progress_photos")
            .add(item)
            .addOnSuccessListener(doc -> Toast.makeText(ProgressPhotoActivity.this, "Photo uploaded successfully!", Toast.LENGTH_SHORT).show())
            .addOnFailureListener(e -> Toast.makeText(ProgressPhotoActivity.this, "Save failed: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    private void fetchPhotos() {
        String uid = FirebaseHelper.getInstance().getCurrentUserUid();
        if (uid == null) return;

        FirebaseHelper.getInstance().getUsersCollection()
            .document(uid)
            .collection("progress_photos")
            .orderBy("timestamp", com.google.firebase.firestore.Query.Direction.DESCENDING)
            .addSnapshotListener((snapshot, e) -> {
                if (e != null || snapshot == null) return;
                photoList.clear();
                for (DocumentSnapshot doc : snapshot.getDocuments()) {
                    ProgressPhotoItem item = doc.toObject(ProgressPhotoItem.class);
                    if (item != null) {
                        item.setDocumentId(doc.getId());
                        photoList.add(item);
                    }
                }
                adapter.notifyDataSetChanged();
            });
    }

    private void openFullScreenImage(ProgressPhotoItem item) {
        Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_full_screen_photo);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);

        ImageView ivFullPhoto = dialog.findViewById(R.id.ivFullPhoto);
        TextView tvPhotoDate = dialog.findViewById(R.id.tvPhotoDate);
        ImageView btnClose = dialog.findViewById(R.id.btnClose);
        Button btnDelete = dialog.findViewById(R.id.btnDelete);

        tvPhotoDate.setText("Logged Date: " + item.getDate());

        // Load image dynamically (supports URL + Base64 fallback)
        String url = item.getPhotoUrl();
        if (url != null && (url.startsWith("data:image") || !url.startsWith("http"))) {
            Bitmap bmp = ImageUtils.base64ToBitmap(url);
            if (bmp != null) {
                ivFullPhoto.setImageBitmap(bmp);
            }
        } else {
            Glide.with(this).load(url).into(ivFullPhoto);
        }

        btnClose.setOnClickListener(v -> dialog.dismiss());

        btnDelete.setOnClickListener(v -> {
            new AlertDialog.Builder(this)
                .setTitle("Delete Photo")
                .setMessage("Are you sure you want to delete this progress photo?")
                .setPositiveButton("Delete", (d, w) -> {
                    String uid = FirebaseHelper.getInstance().getCurrentUserUid();
                    if (uid != null && item.getDocumentId() != null) {
                        FirebaseHelper.getInstance().getUsersCollection()
                            .document(uid)
                            .collection("progress_photos")
                            .document(item.getDocumentId())
                            .delete()
                            .addOnSuccessListener(aVoid -> {
                                Toast.makeText(this, "Photo deleted successfully", Toast.LENGTH_SHORT).show();
                                dialog.dismiss();
                            });
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
        });

        dialog.show();
    }

    // Dynamic item model mapping
    public static class ProgressPhotoItem {
        private String photoUrl;
        private String date;
        private long timestamp;
        private String documentId;

        public ProgressPhotoItem() {}

        public ProgressPhotoItem(String photoUrl, String date, long timestamp) {
            this.photoUrl = photoUrl;
            this.date = date;
            this.timestamp = timestamp;
        }

        public String getPhotoUrl() { return photoUrl; }
        public void setPhotoUrl(String photoUrl) { this.photoUrl = photoUrl; }
        public String getDate() { return date; }
        public void setDate(String date) { this.date = date; }
        public long getTimestamp() { return timestamp; }
        public void setTimestamp(long timestamp) { this.timestamp = timestamp; }
        public String getDocumentId() { return documentId; }
        public void setDocumentId(String documentId) { this.documentId = documentId; }
    }

    class GalleryAdapter extends RecyclerView.Adapter<GalleryAdapter.ViewHolder> {
        private List<ProgressPhotoItem> photos;
        GalleryAdapter(List<ProgressPhotoItem> photos) { this.photos = photos; }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_gallery_photo, parent, false);
            return new ViewHolder(v);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            ProgressPhotoItem item = photos.get(position);
            String url = item.getPhotoUrl();

            // Load dynamically from Base64 or URL
            if (url != null && (url.startsWith("data:image") || !url.startsWith("http"))) {
                Bitmap bmp = ImageUtils.base64ToBitmap(url);
                if (bmp != null) {
                    holder.ivPhoto.setImageBitmap(bmp);
                } else {
                    holder.ivPhoto.setImageResource(R.drawable.pp_1);
                }
            } else {
                Glide.with(ProgressPhotoActivity.this).load(url).into(holder.ivPhoto);
            }

            holder.itemView.setOnClickListener(v -> openFullScreenImage(item));
        }

        @Override public int getItemCount() { return photos.size(); }

        class ViewHolder extends RecyclerView.ViewHolder {
            ImageView ivPhoto;
            ViewHolder(View v) {
                super(v);
                ivPhoto = v.findViewById(R.id.ivPhoto);
            }
        }
    }
}
