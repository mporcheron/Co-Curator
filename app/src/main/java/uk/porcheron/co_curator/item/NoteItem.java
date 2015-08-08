package uk.porcheron.co_curator.item;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.text.DynamicLayout;
import android.text.SpannableStringBuilder;
import android.text.TextPaint;
import android.text.TextUtils;
import android.view.Gravity;
import android.widget.TextView;

import uk.porcheron.co_curator.util.EllipsizingTextView;

/**
 * An item that contains text.
 *
 * Created by map on 06/08/15.
 */
public class NoteItem extends Item {

    private static final String TAG = "CC:Item:Note";

    private int mNoteWidth = 0;
    private int mNoteHeight = 0;

    private int mShadowWidth = 0;
    private int mShadowHeight = 0;

    private String mText;
    private Paint mPaintBg;
    private Paint mPaintSh;
    private TextPaint mPaintFg;
    private TextView mTextView;

    public NoteItem(Context context) {
        super(context);

        Resources res = context.getResources();

        mPaintBg = new Paint(Paint.ANTI_ALIAS_FLAG);
        mPaintBg.setStyle(Paint.Style.FILL);
        mPaintBg.setColor(Style.mNoteBg);

        mPaintSh = new Paint(Paint.ANTI_ALIAS_FLAG);
        mPaintSh.setStyle(Paint.Style.FILL);
        mPaintSh.setColor(Style.mNoteSh);

        mPaintFg = new TextPaint(Paint.ANTI_ALIAS_FLAG);
        mPaintFg.setColor(Style.mNoteFg);
        mPaintFg.setTextSize(Style.mNoteFontSize);

        mNoteWidth = Style.mItemWidth - Style.mItemShadowSize;
        mNoteHeight = Style.mItemHeight - Style.mItemShadowSize;

        mShadowWidth = mNoteWidth + Style.mItemShadowSize;
        mShadowHeight = mNoteHeight + Style.mItemShadowSize;

        int width = (int) (mNoteWidth - (2 * Style.mNotePadding));
        int height = (int) (mNoteHeight - (2 * Style.mNotePadding));

        mTextView = new EllipsizingTextView(getContext());
        mTextView.layout(0, 0, width, height);
        mTextView.setGravity(Gravity.CENTER_VERTICAL);
        mTextView.setEllipsize(TextUtils.TruncateAt.END);
        mTextView.setMaxLines(Style.mNoteLines);
        mTextView.setLineSpacing(0, Style.mNoteLineSpacing);
        mTextView.setTextColor(Style.mNoteFg);

    }

    @Override
    public void onDraw(Canvas canvas) {
        canvas.drawRect(Style.mItemShadowOffset, Style.mItemShadowOffset,
                mShadowWidth, mShadowHeight, mPaintSh);

        canvas.drawRect(0, 0, mNoteWidth, mNoteHeight, mPaintBg);

        mTextView.setDrawingCacheEnabled(true);
        canvas.drawBitmap(mTextView.getDrawingCache(), Style.mNotePadding, Style.mNotePadding, mPaintFg);
        mTextView.setDrawingCacheEnabled(false);
    }

    public void setText(String text) {
        mText = text;
        mTextView.setText(text);
     }

}
