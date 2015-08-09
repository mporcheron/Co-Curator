package uk.porcheron.co_curator.item;

import android.content.Context;
import android.util.Log;
import android.view.View;

import uk.porcheron.co_curator.line.Notch;
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

    public Notch mNotch;

    public Item(Context context, User user) {
        super(context);

        mUser = user;

        mItemX1 = 0;
        mItemX2 = mItemX1 + Style.itemWidth;
        mItemY1 = 0;
        mItemY2 = mItemY1 + Style.itemHeight;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        setMeasuredDimension(Style.itemWidth, Style.itemHeight);
        Log.d(TAG, "Container = (" + Style.itemWidth + "," + Style.itemHeight + ")");
    }
}
