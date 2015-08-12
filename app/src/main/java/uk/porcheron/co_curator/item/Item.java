package uk.porcheron.co_curator.item;

import android.graphics.Canvas;
import android.graphics.RectF;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import java.util.Random;

import uk.porcheron.co_curator.TimelineActivity;
import uk.porcheron.co_curator.user.User;
import uk.porcheron.co_curator.util.Style;

/**
 * A timeline item. Only the _actual_ content is drawn (i.e. no borders etc).
 */
public abstract class Item extends View implements View.OnTouchListener {
    private static final String TAG = "CC:Item";

    private User mUser;
    private int mItemId;

    private RectF mSlotBounds;
    private RectF mOuterBounds;
    private RectF mInnerBounds;
    private RectF mStemBounds;
    private RectF mBranchBounds;

    private float mRandomPadRight;
    private float mRandomPadRightHalf;

    private static Random mRandom = new Random();

    public Item(TimelineActivity activity, User user, int itemId) {
        super(activity);

        mUser = user;
        mItemId = itemId;

        mRandomPadRight = Style.itemXGapMin + mRandom.nextInt((int) Style.itemXGapOffset);
        mRandomPadRightHalf = mRandomPadRight / 2;

        setOnTouchListener(this);
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

    protected final RectF getSlotBounds() {
        return mSlotBounds;
    }

    protected final RectF getInnerBounds() {
        return mInnerBounds;
    }

    protected final RectF getBranchBounds() {
        return mBranchBounds;
    }

    protected final RectF setBounds(float width, float height, float padding) {
        float top;
        if(mUser.above) {
            top = Style.itemFullHeight + mUser.offset - height;
        } else {
            top = mUser.offset;
        }

        mOuterBounds = new RectF(mRandomPadRightHalf, top, mRandomPadRightHalf + width, top + height);

        mInnerBounds = new RectF(mOuterBounds.left + padding,
                mOuterBounds.top + padding,
                mOuterBounds.right - padding,
                mOuterBounds.bottom - padding);

        mSlotBounds = new RectF(0, 0, mOuterBounds.width() + mRandomPadRight, Style.itemFullHeight);

        float offset = mRandomPadRightHalf + Style.itemStemNarrowBy +
                mRandom.nextInt((int) (mInnerBounds.width() - (2 * Style.itemStemNarrowBy)));

        if(mUser.above) {
            mStemBounds = new RectF(offset, mOuterBounds.bottom, offset + Style.lineWidth, mSlotBounds.bottom);
            mBranchBounds = new RectF(offset, 0, mStemBounds.right, mUser.centrelineOffset);
        } else {
            mStemBounds = new RectF(offset, 0, offset + Style.lineWidth, mOuterBounds.top);
            mBranchBounds = new RectF(offset, mUser.centrelineOffset + Style.lineWidth, mStemBounds.right,  Style.layoutCentreHeight);
        }

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

        mBranchBounds = new RectF(offset, mSlotBounds.bottom, mStemBounds.right, mSlotBounds.bottom + mUser.centrelineOffset);
    }

    protected final User getUser() {
        return mUser;
    }

    protected final int getItemId() {
        return mItemId;
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        Log.d(TAG, "Touched");
        if (event.getAction() == MotionEvent.ACTION_DOWN){
            Log.d(TAG, "ACTION_DOWN");
            if(mOuterBounds.contains(event.getX(), event.getY())) {
                Log.d(TAG, "Within Outer Bounds");
                onClick(v);
                return true;
            }
        }

        return false;
    }

    protected abstract void onClick(View v);
}
