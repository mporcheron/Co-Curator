package uk.porcheron.co_curator.item;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.text.TextPaint;
import android.text.TextUtils;
import android.view.Gravity;
import android.widget.TextView;

import uk.porcheron.co_curator.user.User;
import uk.porcheron.co_curator.util.Style;
import uk.porcheron.co_curator.util.EllipsizingTextView;

/**
 * An item that contains text.
 * <p/>
 * Created by map on 06/08/15.
 */
public class ItemNote extends Item {
    private static final String TAG = "CC:ItemNote";

    private int mShadowX1;
    private int mShadowY1;
    private int mShadowX2;
    private int mShadowY2;

    private float mTextX;
    private float mTextY;

    protected String mText;
    protected Paint mPaintNotch;
    protected Paint mPaintBg;
    protected Paint mPaintSh;
    private TextPaint mPaintFg;
    private TextView mTextView;

    public ItemNote(Context context, User user) {
        super(context, user);

        Resources res = context.getResources();

        mPaintBg = new Paint(Paint.ANTI_ALIAS_FLAG);
        mPaintBg.setStyle(Paint.Style.FILL);
        mPaintBg.setColor(Style.noteBg);

        mPaintSh = new Paint(Paint.ANTI_ALIAS_FLAG);
        mPaintSh.setStyle(Paint.Style.FILL);
        mPaintSh.setColor(Style.noteSh);

        mPaintFg = new TextPaint(Paint.ANTI_ALIAS_FLAG);
        mPaintFg.setColor(Style.noteFg);
        mPaintFg.setTextSize(Style.noteFontSize);

        mItemX2 = mItemX2 - Style.noteShadowSize;
        mItemY2 = mItemY2 - Style.noteShadowSize;

        mShadowX1 = Style.noteShadowOffset;
        mShadowY1 = Style.noteShadowOffset;
        mShadowX2 = mItemX2 + Style.noteShadowSize;
        mShadowY2 = mItemY2 + Style.noteShadowSize;

        mTextX = mItemX1 + Style.notePadding;
        mTextY = mItemY1 + Style.notePadding;

        int width = (int) (mItemX2 - (2 * Style.notePadding));
        int height = (int) (mItemY2 - (2 * Style.notePadding));

        mTextView = new EllipsizingTextView(getContext());
        mTextView.layout(0, 0, width, height);
        mTextView.setGravity(Gravity.CENTER_VERTICAL);
        mTextView.setEllipsize(TextUtils.TruncateAt.END);
        mTextView.setMaxLines(Style.noteLines);
        mTextView.setLineSpacing(0, Style.noteLineSpacing);
        mTextView.setTextColor(Style.noteFg);

    }

    @Override
    public void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        canvas.drawRect(mShadowX1, mShadowY1, mShadowX2, mShadowY2, mPaintSh);

        canvas.drawRect(mItemX1, mItemY1, mItemX2, mItemY2, mPaintBg);

        mTextView.setDrawingCacheEnabled(true);
        canvas.drawBitmap(mTextView.getDrawingCache(), mTextX, mTextY, mPaintFg);
        mTextView.setDrawingCacheEnabled(false);
    }
    
    public void setText(String text) {
        mText = text;
        mTextView.setText(text);
    }
}
