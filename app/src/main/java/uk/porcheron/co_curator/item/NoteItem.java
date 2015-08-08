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
public class NoteItem extends Item {
    private static final String TAG = "CC:Item:Note";

    private int mNoteWidth = 0;
    private int mNoteHeight = 0;

    private int mShadowWidth = 0;
    private int mShadowHeight = 0;

    private String mText;
    private Paint mPaintNotch;
    private Paint mPaintBg;
    private Paint mPaintSh;
    private TextPaint mPaintFg;
    private TextView mTextView;

    public NoteItem(Context context, User user, boolean above) {
        super(context, user, above);

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

        mNoteWidth = Style.itemWidth - Style.noteShadowSize;
        mNoteHeight = Style.itemHeight - Style.noteShadowSize;

        mShadowWidth = mNoteWidth + Style.noteShadowSize;
        mShadowHeight = mNoteHeight + Style.noteShadowSize;

        int width = (int) (mNoteWidth - (2 * Style.notePadding));
        int height = (int) (mNoteHeight - (2 * Style.notePadding));

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
        canvas.drawRect(Style.noteShadowOffset, Style.noteShadowOffset,
                mShadowWidth, mShadowHeight, mPaintSh);

        canvas.drawRect(0, 0, mNoteWidth, mNoteHeight, mPaintBg);

        mTextView.setDrawingCacheEnabled(true);
        canvas.drawBitmap(mTextView.getDrawingCache(), Style.notePadding, Style.notePadding, mPaintFg);
        mTextView.setDrawingCacheEnabled(false);
    }

    public void setText(String text) {
        mText = text;
        mTextView.setText(text);
    }
}
