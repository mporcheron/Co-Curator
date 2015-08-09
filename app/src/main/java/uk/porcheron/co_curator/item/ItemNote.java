package uk.porcheron.co_curator.item;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.text.TextPaint;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.widget.TextView;

import uk.porcheron.co_curator.util.Style;
import uk.porcheron.co_curator.util.EllipsizingTextView;

/**
 * An item that contains text.
 * <p/>
 * Created by map on 06/08/15.
 */
public class ItemNote extends Item {
    private static final String TAG = "CC:ItemNote";

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

    private String mText;
    private Paint mPaintBg;
    private Paint mPaintSh;
    private TextPaint mPaintFg;
    private TextView mTextView;

    public ItemNote(Context context) {
        super(context);

        mPaintBg = new Paint(Paint.ANTI_ALIAS_FLAG);
        mPaintBg.setStyle(Paint.Style.FILL);
        mPaintSh = new Paint(Paint.ANTI_ALIAS_FLAG);
        mPaintSh.setStyle(Paint.Style.FILL);
        mPaintFg = new TextPaint(Paint.ANTI_ALIAS_FLAG);

        mTextView = new EllipsizingTextView(getContext());

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

        mTextView.layout(0, 0, (int) mTextRight, (int) mTextBottom);
        mTextView.setGravity(Gravity.CENTER_VERTICAL);
        mTextView.setEllipsize(TextUtils.TruncateAt.END);
        mTextView.setMaxLines(Style.noteLines);
        mTextView.setLineSpacing(0, Style.noteLineSpacing);
        mTextView.setTextColor(Style.noteFg);
    }

    @Override
    public void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        canvas.drawRect(mShadowLeft, mShadowTop, mShadowRight, mShadowBottom, mPaintSh);
        canvas.drawRect(mNoteLeft, mNoteTop, mNoteRight, mNoteBottom, mPaintBg);

        mTextView.setDrawingCacheEnabled(true);
        canvas.drawBitmap(mTextView.getDrawingCache(), mTextLeft, mTextTop, mPaintFg);
        mTextView.setDrawingCacheEnabled(false);
    }

    public String getText() {
       return mText;
    }
    
    public void setText(String text) {
        mText = text;
        mTextView.setText(text);
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
