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

    private Item mItem;
    private User mUser;
    private Notch mNotch;

    public ItemContainer(Context context, Item item, User user, boolean above) {
        super(context);

        mUser = user;
        mItem = item;
        mNotch = new Notch(context, user, above);

        mItem.setContainer(this);
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

    protected User getUser() {
        return mUser;
    }

    public Notch getNotch() {
        return mNotch;
    }
}
