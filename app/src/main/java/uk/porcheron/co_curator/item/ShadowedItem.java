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

import uk.porcheron.co_curator.user.User;
import uk.porcheron.co_curator.util.Style;
import uk.porcheron.co_curator.util.EllipsizingTextView;

/**
 * An item that has a shadow.
 */
public class ShadowedItem extends Item {
    private static final String TAG = "CC:ShadowedItem";

    private float mItemLeft;
    private float mItemTop;
    private float mItemRight;
    private float mItemBottom;

    private float mShadowLeft;
    private float mShadowTop;
    private float mShadowRight;
    private float mShadowBottom;

    private Paint mPaintSh;

    public ShadowedItem(Context context, Item item) {
        super(context, item.getUser(), item.getItemId());

        mPaintSh = new Paint(Paint.ANTI_ALIAS_FLAG);
        mPaintSh.setStyle(Paint.Style.FILL);

        setShadowBackgroundColor(Style.noteSh);

        RectF b = getBounds();
        mItemLeft = b.left;
        mItemTop = b.top;
        mItemRight = b.right - Style.noteShadowSize;
        mItemBottom = b.bottom - Style.noteShadowSize;

        mShadowLeft = Style.noteShadowOffset;
        mShadowTop = Style.noteShadowOffset;
        mShadowRight = b.right;
        mShadowBottom = b.bottom;
    }

    @Override
    public void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        canvas.drawRect(mShadowLeft, mShadowTop, mShadowRight, mShadowBottom, mPaintSh);
    }

    public void setShadowBackgroundColor(int color) {
        mPaintSh.setColor(color);
    }

}
