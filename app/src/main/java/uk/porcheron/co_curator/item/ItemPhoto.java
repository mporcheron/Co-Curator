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

import java.io.FileInputStream;
import java.io.IOException;

import uk.porcheron.co_curator.ImageDialogActivity;
import uk.porcheron.co_curator.R;
import uk.porcheron.co_curator.TimelineActivity;
import uk.porcheron.co_curator.user.User;
import uk.porcheron.co_curator.util.Image;
import uk.porcheron.co_curator.val.Instance;
import uk.porcheron.co_curator.val.Style;

/**
 * An item that contains text.
 */
public class ItemPhoto extends Item {
    private static final String TAG = "CC:ItemImage";

    private TimelineActivity mActivity;

    private Bitmap mBitmap = null;
    private Bitmap mBitmapThumbnail = null;
    private String mImagePath;

    public ItemPhoto(Context context) { super(context); }

    public ItemPhoto(Context context, AttributeSet attrs) { super(context, attrs); }

    public ItemPhoto(Context context, AttributeSet attrs, int defStyle) { super(context, attrs, defStyle); }

    public ItemPhoto(int itemId, User user, int dateTime) {
        super(user, itemId, dateTime);

        mActivity = TimelineActivity.getInstance();

        setBounds(Style.photoWidth, Style.photoHeight, Style.photoPadding);
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
                Log.e(TAG, "Could not open " + mImagePath + "-thumb");
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
        mImagePath = imagePath;
        mBitmap = null;
        mBitmapThumbnail = null;
        return imagePath;
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


    public static int getThumbnailWidth() {
        return (int) (Style.photoWidth - (2 * Style.photoPadding));
    }

    public static int getThumbnailHeight() {
        return (int) (Style.photoHeight - (2 * Style.photoPadding));
    }

}
