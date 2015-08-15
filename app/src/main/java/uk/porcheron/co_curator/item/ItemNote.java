package uk.porcheron.co_curator.item;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.text.TextPaint;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.widget.TextView;

import uk.porcheron.co_curator.TimelineActivity;
import uk.porcheron.co_curator.user.User;
import uk.porcheron.co_curator.util.EllipsizingTextView;
import uk.porcheron.co_curator.val.Style;

/**
 * An item that contains text.
 * <p/>
 * Created by map on 06/08/15.
 */
public class ItemNote extends Item {
    private static final String TAG = "CC:ItemNote";

    private String mText;
    private TextPaint mPaintFg;
    private TextView mTextView;

    public ItemNote(Context context) { super(context); }

    public ItemNote(Context context, AttributeSet attrs) { super(context, attrs); }

    public ItemNote(Context context, AttributeSet attrs, int defStyle) { super(context, attrs, defStyle); }

    public ItemNote(int itemId, User user, int dateTime) {
        super(user, itemId, dateTime);

        mPaintFg = new TextPaint(Paint.ANTI_ALIAS_FLAG);
        mPaintFg.setColor(user.fgColor);
        mPaintFg.setTextSize(Style.noteFontSize);

        RectF innerBounds = setBounds(Style.noteWidth, Style.noteHeight, Style.notePadding);

        mTextView = new EllipsizingTextView(TimelineActivity.getInstance());
        mTextView.layout((int) innerBounds.left, (int) innerBounds.top,
                (int) innerBounds.right, (int) innerBounds.bottom);
        mTextView.setGravity(Gravity.CENTER_VERTICAL);
        mTextView.setEllipsize(TextUtils.TruncateAt.END);
        mTextView.setMaxLines(Style.noteLines);
        mTextView.setLineSpacing(0, Style.noteLineSpacing);
        mTextView.setTextSize(TypedValue.COMPLEX_UNIT_PT, Style.noteFontSize);
        mTextView.setTextColor(user.fgColor);
    }

    @Override
    public void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        RectF innerBounds = getInnerBounds();
        mTextView.setDrawingCacheEnabled(true);
        canvas.drawBitmap(mTextView.getDrawingCache(), innerBounds.left, innerBounds.top, mPaintFg);
        mTextView.setDrawingCacheEnabled(false);
    }

    public String getText() {
        return mText;
    }

    public void setText(String text) {
        mText = text;
        mTextView.setText(text);
    }

    @Override
    public boolean onTap() {
        return false;
    }

}
