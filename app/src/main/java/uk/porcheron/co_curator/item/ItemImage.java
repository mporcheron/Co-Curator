package uk.porcheron.co_curator.item;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.RectF;
import android.os.AsyncTask;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

import java.io.FileInputStream;
import java.io.IOException;

import uk.porcheron.co_curator.ImageDialogActivity;
import uk.porcheron.co_curator.R;
import uk.porcheron.co_curator.TimelineActivity;
import uk.porcheron.co_curator.user.User;
import uk.porcheron.co_curator.util.Image;
import uk.porcheron.co_curator.util.Web;
import uk.porcheron.co_curator.val.Instance;
import uk.porcheron.co_curator.val.Phone;
import uk.porcheron.co_curator.val.Style;

/**
 * An item that contains text.
 */
public class ItemImage extends Item {
    private static final String TAG = "CC:ItemImage";

    private TimelineActivity mActivity;

    private Bitmap mBitmap = null;
    private Bitmap mBitmapThumbnail = null;
    private String mImagePath;

    public ItemImage(Context context) { super(context); }

    public ItemImage(Context context, AttributeSet attrs) { super(context, attrs); }

    public ItemImage(Context context, AttributeSet attrs, int defStyle) { super(context, attrs, defStyle); }

    public ItemImage(int itemId, User user, int dateTime) {
        super(user, itemId, dateTime);

        mActivity = TimelineActivity.getInstance();

        setBounds(Style.imageWidth, Style.imageHeight, Style.imagePadding);
    }

    @Override
    public void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        RectF b = getInnerBounds();

        if (mBitmap == null) {
            Log.v(TAG, "Load image file " + mImagePath);
            try {
                FileInputStream fis = getContext().openFileInput(mImagePath + ".png");
                mBitmap = BitmapFactory.decodeStream(fis);
                fis.close();

                if (mBitmap == null) {
                    Log.e(TAG, "Could not decode file to bitmap " + mImagePath);
                }
            } catch (Exception e) {
                Log.e(TAG, "Could not open " + mImagePath);
            }


            try {
                FileInputStream fis = getContext().openFileInput(mImagePath + "-thumb.png");
                mBitmapThumbnail = BitmapFactory.decodeStream(fis);
                fis.close();

                if (mBitmapThumbnail == null) {
                    Log.e(TAG, "Could not decode thumbnail file to bitmap " + mImagePath);
                }
            } catch (Exception e) {
                Log.e(TAG, "Could not open " + mImagePath);
            }
        }

        if (mBitmap != null && mBitmapThumbnail != null) {
            canvas.drawBitmap(mBitmapThumbnail, b.left, b.top, Style.normalPaint);
        } else {
            canvas.drawRect(b, Style.normalPaint);
        }

    }

    @Override
    public String getData() { return mImagePath; }

    @Override
    public String setData(String imagePath) {
        mImagePath = imagePath; return imagePath;
    }

    @Override
    public boolean onTap() {
        Log.v(TAG, "Image clicked!");

        Intent intent = new Intent(TimelineActivity.getInstance(), ImageDialogActivity.class);
        intent.putExtra(ImageDialogActivity.IMAGE, mImagePath);

        mActivity.startActivity(intent);
        mActivity.overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
        return true;
    }

    @Override
    protected boolean onLongPress() {
        return false;
    }

    public static synchronized String fileToFile(String file) {
        String filename = Instance.globalUserId + "-" + System.currentTimeMillis();

        Bitmap b =  BitmapFactory.decodeFile(file);

        int imageWidth = (int) (Style.imageWidth - (2 * Style.imagePadding));
        int imageHeight = (int) (Style.imageHeight - (2 * Style.imagePadding));

        return bitmapToFile(b, filename, imageWidth, imageHeight, Instance.globalUserId);
    }

    public static synchronized String urlToFile(String url, int globalUserId) {
        int imageWidth = (int) (Style.imageWidth - (2 * Style.imagePadding));
        int imageHeight = (int) (Style.imageHeight - (2 * Style.imagePadding));

        return urlToFile(url, globalUserId, imageWidth, imageHeight);
    }

    public static synchronized String urlToFile(String url, int globalUserId, int imageWidth, int imageHeight) {
        String filename = globalUserId + "-" + System.currentTimeMillis();

        Log.d(TAG, "Download " + url + " and save as " + filename);

        new UrlToFile(url, filename, imageWidth, imageHeight, globalUserId).execute();
        return filename;
    }

    public final static synchronized String bitmapToFile(Bitmap bitmap, String filename, int imageWidth, int imageHeight, int globalUserId) {

        try {
            Image.save(TimelineActivity.getInstance(), bitmap, filename);
            new ScaleImage(bitmap, filename, globalUserId, imageWidth, imageHeight, true, new OnCompleteRunner() {

                @Override
                public void run(String result) {
                    if(result != null && !Instance.items.add(ItemType.PHOTO, Instance.user(), result, false, true, true)) {
                        Log.e(TAG, "Failed to save image");
                    }

                    TimelineActivity.getInstance().hideLoadingDialog();
                }
            }).execute();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    private interface OnCompleteRunner {
        void run(String result);
    }

    private static class UrlToFile extends AsyncTask<Void,Void,Bitmap> {

        private String mUrl;
        private String mFilename;
        private int mGlobalUserId;
        private int mImageWidth;
        private int mImageHeight;

        UrlToFile(String url, String filename, int globalUserId, int imageWidth, int imageHeight) {
            mUrl = url;
            mGlobalUserId = globalUserId;
            mFilename = filename;
            mImageWidth = imageWidth;
            mImageHeight = imageHeight;
        }

        @Override
        protected Bitmap doInBackground(Void... params) {
            return Image.getBitmapFromURL(mUrl);
        }

        @Override
        protected void onPostExecute(Bitmap bitmap) {
            ItemURL.bitmapToFile(bitmap, mFilename, mImageWidth, mImageHeight, mGlobalUserId);
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
