/*
 * ProgressActivity displays visual body progress charts, BMI categories, and weight tracking history.
 */
package com.fitai.gym;

import android.os.Bundle;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.LinearLayout;
import android.widget.ImageView;
import android.view.ViewGroup;
import android.util.TypedValue;
import android.content.Intent;
import androidx.appcompat.app.AppCompatActivity;
import com.bumptech.glide.Glide;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.Query;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class ProgressActivity extends AppCompatActivity {

    private ProgressBar pbMon, pbTue, pbWed, pbThu, pbFri, pbSat, pbSun;
    private TextView tvTotalCal, tvTotalMins;
    private LinearLayout llProgressPhotos;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_progress);
        findViewById(R.id.ivBack).setOnClickListener(v -> finish());

        pbMon = findViewById(R.id.pbMon);
        pbTue = findViewById(R.id.pbTue);
        pbWed = findViewById(R.id.pbWed);
        pbThu = findViewById(R.id.pbThu);
        pbFri = findViewById(R.id.pbFri);
        pbSat = findViewById(R.id.pbSat);
        pbSun = findViewById(R.id.pbSun);
        tvTotalCal = findViewById(R.id.tvTotalCal);
        tvTotalMins = findViewById(R.id.tvTotalMins);
        llProgressPhotos = findViewById(R.id.llProgressPhotos);

        findViewById(R.id.tvSeeMorePhotos).setOnClickListener(v -> {
            startActivity(new Intent(this, ProgressPhotoActivity.class));
        });

        loadWeeklyProgress();
        loadProgressPhotos();
    }

    private void loadWeeklyProgress() {
        FirebaseHelper fb = FirebaseHelper.getInstance();
        String uid = fb.getCurrentUserUid();
        if (uid == null) return;

        
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        Timestamp weekStart = new Timestamp(cal.getTime());

        fb.getUsersCollection().document(uid).collection("workout_history")
            .whereGreaterThanOrEqualTo("completedAt", weekStart)
            .orderBy("completedAt", Query.Direction.ASCENDING)
            .get()
            .addOnSuccessListener(snapshot -> {
                
                int[] dayCals = new int[7]; 
                int totalCal = 0;
                int totalSec = 0;

                SimpleDateFormat dayFmt = new SimpleDateFormat("EEEE", Locale.getDefault());

                for (DocumentSnapshot doc : snapshot.getDocuments()) {
                    Timestamp ts = doc.getTimestamp("completedAt");
                    Long docCal = doc.getLong("caloriesBurned");
                    Long docTime = doc.getLong("totalTimeSeconds");

                    if (ts == null) continue;

                    String dayName = dayFmt.format(ts.toDate()).toLowerCase();
                    int c = docCal != null ? docCal.intValue() : 0;
                    int t = docTime != null ? docTime.intValue() : 0;
                    totalCal += c;
                    totalSec += t;

                    switch (dayName) {
                        case "monday":    dayCals[0] += c; break;
                        case "tuesday":   dayCals[1] += c; break;
                        case "wednesday": dayCals[2] += c; break;
                        case "thursday":  dayCals[3] += c; break;
                        case "friday":    dayCals[4] += c; break;
                        case "saturday":  dayCals[5] += c; break;
                        case "sunday":    dayCals[6] += c; break;
                    }
                }

                
                int maxCal = 500;
                for (int d : dayCals) if (d > maxCal) maxCal = d;

                ProgressBar[] bars = {pbMon, pbTue, pbWed, pbThu, pbFri, pbSat, pbSun};
                for (int i = 0; i < 7; i++) {
                    if (bars[i] != null) {
                        int pct = maxCal > 0 ? Math.min((dayCals[i] * 100) / maxCal, 100) : 0;
                        bars[i].setProgress(pct);
                    }
                }

                if (tvTotalCal != null) tvTotalCal.setText(String.format(Locale.getDefault(), "%,d", totalCal));
                if (tvTotalMins != null) tvTotalMins.setText(String.valueOf(totalSec / 60));
            });
    }

    private void loadProgressPhotos() {
        FirebaseHelper fb = FirebaseHelper.getInstance();
        String uid = fb.getCurrentUserUid();
        if (uid == null) return;

        fb.getUsersCollection().document(uid).collection("progress_photos")
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .limit(5)
            .get()
            .addOnSuccessListener(snapshot -> {
                llProgressPhotos.removeAllViews();
                
                if (snapshot.isEmpty()) {
                    
                    TextView tvEmpty = new TextView(this);
                    tvEmpty.setText("No photos uploaded yet. Tap 'See more' to add some!");
                    tvEmpty.setTextColor(android.graphics.Color.GRAY);
                    tvEmpty.setTextSize(12);
                    tvEmpty.setPadding(0, 20, 0, 20);
                    llProgressPhotos.addView(tvEmpty);
                    return;
                }

                int dp100 = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 100, getResources().getDisplayMetrics());
                int dp120 = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 120, getResources().getDisplayMetrics());
                int dp12 = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 12, getResources().getDisplayMetrics());

                for (DocumentSnapshot doc : snapshot.getDocuments()) {
                    String photoUrl = doc.getString("photoUrl");
                    if (photoUrl == null || photoUrl.isEmpty()) continue;

                    ImageView iv = new ImageView(this);
                    LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(dp100, dp120);
                    params.setMarginEnd(dp12);
                    iv.setLayoutParams(params);
                    iv.setScaleType(ImageView.ScaleType.CENTER_CROP);
                    
                    
                    iv.setOutlineProvider(new android.view.ViewOutlineProvider() {
                        @Override
                        public void getOutline(android.view.View view, android.graphics.Outline outline) {
                            outline.setRoundRect(0, 0, view.getWidth(), view.getHeight(), dp12);
                        }
                    });
                    iv.setClipToOutline(true);
                    
                    if (photoUrl.startsWith("http")) {
                        Glide.with(this).load(photoUrl).into(iv);
                    } else if (photoUrl.startsWith("base64:")) {
                        android.graphics.Bitmap bitmap = ImageUtils.base64ToBitmap(photoUrl);
                        if (bitmap != null) {
                            iv.setImageBitmap(bitmap);
                        }
                    } else {
                        
                        android.graphics.Bitmap bitmap = ImageUtils.base64ToBitmap(photoUrl);
                        if (bitmap != null) {
                            iv.setImageBitmap(bitmap);
                        }
                    }

                    llProgressPhotos.addView(iv);
                }
            });
    }
}
