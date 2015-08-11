package uk.porcheron.co_curator.item;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.RectF;
import android.view.View;

import java.util.Random;

import uk.porcheron.co_curator.TimelineActivity;
import uk.porcheron.co_curator.user.User;
import uk.porcheron.co_curator.util.Style;

/**
 * A timeline item. Only the _actual_ content is drawn (i.e. no borders etc).
 */
public abstract class Item extends View {
    private static final String TAG = "CC:Item";

    private User mUser;
    private int mItemId;

    private RectF mSlotBounds;
    private RectF mOuterBounds;
    private RectF mInnerBounds;
    private RectF mStemBounds;
    private RectF mStemConnectorBounds;

    private float mRandomPadRight;
    private float mRandomPadRightHalf;

    private Random mRandom = new Random();

    public Item(TimelineActivity activity, User user, int itemId) {
        super(activity);

        mUser = user;
        mItemId = itemId;

        mRandomPadRight = Style.itemXGapMin + mRandom.nextInt((int) Style.itemXGapOffset);
        mRandomPadRightHalf = mRandomPadRight / 2;
    }

    @Override
    protected final void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        setMeasuredDimension((int) mSlotBounds.width(), (int) mSlotBounds.height());
    }

    @Override
    public void onDraw(Canvas canvas) {
        canvas.drawRoundRect(mOuterBounds,
                Style.itemRoundedCorners,
                Style.itemRoundedCorners,
                getUser().bgPaint);

        canvas.drawRect(mStemBounds,
                getUser().bgPaint);
    }

    protected final RectF getInnerBounds() {
        return mInnerBounds;
    }

    protected final RectF getmStemConnectorBounds() {
        return mStemConnectorBounds;
    }

    protected final RectF setBounds(float width, float height, float padding) {
        float top = Style.itemFullHeight + mUser.offset - height;

        mOuterBounds = new RectF(mRandomPadRightHalf, top, mRandomPadRightHalf + width, top + height);

        mInnerBounds = new RectF(mOuterBounds.left + padding,
                mOuterBounds.top + padding,
                mOuterBounds.right - padding,
                mOuterBounds.bottom - padding);

        mSlotBounds = new RectF(0, 0, mOuterBounds.width() + mRandomPadRight, Style.itemFullHeight);

        float offset = mRandomPadRightHalf + Style.itemStemNarrowBy +
                mRandom.nextInt((int) (mInnerBounds.width() - (2 * Style.itemStemNarrowBy)));
        mStemBounds = new RectF(offset, mOuterBounds.bottom, offset + Style.lineWidth, mSlotBounds.bottom);

        mStemConnectorBounds = new RectF(offset, mSlotBounds.bottom, mStemBounds.right, mSlotBounds.bottom + mUser.centrelineOffset);

        return mInnerBounds;
    }

    protected final void shrink(float dw, float dh) {
        mOuterBounds.right -= dw;
        mInnerBounds.right -= dw;
        mSlotBounds.right -= dw;

        mOuterBounds.bottom -= dh;
        mInnerBounds.bottom -= dh;
        mSlotBounds.bottom -= dh;

        float offset = mRandomPadRightHalf + Style.itemStemNarrowBy +
                mRandom.nextInt((int) (mInnerBounds.width() - (2 * Style.itemStemNarrowBy)));
        mStemBounds = new RectF(offset, mOuterBounds.bottom, offset + Style.lineWidth, mSlotBounds.bottom);

        mStemConnectorBounds = new RectF(offset, mSlotBounds.bottom, mStemBounds.right, mSlotBounds.bottom + mUser.centrelineOffset);
    }

    protected final User getUser() {
        return mUser;
    }

    protected final int getItemId() {
        return mItemId;
    }
}
