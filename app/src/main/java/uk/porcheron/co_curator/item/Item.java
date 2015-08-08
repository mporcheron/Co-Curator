package uk.porcheron.co_curator.item;

import android.content.Context;
import android.view.View;

import uk.porcheron.co_curator.user.User;
import uk.porcheron.co_curator.util.Style;

/**
 * A timeline item.
 */
public abstract class Item extends View {
    protected User mUser;

    protected float mNotchX1;
    protected float mNotchX2;
    protected float mNotchY1;
    protected float mNotchY2;

    public Item(Context context, User user, boolean above) {
        super(context);
        mUser = user;

        float halfLineWidth = Style.lineWidth / 2;

        if(above) {
            mNotchX1 = Style.itemFullWidth / 2 - halfLineWidth;
            mNotchX2 = mNotchX1 + Style.lineWidth;

            mNotchY1 = Style.notchHeight - mUser.offset - halfLineWidth;
            mNotchY2 = mUser.offset - halfLineWidth;
        } else {
            mNotchX1 = Style.itemFullWidth / 2 - halfLineWidth;
            mNotchX2 = mNotchX1 + Style.lineWidth;

            mNotchY1 = Style.notchHeight - mUser.offset - halfLineWidth;
            mNotchY2 = mUser.offset - halfLineWidth;
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        setMeasuredDimension(Style.itemFullWidth, Style.itemFullHeight);
    }
}
