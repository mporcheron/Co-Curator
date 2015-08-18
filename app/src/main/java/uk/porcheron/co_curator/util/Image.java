package uk.porcheron.co_curator.util;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.Log;

import java.io.FileOutputStream;
import java.io.IOException;

import uk.porcheron.co_curator.val.Instance;
import uk.porcheron.co_curator.val.Phone;
import uk.porcheron.co_curator.val.Style;

/**
 * Utilities for handling images within the application.
 */
public class Image {
    private static final String TAG = "CC:Image";

    public static String save(Context context, Bitmap bitmap) throws IOException, IllegalArgumentException {
        String filename = Instance.globalUserId + "-" + Instance.userId + "-" + System.currentTimeMillis();

        int width = bitmap.getWidth();
        int height = bitmap.getHeight();

        // General Bitmap
        int imageWidth = Phone.screenWidth;
        int imageHeight = Phone.screenHeight;

        float thumbScale = imageWidth / (float) width;
        if(height * thumbScale < imageHeight) {
            thumbScale = imageHeight / (float) height;
        }

        int scaleWidth = (int) (width * thumbScale);
        int scaleHeight = (int) (height * thumbScale);

        Log.d(TAG, "Scaled Image is is (" + scaleWidth + "," + scaleHeight + ")");
        bitmap = Bitmap.createScaledBitmap(bitmap, scaleWidth, scaleHeight, true);


        // Thumbnail Generation
        int thumbWidth = (int) (Style.imageWidth - 2 * Style.imagePadding);
        int thumbHeight = (int) (Style.imageHeight - 2 * Style.imagePadding);

        Log.d(TAG, "Image is (" + width + "," + height + ")");

        thumbScale = thumbWidth / (float) width;
        if(height * thumbScale < thumbHeight) {
            thumbScale = thumbHeight / (float) height;
        }

        scaleWidth = (int) (width * thumbScale);
        scaleHeight = (int) (height * thumbScale);

        Log.d(TAG, "Scaled Image is is (" + scaleWidth + "," + scaleHeight + ")");
        Bitmap scaledBitmap = Bitmap.createScaledBitmap(bitmap, scaleWidth, scaleHeight, true);

        int x = (int) ((scaleWidth / 2f) - (thumbWidth / 2f));
        int y = (int) ((scaleHeight / 2f) - (thumbHeight / 2f));

        Log.d(TAG, "Thumbnail Image is is (" + thumbWidth + "," + thumbHeight + ")");
        Bitmap thumbnail = Bitmap.createBitmap(scaledBitmap, x, y, thumbWidth, thumbHeight);

        final FileOutputStream fos = context.openFileOutput(filename + ".png", Context.MODE_PRIVATE);
        if (!bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos)) {
            Log.e(TAG, "Could not save bitmap locally");
        }
        fos.close();

        final FileOutputStream fosThumb = context.openFileOutput(filename + "-thumb.png", Context.MODE_PRIVATE);
        if (!thumbnail.compress(Bitmap.CompressFormat.PNG, 100, fosThumb)) {
            Log.e(TAG, "Could not save bitmap locally");
        }
        fosThumb.close();

        return filename;
    }

}
