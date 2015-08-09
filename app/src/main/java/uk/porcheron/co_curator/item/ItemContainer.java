package uk.porcheron.co_curator.item;

import android.content.Context;
import android.graphics.Canvas;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;

import uk.porcheron.co_curator.line.Notch;
import uk.porcheron.co_curator.user.User;
import uk.porcheron.co_curator.util.Style;

/**
 * A timeline item.
 */
public class ItemContainer extends ViewGroup {
    private static final String TAG = "CC:ItemContainer";

    protected int mItemX1;
    protected int mItemX2;
    protected int mItemY1;
    protected int mItemY2;
    protected Item mItem;

    protected User mUser;

    public Notch mNotch;

    public ItemContainer(Context context, Item item, User user, boolean above) {
        super(context);

        mUser = user;
        mItem = item;

        mItemX1 = 0;
        mItemX2 = mItemX1 + Style.itemWidth;
        mItemY1 = Style.itemPadY;
        mItemY2 = mItemY1 + Style.itemHeight;

        mNotch = new Notch(context, user, above);

        mItem.layout(0, Style.itemPadY, Style.itemWidth, Style.itemHeight + Style.itemPadY);
        addView(mItem, Style.itemWidth, Style.itemHeight);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        setMeasuredDimension(Style.itemFullWidth, Style.itemFullHeight);
    }


    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {

    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        mItem.draw(canvas);
    }
}
