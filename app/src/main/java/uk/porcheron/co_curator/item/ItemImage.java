package uk.porcheron.co_curator.item;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

import java.io.FileInputStream;

import uk.porcheron.co_curator.ImageDialogActivity;
import uk.porcheron.co_curator.R;
import uk.porcheron.co_curator.TimelineActivity;
import uk.porcheron.co_curator.user.User;
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

    public ItemImage(int itemId, User user, String dateTime) {
        super(user, itemId, dateTime);

        mActivity = TimelineActivity.getInstance();

        setBounds(Style.imageWidth, Style.imageHeight, Style.imagePadding);
    }

    @Override
    public void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        RectF b = getInnerBounds();

        if (mBitmap == null) {
            Log.d(TAG, "Load image file " + mImagePath);
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

    public void setImagePath(String imagePath) {
        mImagePath = imagePath;
    }

    @Override
    public boolean onTap() {
        Log.d(TAG, "Image clicked!");

        Intent intent = new Intent(TimelineActivity.getInstance(), ImageDialogActivity.class);
        intent.putExtra(ImageDialogActivity.IMAGE, mImagePath);

        mActivity.startActivity(intent);
        mActivity.overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
        return true;
    }

}
