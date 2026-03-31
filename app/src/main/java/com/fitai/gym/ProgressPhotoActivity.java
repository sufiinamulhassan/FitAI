package com.fitai.gym;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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

import java.io.ByteArrayOutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ProgressPhotoActivity extends AppCompatActivity {

    private RecyclerView rvGallery;
    private List<ProgressPhoto> photoList = new ArrayList<>();
    private GalleryAdapter adapter;

    private final ActivityResultLauncher<Intent> cameraLauncher = registerForActivityResult(
        new ActivityResultContracts.StartActivityForResult(),
        result -> {
            if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                Bitmap bitmap = (Bitmap) result.getData().getExtras().get("data");
                uploadPhoto(bitmap);
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
        findViewById(R.id.fabCamera).setOnClickListener(v -> {
            Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            cameraLauncher.launch(intent);
        });

        fetchPhotos();
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
        
        StorageReference ref = FirebaseHelper.getInstance().getStorageReference()
            .child("progress_photos")
            .child(uid)
            .child(timestamp + ".jpg");

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        byte[] data = baos.toByteArray();

        Toast.makeText(this, "Uploading photo...", Toast.LENGTH_SHORT).show();

        ref.putBytes(data).addOnSuccessListener(taskSnapshot -> {
            ref.getDownloadUrl().addOnSuccessListener(uri -> {
                ProgressPhoto pp = new ProgressPhoto(uri.toString(), date, timestamp);
                FirebaseHelper.getInstance().getUsersCollection()
                    .document(uid)
                    .collection("progress_photos")
                    .add(pp)
                    .addOnSuccessListener(doc -> Toast.makeText(this, "Photo saved!", Toast.LENGTH_SHORT).show());
            });
        }).addOnFailureListener(e -> Toast.makeText(this, "Upload failed", Toast.LENGTH_SHORT).show());
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
                for (com.google.firebase.firestore.DocumentSnapshot doc : snapshot.getDocuments()) {
                    ProgressPhoto pp = doc.toObject(ProgressPhoto.class);
                    if (pp != null) photoList.add(pp);
                }
                adapter.notifyDataSetChanged();
            });
    }

    class GalleryAdapter extends RecyclerView.Adapter<GalleryAdapter.ViewHolder> {
        private List<ProgressPhoto> photos;
        GalleryAdapter(List<ProgressPhoto> photos) { this.photos = photos; }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_gallery_photo, parent, false);
            return new ViewHolder(v);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            Glide.with(ProgressPhotoActivity.this).load(photos.get(position).getPhotoUrl()).into(holder.ivPhoto);
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
