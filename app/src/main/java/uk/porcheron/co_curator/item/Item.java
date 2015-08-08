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

//        boolean above = user.offset <= 0;
        Log.d(TAG, "id = " + user.userId + "; offset = " + user.offset);

        mItemX1 = 0;
        mItemX2 = mItemX1 + Style.itemWidth;
        mItemY1 = Style.itemPadY;
        mItemY2 = mItemY1 + Style.itemHeight;

        float halfLineWidth = Style.lineWidth / 2;

        mNotchX1 = (Style.itemWidth / 2) - halfLineWidth;
        mNotchX2 = mNotchX1 + Style.lineWidth;

        if(above) {
            mNotchY1 = Style.itemFullHeight - Style.notchHeight - Style.layoutAboveOverlap;
            mNotchY2 = mNotchY1 + Style.notchHeight;

            if(mUser.offset < 0) {
                mNotchY2 = mNotchY2 + mUser.offset;
            } else if(mUser.offset > 0) {
                mNotchY2 = mNotchY1 + Style.notchHeight - (2 * -mUser.offset);
                Log.d(TAG, "y1 = " + mNotchY1 + "; y2 = " + mNotchY2);
            }
        } else {
            mNotchY1 = mUser.offset;
            mNotchY2 = Style.notchHeight - mNotchY1;


            //mNotchY1 = mNotchY1 - Style.itemFullHeight + Style.notchHeight;
            //mNotchY2 = mNotchY2 - Style.itemFullHeight + Style.notchHeight;
        }

    }

    @Override
    public void onDraw(Canvas canvas) {
        canvas.drawRect(mNotchX1, mNotchY1, mNotchX2, mNotchY2, mUser.paint);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        setMeasuredDimension(Style.itemFullWidth, Style.itemFullHeight);
    }
}
