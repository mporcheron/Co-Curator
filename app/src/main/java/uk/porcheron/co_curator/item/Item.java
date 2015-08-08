package uk.porcheron.co_curator.item;

import android.content.Context;
import android.graphics.Canvas;
import android.util.Log;
import android.view.View;

import uk.porcheron.co_curator.user.User;
import uk.porcheron.co_curator.util.Style;

/**
 * A timeline item.
 */
public abstract class Item extends View {
    private static final String TAG = "CC:Item";

    protected int mItemX1;
    protected int mItemX2;
    protected int mItemY1;
    protected int mItemY2;

    protected User mUser;

    protected float mNotchX1;
    protected float mNotchX2;
    protected float mNotchY1;
    protected float mNotchY2;

    public Item(Context context, User user, boolean above) {
        super(context);
        mUser = user;

        mItemX1 = 0;
        mItemX2 = mItemX1 + Style.itemWidth;
        mItemY1 = Style.itemPadY;
        mItemY2 = mItemY1 + Style.itemHeight;

        float halfLineWidth = Style.lineWidth / 2;

        mNotchX1 = (Style.itemWidth / 2) - halfLineWidth;
        mNotchX2 = mNotchX1 + Style.lineWidth;

        mNotchY1 = Style.itemFullHeight - Style.notchHeight;
        mNotchY2 = mNotchY1 + (Style.notchHeight - mUser.offset);

        if(!above) {
            mNotchY1 = mNotchY1 - Style.itemFullHeight + Style.notchHeight;
            mNotchY2 = mNotchY2 - Style.itemFullHeight + Style.notchHeight;
        }

    }

    @Override
    public void onDraw(Canvas canvas) {
        Log.d(TAG, "Draw Notch from (" + mNotchX1 + "," + mNotchY1 + ") to (" + mNotchX2 + "," + mNotchY2 + ")");
        canvas.drawRect(mNotchX1, mNotchY1, mNotchX2, mNotchY2, mUser.paint);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        setMeasuredDimension(Style.itemFullWidth, Style.itemFullHeight);
    }
}
