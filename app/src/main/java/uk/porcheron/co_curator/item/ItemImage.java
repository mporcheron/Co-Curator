package uk.porcheron.co_curator.item;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.text.TextPaint;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.TextView;

import java.io.FileInputStream;

import uk.porcheron.co_curator.user.User;
import uk.porcheron.co_curator.util.EllipsizingTextView;
import uk.porcheron.co_curator.util.Style;

/**
 * An item that contains text.
 * <p/>
 * Created by map on 06/08/15.
 */
public class ItemImage extends Item {
    private static final String TAG = "CC:ItemImage";

    private float mNoteLeft;
    private float mNoteTop;
    private float mNoteRight;
    private float mNoteBottom;

    private float mShadowLeft;
    private float mShadowTop;
    private float mShadowRight;
    private float mShadowBottom;

    private float mTextLeft;
    private float mTextTop;
    private float mTextRight;
    private float mTextBottom;

    private Bitmap mBitmap = null;
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
        mNoteLeft = b.left;
        mNoteTop = b.top;
        mNoteRight = b.right - Style.noteShadowSize;
        mNoteBottom = b.bottom - Style.noteShadowSize;

        mShadowLeft = Style.noteShadowOffset;
        mShadowTop = Style.noteShadowOffset;
        mShadowRight = b.right;
        mShadowBottom = b.bottom;

        mTextLeft = mNoteLeft + Style.notePadding;
        mTextTop = mNoteTop + Style.notePadding;
        mTextRight = (b.right - (2 * Style.notePadding));
        mTextBottom = (b.bottom - (2 * Style.notePadding));
    }

    @Override
    public void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        canvas.drawRect(mShadowLeft, mShadowTop, mShadowRight, mShadowBottom, mPaintSh);
        canvas.drawRect(mNoteLeft, mNoteTop, mNoteRight, mNoteBottom, mPaintBg);

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

        canvas.drawBitmap(mBitmap, mNoteLeft, mNoteTop, mPaintFg);
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
