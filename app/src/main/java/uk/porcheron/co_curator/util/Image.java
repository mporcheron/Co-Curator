package uk.porcheron.co_curator.util;

import android.content.Context;
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
import uk.porcheron.co_curator.item.ItemPhoto;
import uk.porcheron.co_curator.item.ItemURL;
import uk.porcheron.co_curator.val.Instance;
import uk.porcheron.co_curator.val.Phone;
import uk.porcheron.co_curator.val.Style;

/**
 * Utilities for handling images within the application.
 */
public class Image {
    private static final String TAG = "CC:Image";

    public interface OnCompleteRunner {
        void run(String fileName);
    }
    
    public static Bitmap getBitmapFromURL(String src) {
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

    public static Bitmap save(Context context, Bitmap bitmap, String filename) throws IOException, IllegalArgumentException {
        final FileOutputStream fos = context.openFileOutput(filename + ".png", Context.MODE_PRIVATE);
        if (!bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos)) {
            Log.e(TAG, "Could not save bitmap locally");
        }
        fos.close();

        return bitmap;
    }

    public static Bitmap save(Context context, Bitmap bitmap, String filename, int finalWidth, int finalHeight, boolean crop) throws IOException, IllegalArgumentException {
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
//
//    public static String save(Context context, Bitmap bitmap) throws IOException, IllegalArgumentException {
//        String filename = Instance.globalUserId + "-" + Instance.userId + "-" + System.currentTimeMillis();
//
//        int width = bitmap.getWidth();
//        int height = bitmap.getHeight();
//
//        // General Bitmap
//        int photoWidth = Phone.screenWidth;
//        int photoHeight = Phone.screenHeight;
//
//        float thumbScale = photoWidth / (float) width;
//        if(height * thumbScale < photoHeight) {
//            thumbScale = photoHeight / (float) height;
//        }
//
//        int scaleWidth = (int) (width * thumbScale);
//        int scaleHeight = (int) (height * thumbScale);
//
//        Log.d(TAG, "Scaled Image is is (" + scaleWidth + "," + scaleHeight + ")");
//        bitmap = Bitmap.createScaledBitmap(bitmap, scaleWidth, scaleHeight, true);
//
//
//        // Thumbnail Generation
//        int thumbWidth = (int) (Style.photoWidth - 2 * Style.photoPadding);
//        int thumbHeight = (int) (Style.photoHeight - 2 * Style.photoPadding);
//
//        Log.d(TAG, "Image is (" + width + "," + height + ")");
//
//        thumbScale = thumbWidth / (float) width;
//        if(height * thumbScale < thumbHeight) {
//            thumbScale = thumbHeight / (float) height;
//        }
//
//        scaleWidth = (int) (width * thumbScale);
//        scaleHeight = (int) (height * thumbScale);
//
//        Log.d(TAG, "Scaled Image is is (" + scaleWidth + "," + scaleHeight + ")");
//        Bitmap scaledBitmap = Bitmap.createScaledBitmap(bitmap, scaleWidth, scaleHeight, true);
//
//        int x = (int) ((scaleWidth / 2f) - (thumbWidth / 2f));
//        int y = (int) ((scaleHeight / 2f) - (thumbHeight / 2f));
//
//        Log.d(TAG, "Thumbnail Image is is (" + thumbWidth + "," + thumbHeight + ")");
//        Bitmap thumbnail = Bitmap.createBitmap(scaledBitmap, x, y, thumbWidth, thumbHeight);
//
//        final FileOutputStream fos = context.openFileOutput(filename + ".png", Context.MODE_PRIVATE);
//        if (!bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos)) {
//            Log.e(TAG, "Could not save bitmap locally");
//        }
//        fos.close();
//
//        final FileOutputStream fosThumb = context.openFileOutput(filename + "-thumb.png", Context.MODE_PRIVATE);
//        if (!thumbnail.compress(Bitmap.CompressFormat.PNG, 100, fosThumb)) {
//            Log.e(TAG, "Could not save bitmap locally");
//        }
//        fosThumb.close();
//
//        return filename;
//    }

    public static synchronized String fileToFile(String file, OnCompleteRunner onCompleteRunner) {
        String filename = Instance.globalUserId + "-" + System.currentTimeMillis();

        Bitmap b =  BitmapFactory.decodeFile(file);

        int imageWidth = (int) (Style.photoWidth - (2 * Style.photoPadding));
        int imageHeight = (int) (Style.photoHeight - (2 * Style.photoPadding));

        return bitmapToFile(b, filename, imageWidth, imageHeight, Instance.globalUserId, onCompleteRunner);
    }

    public static synchronized String urlToFile(String url, int globalUserId, OnCompleteRunner onCompleteRunner) {
        int imageWidth = (int) (Style.photoWidth - (2 * Style.photoPadding));
        int imageHeight = (int) (Style.photoHeight - (2 * Style.photoPadding));

        return urlToFile(url, globalUserId, imageWidth, imageHeight, onCompleteRunner);
    }

    public static synchronized String urlToFile(String url, int globalUserId, int imageWidth, int imageHeight, OnCompleteRunner onCompleteRunner) {
        String filename = globalUserId + "-" + System.currentTimeMillis();

        Log.d(TAG, "Download " + url + " and save as " + filename);

        new UrlToFile(url, filename, globalUserId, imageWidth, imageHeight, onCompleteRunner).execute();
        return filename;
    }

    public final static synchronized String bitmapToFile(Bitmap bitmap, String filename, int imageWidth, int imageHeight, int globalUserId, final OnCompleteRunner onCompleteRunner) {

        try {
            Image.save(TimelineActivity.getInstance(), bitmap, filename);
            new ScaleImage(bitmap, filename, globalUserId, imageWidth, imageHeight, true, new OnCompleteRunner() {

                @Override
                public void run(String result) {
                    if(onCompleteRunner != null) {
                        onCompleteRunner.run(result);
                    }
                }
            }).execute();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    private static class UrlToFile extends AsyncTask<Void,Void,Bitmap> {

        private String mUrl;
        private String mFilename;
        private int mGlobalUserId;
        private int mImageWidth;
        private int mImageHeight;
        private OnCompleteRunner mOnCompleteRunner = null;

        UrlToFile(String url, String filename, int globalUserId, int imageWidth, int imageHeight, OnCompleteRunner onCompleteRunner) {
            mUrl = url;
            mGlobalUserId = globalUserId;
            mFilename = filename;
            mImageWidth = imageWidth;
            mImageHeight = imageHeight;
            mOnCompleteRunner = onCompleteRunner;
        }

        @Override
        protected Bitmap doInBackground(Void... params) {
            return Image.getBitmapFromURL(mUrl);
        }

        @Override
        protected void onPostExecute(Bitmap bitmap) {
            bitmapToFile(bitmap, mFilename, mImageWidth, mImageHeight, mGlobalUserId, mOnCompleteRunner);
        }
    }

    private static class ScaleImage extends AsyncTask<Void,Void,String> {

        private TimelineActivity mActivity;
        private Bitmap mBitmap;
        private String mFilename;
        private int mGlobalUserId;
        private OnCompleteRunner mOnComplete;
        private int mImageWidth;
        private int mImageHeight;
        private boolean mCrop;

        ScaleImage(Bitmap bitmap, String filename, int globalUserId, int imageWidth, int imageHeight, boolean crop, OnCompleteRunner onComplete) {
            mActivity = TimelineActivity.getInstance();
            mBitmap = bitmap;
            mFilename = filename;
            mGlobalUserId = globalUserId;
            mOnComplete = onComplete;
            mImageWidth = imageWidth;
            mImageHeight = imageHeight;
            mCrop = crop;
        }

        @Override
        protected String doInBackground(Void... params) {
            try {
                //Bitmap scaledBitmap = Image.save(mActivity, mBitmap, mFilename, Phone.screenWidth, Phone.screenHeight, false);
                Image.save(mActivity, mBitmap, mFilename + "-thumb", mImageWidth, mImageHeight, mCrop);
                return mFilename;
            } catch (IOException e) {
                return null;
            }
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            mOnComplete.run(result);
        }
    }
}
