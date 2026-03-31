package com.fitai.gym;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;
import java.io.ByteArrayOutputStream;

public class ImageUtils {

    public static String bitmapToBase64(Bitmap bitmap) {
        if (bitmap == null) return null;
        
        // Resize to absolute minimum for Firestore (200x200 is plenty for profile pic)
        Bitmap resized = Bitmap.createScaledBitmap(bitmap, 200, 200, true);
        
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        resized.compress(Bitmap.CompressFormat.JPEG, 70, baos); // 70% quality
        byte[] b = baos.toByteArray();
        return Base64.encodeToString(b, Base64.NO_WRAP);
    }

    public static Bitmap base64ToBitmap(String base64Str) {
        if (base64Str == null || base64Str.isEmpty()) return null;
        try {
            byte[] decodedBytes = Base64.decode(base64Str, Base64.NO_WRAP);
            return BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.length);
        } catch (Exception e) {
            return null;
        }
    }
}
