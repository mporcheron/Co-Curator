package uk.porcheron.co_curator.item;

import android.content.Context;
import android.graphics.RectF;
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

    private ItemContainer mContainer;
    private User mUser;
    private int mItemId;

    private RectF mBounds;

    public Item(Context context, User user, int itemId) {
        super(context);

        mUser = user;
        mItemId = itemId;
        mBounds = new RectF(0, 0, Style.itemWidth, Style.itemHeight);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        setMeasuredDimension((int) mBounds.width(), (int) mBounds.height());
    }

    protected final void setContainer(ItemContainer container) {
        mContainer = container;
        this.onCreate();
    }

    protected RectF getBounds() {
        return mBounds;
    }

    protected ItemContainer getContainer() {
        return mContainer;
    }

    protected User getUser() {
        return mUser;
    }

    protected int getItemId() {
        return mItemId;
    }

    protected void onCreate() {

    }
}