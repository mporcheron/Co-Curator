package uk.porcheron.co_curator.item;

import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.text.TextPaint;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;

import uk.porcheron.co_curator.TimelineActivity;
import uk.porcheron.co_curator.item.dialog.DialogNote;
import uk.porcheron.co_curator.user.User;
import uk.porcheron.co_curator.util.CCLog;
import uk.porcheron.co_curator.util.EllipsizingTextView;
import uk.porcheron.co_curator.util.Event;
import uk.porcheron.co_curator.val.Instance;
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
    private EllipsizingTextView mTextView;

    public ItemNote(Context context) { super(context); }

    public ItemNote(Context context, AttributeSet attrs) { super(context, attrs); }

    public ItemNote(Context context, AttributeSet attrs, int defStyle) { super(context, attrs, defStyle); }

    public ItemNote(int itemId, User user, int dateTime) {
        super(user, itemId, dateTime);

        mPaintFg = new TextPaint(Paint.ANTI_ALIAS_FLAG);
        mPaintFg.setColor(user.fgColor);
        mPaintFg.setTextSize(Style.noteFontSize);

    }

    @Override
    public String getData() {
        return mText;
    }

    @Override
    public String setData(String text) {
        mText = text;

        RectF innerBounds = setBounds(Style.noteWidth, Style.noteHeight, Style.notePadding);

        mTextView = new EllipsizingTextView(TimelineActivity.getInstance());
        mTextView.layout((int) innerBounds.left, (int) innerBounds.top,
                (int) innerBounds.right, (int) innerBounds.bottom);
        mTextView.setGravity(Gravity.CENTER_VERTICAL);
        mTextView.setEllipsize(TextUtils.TruncateAt.END);
        mTextView.setMaxLines(Style.noteLines);
        mTextView.setLineSpacing(0, Style.noteLineSpacing);
        mTextView.setTextSize(TypedValue.COMPLEX_UNIT_PT, Style.noteFontSize);
        mTextView.setTextColor(getUser().fgColor);
        mTextView.setText(text);

        return text;
    }

    @Override
    public void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        RectF innerBounds = getInnerBounds();
        mTextView.setDrawingCacheEnabled(true);
        canvas.drawBitmap(mTextView.getDrawingCache(), innerBounds.left, innerBounds.top, mPaintFg);
        mTextView.setDrawingCacheEnabled(false);
    }

    @Override
    public boolean onTap() {
        onSelect(false, false);
        return true;
    }

    @Override
    protected boolean onLongPress() {
        boolean userMatches = getUser().equals(Instance.user());
        onSelect(userMatches, userMatches);

        return true;
    }

    @Override
    protected void onSelect(boolean editable, boolean deletable) {
        new DialogNote()
                .setText(mText)
                .setOnSubmitListener(new DialogNote.OnSubmitListener() {
                    @Override
                    public void onSubmit(DialogInterface dialog, String text) {
                        CCLog.write(Event.ITEM_UPDATE, "{uniqueItemId=" + getUniqueItemId() + ",text=" + text + "}");
                        Instance.items.update(ItemNote.this, text, true, true);
                    }
                })
                .setOnDeleteListener(new DialogNote.OnDeleteListener() {
                    @Override
                    public void onDelete(DialogInterface dialog) {
                        CCLog.write(Event.ITEM_DELETE, "{uniqueItemId=" + getUniqueItemId() + "}");
                        Instance.items.remove(ItemNote.this, true, true, true);
                    }
                })
                .setUser(getUser())
                .isDeletable(deletable)
                .isEditable(editable)
                .create()
                .show();
    }

}
