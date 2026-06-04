/*
 * ImageLoaderHelper implements asynchronous image downloading and caching for workout and food items.
 */
package com.fitai.gym;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;
import android.widget.ImageView;

public class ImageLoaderHelper {

    
    public static void loadImage(Context context, ImageView imageView, String imageKey, int fallbackResId) {
        if (imageView == null) return;

        if (imageKey != null && imageKey.startsWith("base64:")) {
            try {
                String base64Str = imageKey.substring("base64:".length());
                byte[] decodedBytes = Base64.decode(base64Str, Base64.DEFAULT);
                Bitmap bitmap = BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.length);
                if (bitmap != null) {
                    imageView.setImageBitmap(bitmap);
                    return;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        
        int resId = fallbackResId;
        if (imageKey != null && !imageKey.isEmpty() && !imageKey.startsWith("base64:")) {
            int identifier = context.getResources().getIdentifier(imageKey.toLowerCase(), "drawable", context.getPackageName());
            if (identifier != 0) {
                resId = identifier;
            }
        }
        imageView.setImageResource(resId);
    }
}
