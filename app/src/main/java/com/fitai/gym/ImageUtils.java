/*
 * ImageUtils contains utility functions for converting images, Base64 strings, and handling bitmap adjustments.
 */
package com.fitai.gym;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;
import java.io.ByteArrayOutputStream;

public class ImageUtils {

    public static String bitmapToBase64(Bitmap bitmap) {
        return bitmapToBase64(bitmap, 200);
    }

    public static String bitmapToBase64(Bitmap bitmap, int size) {
        if (bitmap == null) return null;
        
        
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        float ratio = (float) width / (float) height;
        int newWidth = size;
        int newHeight = size;
        if (width > height) {
            newHeight = (int) (size / ratio);
        } else {
            newWidth = (int) (size * ratio);
        }
        
        Bitmap resized = Bitmap.createScaledBitmap(bitmap, newWidth, newHeight, true);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        resized.compress(Bitmap.CompressFormat.JPEG, 75, baos); 
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
