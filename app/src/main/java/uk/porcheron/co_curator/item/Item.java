package uk.porcheron.co_curator.item;

import android.app.Activity;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.RectF;
import android.os.Handler;
import android.util.AttributeSet;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;

import java.util.Random;

import uk.porcheron.co_curator.TimelineActivity;
import uk.porcheron.co_curator.user.User;
import uk.porcheron.co_curator.util.CCLog;
import uk.porcheron.co_curator.util.Event;
import uk.porcheron.co_curator.val.Style;

/**
 * A timeline item. Only the _actual_ content is drawn (i.e. no borders etc).
 */
public abstract class Item extends View {
    private static final String TAG = "CC:Item";

    private static Random mRandom = new Random();

    private float mSetWidth;
    private float mSetHeight;
    private float mSetPadding;

    private User mUser;
    private int mItemId;
    private int mItemIndex;
    private long mDateTime;

    private float mX;
    private float mLongPressX;

    private float mOuterMargin;
    private RectF mSlotBounds;
    private RectF mOuterBounds;
    private RectF mInnerBounds;
    private RectF mStemBounds;
    private float mStemStepChangeT;
    private float mStemStepChangeB;
    private long mStemRedraw;

    private boolean mDrawn = false;
    private boolean mDeleted = false;
    private int mStemGrowth = 0;

    private static final int SWIPE_MIN_DISTANCE = 120;
    private static final int SWIPE_THRESHOLD_VELOCITY = 200;

    public Item(Context context) { super(context); }

    public Item(Context context, AttributeSet attrs) { super(context, attrs); }

    public Item(Context context, AttributeSet attrs, int defStyle) { super(context, attrs, defStyle); }

    public Item(User user, int itemId, long dateTime) {
        super(TimelineActivity.getInstance());

        mUser = user;
        mItemId = itemId;
        mDateTime = dateTime;

        final GestureDetector gD  = new GestureDetector(TimelineActivity.getInstance(), new GestureListener());
        setOnTouchListener(new OnTouchListener() {
            @Override
            public boolean onTouch(final View view, final MotionEvent e) {
                if (mOuterBounds.contains(e.getX(), e.getY())) {
                    gD.onTouchEvent(e);

                    if (e.getAction() == MotionEvent.ACTION_DOWN) {
                        handler.postDelayed(mLongPressed, LONG_PRESS_DELAY);
                        mLongPressX = e.getX();
                        mCancelLongPress = false;
                        return true;
                    }

                    if (mCancelLongPress || e.getAction() == MotionEvent.ACTION_MOVE
                            || e.getAction() == MotionEvent.ACTION_UP
                            || e.getAction() == MotionEvent.ACTION_SCROLL) {
                        handler.removeCallbacks(mLongPressed);
                    }

                    return false;
                }
                return false;
            }
        });
    }

    public boolean dataChanged(String data) {
        return !getData().equals(data);
    }

    public abstract String getData();

    abstract String setData(String data);

    @Override
    protected final void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        setMeasuredDimension((int) mSlotBounds.width(), (int) mSlotBounds.height());
    }

    public final void setDrawn(boolean newState) {
        mDrawn = newState;
    }

    public final boolean isDrawn() {
        return mDrawn;
    }

    final void setDeleted(boolean newState) {
        mDeleted = newState;
    }

    public final boolean isDeleted() {
        return mDeleted;
    }

    @Override
    public void onDraw(Canvas canvas) {
        //if(mStemGrowth == 0) {
        canvas.drawRoundRect(mOuterBounds,
                Style.itemRoundedCorners,
                Style.itemRoundedCorners,
                getUser().bgPaint);
        //}

        canvas.drawRect(mStemBounds, getUser().bgPaint);
//        mStemBounds.set(mStemBounds.left, mStemBounds.top + mStemStepChangeT,
//                mStemBounds.right, mStemBounds.bottom + mStemStepChangeB);
//
//        if(mStemGrowth++ < Style.itemStemGrowSteps) {
//            this.postInvalidateDelayed(mStemRedraw);
//        }
    }

    protected final void setDrawnX(float x) {
        mX = x;
    }

    protected final float getDrawnX() {
        return mX;
    }

    protected final RectF getSlotBounds() {
        return mSlotBounds;
    }

    protected final RectF getInnerBounds() {
        return mInnerBounds;
    }

    protected final float getOuterMargin() { return mOuterMargin; }

    protected final RectF setBounds(float width, float height, float padding) {
        mSetWidth = width;
        mSetHeight = height;
        mSetPadding = padding;

        float top;
        if (mUser.above) {
            mOuterMargin = Style.itemFullHeight + mUser.offset - height;
            top = 0;
        } else {
            mOuterMargin = 0;
            top = Style.layoutCentreHeight + mUser.offset;
        }

        // mRandomPadRightHalf
        mOuterBounds = new RectF(0, top, width, top + height);

        mInnerBounds = new RectF(mOuterBounds.left + padding,
                mOuterBounds.top + padding,
                mOuterBounds.right - padding,
                mOuterBounds.bottom - padding);

        //+ mRandomPadRight
        mSlotBounds = new RectF(0, 0, mOuterBounds.width(), Style.itemFullHeight + Style.layoutCentreHeight);

        float offset = Style.itemStemNarrowBy +
                mRandom.nextInt((int) (mInnerBounds.width() - (2 * Style.itemStemNarrowBy)));

        if (mUser.above) {
            float finalTop =  mOuterBounds.bottom;
            float finalBottom = Style.layoutHalfPadding + mUser.centrelineOffset - mOuterMargin + 1;
            float startTop = finalBottom - finalTop;

            //mStemBounds = new RectF(offset, finalTop, offset + Style.lineWidth, finalTop);
            mStemBounds = new RectF(offset, finalTop, offset + Style.lineWidth, finalBottom);
            mStemStepChangeT = 0;
            mStemStepChangeB = -((finalTop - finalBottom) / Style.itemStemGrowSteps);
        } else {
            float finalTop = mUser.centrelineOffset + Style.lineWidth - 3;
            float finalBottom = mOuterBounds.top;
            float startBottom = finalBottom + (finalBottom - finalTop);

            mStemBounds = new RectF(offset, finalBottom, offset + Style.lineWidth, finalBottom);
            mStemBounds = new RectF(offset, finalTop, offset + Style.lineWidth, finalBottom);
            //mStemStepChangeT = (finalTop - finalBottom) / Style.itemStemGrowSteps;
            mStemStepChangeB = 0;
        }
        mStemRedraw = Style.itemStemGrowOver / Style.itemStemGrowSteps;


        return mInnerBounds;
    }

    protected void drawStem(Canvas canvas) {

    }

    public final void reassessBounds() {
        setBounds(mSetWidth, mSetHeight, mSetPadding);
    }

    public final User getUser() {
        return mUser;
    }

    public final int getItemId() {
        return mItemId;
    }

    protected final int getItemIndex() {
        return mItemIndex;
    }

    protected final void setItemIndex(int itemIndex) {
        mItemIndex = itemIndex;
    }

    public final String getUniqueItemId() { return mUser.globalUserId + "-" + mItemId; }

    protected final void setDateTime(long dateTime) { mDateTime = dateTime; }

    public final long getDateTime() { return mDateTime; }

    public final void simulateTap(Activity activity) {
        onTap(activity);
    }

    protected boolean onTap(Activity activity) {
        return onLongPress(activity);
    }

    protected boolean onTap() {
        return onTap(TimelineActivity.getInstance());
    }

    protected boolean onLongPress() {
        return onLongPress(TimelineActivity.getInstance());
    }

    protected abstract boolean onLongPress(Activity activity);

    private boolean mCancelLongPress = false;

    public final void cancelLongPress() {
        mCancelLongPress = true;
    }

    private static long LONG_PRESS_DELAY = 500;

    private class GestureListener extends GestureDetector.SimpleOnGestureListener {

        @Override
        public boolean onDoubleTap(MotionEvent e) {
            Log.d(TAG, "Double tap on Item[" + getUniqueItemId() + "]");
            CCLog.write(Event.ITEM_DOUBLE_TAP, "{uniqueItemId=" + getUniqueItemId() + "}");
            return true;
        }

        @Override
        public boolean onSingleTapConfirmed(MotionEvent e) {
            Log.d(TAG, "Single tap on Item[" + getUniqueItemId() + "]");
            CCLog.write(Event.ITEM_SINGLE_TAP, "{uniqueItemId=" + getUniqueItemId() + "}");

            return onTap();
        }
//
//        @Override
//        public boolean onTouchEvent(MotionEvent e) {
//            if(mCancelLongPress) {
//                mCancelLongPress = false;
//                return false;
//            }
//
//            if(e.getAction() == MotionEvent.ACTION_DOWN) {
//                handler.postDelayed(mLongPressed, LONG_PRESS_DELAY);
//                return true;
//            }
//
//            if(e.getAction() == MotionEvent.ACTION_MOVE
//                    || e.getAction() == MotionEvent.ACTION_UP
//                    || e.getAction() == MotionEvent.ACTION_SCROLL) {
//                handler.removeCallbacks(mLongPressed);
//            }
//            return false;
//        }
    }


    final Handler handler = new Handler();
    Runnable mLongPressed = new Runnable() {
        public void run() {
            //if(getUser().globalUserId == Instance.globalUserId) {
                Log.d(TAG, "Long press on Item[" + getUniqueItemId() + "]");
                CCLog.write(Event.ITEM_LONG_PRESS, "{uniqueItemId=" + getUniqueItemId() + "}");
                Item.this.onLongPress();
//            } else {
//                TimelineActivity.getInstance().promptAdd(getDrawnX() + mLongPressX);
//            }
        }
    };

    protected abstract void onSelect(Activity activity, boolean editable, boolean deletable);
}
