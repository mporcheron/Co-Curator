package uk.porcheron.co_curator.util;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.util.Log;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import uk.porcheron.co_curator.TimelineActivity;
import uk.porcheron.co_curator.val.Instance;
import uk.porcheron.co_curator.val.Phone;

/**
 * Utilities for handling images within the application.
 */
public class Image {
    private static final String TAG = "CC:Image";

    public interface OnCompleteRunner {
        void run(String filename);
    }

    public static void url2File(final String url, final String destination, final int thumbWidth, final int thumbHeight, final Runnable onCompleteRunner) {
        Log.d(TAG, "Download " + url + " and save as " + destination);

        new Thread(new Runnable() {

            @Override
            public void run() {
                TimelineActivity activity = TimelineActivity.getInstance();

                try {
                    // Download and save the bitmap
                    Bitmap bitmap = Image.getBitmapFromURL(url);
                    Image.save(activity, bitmap, destination);

                    // Thumbnail
                    Image.save(activity, bitmap, destination + "-thumb", thumbWidth, thumbHeight, true);

                    // On Complete…
                    if(onCompleteRunner != null) {
                        activity.runOnUiThread(onCompleteRunner);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }

            }
        }).start();
    }

    public static void file2file(final String source, final String destination, final int thumbWidth, final int thumbHeight, final Runnable onCompleteRunner) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                TimelineActivity activity = TimelineActivity.getInstance();

                try {
                    // Import the photo for this phone
                    Bitmap bitmap = decodeSampledBitmapFromResource(source, Phone.screenWidth, Phone.screenHeight);
                    Image.save(activity, bitmap, destination);

                    // Thumbnail
                    Image.save(activity, bitmap, destination + "-thumb", thumbWidth, thumbHeight, true);

                    // On Complete…
                    if(onCompleteRunner != null) {
                        activity.runOnUiThread(onCompleteRunner);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    /////////////////////////////////////////////////////////////////
    // Below here are helper methods; they do NOT handle threading //
    /////////////////////////////////////////////////////////////////

    private static Bitmap getBitmapFromURL(String src) {
        try {
            Log.d(TAG, "Download image from " + src);
            HttpURLConnection connection =
                    (HttpURLConnection) (new URL(src)).openConnection();
            connection.setDoInput(true);
            connection.connect();

            InputStream input = connection.getInputStream();
            return BitmapFactory.decodeStream(input);
        } catch (IOException e) {
            Log.e(TAG, "Failed to get image from URL: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    private static Bitmap save(Context context, Bitmap bitmap, String filename) throws IOException, IllegalArgumentException {
        final FileOutputStream fos = context.openFileOutput(filename + ".png", Context.MODE_PRIVATE);
        if (!bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos)) {
            Log.e(TAG, "Could not save bitmap locally");
        }
        fos.close();

        return bitmap;
    }

    private static Bitmap save(Context context, Bitmap bitmap, String filename, int finalWidth, int finalHeight, boolean crop) throws IOException, IllegalArgumentException {
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        Log.d(TAG, "Image is (" + width + "," + height + ")");

        float thumbScale = finalWidth / (float) width;
        if(height * thumbScale < finalHeight) {
            thumbScale = finalHeight / (float) height;
        }

        int scaleWidth = (int) (width * thumbScale);
        int scaleHeight = (int) (height * thumbScale);

        Log.d(TAG, "Scaled Image is is (" + scaleWidth + "," + scaleHeight + ")");
        bitmap = Bitmap.createScaledBitmap(bitmap, scaleWidth, scaleHeight, true);

        if(crop) {
            int x = (int) ((scaleWidth / 2f) - (finalWidth / 2f));
            int y = (int) ((scaleHeight / 2f) - (finalHeight / 2f));

            Log.d(TAG, "Thumbnail Image is is (" + finalWidth + "," + finalHeight + ")");
            bitmap = Bitmap.createBitmap(bitmap, x, y, finalWidth, finalHeight);
        }

        final FileOutputStream fos = context.openFileOutput(filename + ".png", Context.MODE_PRIVATE);
        if (!bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos)) {
            Log.e(TAG, "Could not save bitmap locally");
        }
        fos.close();

        return bitmap;
    }

    private static int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
        // Raw height and width of image
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {

            final int halfHeight = height / 2;
            final int halfWidth = width / 2;

            // Calculate the largest inSampleSize value that is a power of 2 and keeps both
            // height and width larger than the requested height and width.
            while ((halfHeight / inSampleSize) > reqHeight
                    && (halfWidth / inSampleSize) > reqWidth) {
                inSampleSize *= 2;
            }
        }

        return inSampleSize;
    }

    private static Bitmap decodeSampledBitmapFromResource(String file, int reqWidth, int reqHeight) {
        // First decode with inJustDecodeBounds=true to check dimensions
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(file, options);

        // Calculate inSampleSize
        options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);

        // Decode bitmap with inSampleSize set
        options.inJustDecodeBounds = false;
        return BitmapFactory.decodeFile(file, options);
    }

}
