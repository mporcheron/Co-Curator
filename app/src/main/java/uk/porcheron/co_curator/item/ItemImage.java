package uk.porcheron.co_curator.item;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.text.TextPaint;
import android.util.Log;
import android.view.ViewGroup;
import android.widget.TextView;

import java.io.FileInputStream;

import uk.porcheron.co_curator.user.User;
import uk.porcheron.co_curator.util.Style;

/**
 * An item that contains text.
 * <p/>
 * Created by map on 06/08/15.
 */
public class ItemImage extends Item {
    private static final String TAG = "CC:ItemImage";

    private float mImageLeft;
    private float mImageTop;
    private float mImageRight;
    private float mImageBottom;

    private float mShadowLeft;
    private float mShadowTop;
    private float mShadowRight;
    private float mShadowBottom;

    private Bitmap mBitmap = null;
    private Bitmap mBitmapThumbnail = null;
    private String mImagePath;

    private Paint mPaintBg;
    private Paint mPaintSh;
    private TextPaint mPaintFg;
    private TextView mTextView;

    public ItemImage(Context context, ViewGroup vg, int itemId, User user) {
        super(context, user, itemId);

        mPaintBg = new Paint(Paint.ANTI_ALIAS_FLAG);
        mPaintBg.setStyle(Paint.Style.FILL);
        mPaintSh = new Paint(Paint.ANTI_ALIAS_FLAG);
        mPaintSh.setStyle(Paint.Style.FILL);
        mPaintFg = new TextPaint(Paint.ANTI_ALIAS_FLAG);

        setNoteBackgroundColor(Style.noteBg);
        setShadowBackgroundColor(Style.noteSh);
        setTextStyle(Style.noteFg, Style.noteFontSize);

        RectF b = getBounds();
        mImageLeft = b.left;
        mImageTop = b.top;
        mImageRight = b.right - Style.noteShadowSize;
        mImageBottom = b.bottom - Style.noteShadowSize;

        mShadowLeft = Style.noteShadowOffset;
        mShadowTop = Style.noteShadowOffset;
        mShadowRight = b.right;
        mShadowBottom = b.bottom;
    }

    @Override
    public void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        if(mBitmap == null) {
            try{
                Log.v(TAG, "Creating bitmap from image path");
                FileInputStream fis = getContext().openFileInput(mImagePath);
                mBitmap = BitmapFactory.decodeStream(fis);
                fis.close();
                Log.v(TAG, "Created bitmap from image path");
            }
            catch(Exception e){
                Log.e(TAG, "Could not open " + mImagePath);
            }
        }


        if(mBitmapThumbnail == null) {
            Log.d(TAG, "Creating image thumbnail");

            float ratio = (float) mBitmap.getHeight() / (float) mBitmap.getWidth() ;//, mBitmap.getHeight()/mBitmap.getWidth());
            int width = (int) mImageRight;
            int height = (int) (mImageBottom * ratio);

            mBitmapThumbnail = Bitmap.createScaledBitmap(mBitmap, width, height,                    false);

            mShadowRight = mShadowLeft + width + Style.noteShadowOffset;
            mShadowBottom = mShadowTop + height + Style.noteShadowOffset;
        }

        canvas.drawRect(mShadowLeft, mShadowTop, mShadowRight, mShadowBottom, mPaintSh);

        canvas.drawBitmap(mBitmapThumbnail, mImageLeft, mImageTop, mPaintFg);
    }

    public Bitmap getBitmap() {
        return mBitmap;
    }

    public void setBitmap(Bitmap bitmap) {
        mBitmap = bitmap;
    }
    
    public void setImagePath(String imagePath) {
        mImagePath = imagePath;
    }

    public void setNoteBackgroundColor(int color) {
        mPaintBg.setColor(color);
    }

    public void setShadowBackgroundColor(int color) {
        mPaintSh.setColor(color);
    }

    public void setTextStyle(int color, int size) {
        mPaintFg.setColor(color);
        mPaintFg.setTextSize(size);
    }
}
