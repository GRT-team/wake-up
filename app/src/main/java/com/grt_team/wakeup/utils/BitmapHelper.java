
package com.grt_team.wakeup.utils;

import java.io.File;
import java.io.FileNotFoundException;

import android.content.ContentResolver;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapFactory.Options;
import android.net.Uri;
import android.util.Log;

public class BitmapHelper {

    private final static String TAG = "BitmapHelper";

    private BitmapHelper() {
    }

    /**
     * Load bitmap from specified path. If bitmap width or height are bigger
     * then specified then bitmap will be scaled
     * 
     * @param path - poster path
     * @param maxWidth - maximum required width
     * @param maxHeight - maximum required height
     * @return loaded scaled bitmap
     */
    public static Bitmap loadScaledBitmap(String path, int maxWidth, int maxHeight, Options options) {
        File file = new File(path);
        if (!file.exists()) {
            Log.w(TAG, "File not found: " + path);
            return null;
        }

        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(path, options);
        options.inSampleSize = calculateInSampleSize(options, maxWidth, maxHeight);
        options.inJustDecodeBounds = false;
        try {
            return BitmapFactory.decodeFile(path, options);
        } catch (OutOfMemoryError e) {
            Log.e(TAG, "Out of memory");
            return null;
        }
    }

    public static Bitmap loadScaledBitmap(ContentResolver resolver, Uri uri, int maxWidth,
            int maxHeight, Options options) {

        options.inSampleSize = calculateInSampleSize(options, maxWidth, maxHeight);
        options.inJustDecodeBounds = false;
        try {
            return BitmapFactory.decodeStream(resolver.openInputStream(uri), null, options);
        } catch (OutOfMemoryError e) {
            Log.e(TAG, "Out of memory");
        } catch (FileNotFoundException e) {
            Log.e(TAG, "File not found: " + uri);
        }
        return null;
    }

    /**
     * Calculate scale ratio for image to save image proportion and fit maximum
     * width and height
     * 
     * @param options - {@link android.graphics.BitmapFactory.Options} options
     *            for loading image. Used to get image width and height.
     * @param maxWidth - maximum required width
     * @param maxHeight - maximum required height
     * @return the scale ratio for image
     */
    private static int calculateInSampleSize(Options options, int maxWidth, int maxHeight) {
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > maxHeight || width > maxWidth) {
            if (width > height) {
                inSampleSize = Math.round((float) height / (float) maxHeight);
            } else {
                inSampleSize = Math.round((float) width / (float) maxWidth);
            }
        }
        return inSampleSize;
    }

}
