package uk.porcheron.co_curator.item;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Paint;
import android.graphics.Point;
import android.view.Display;
import android.view.ViewGroup;
import android.view.WindowManager;

import uk.porcheron.co_curator.R;

/**
 * Created by map on 08/08/15.
 */
public class Style {

    protected static float mOffsetY = 0.0f;

    protected static Paint mLinePaint;
    protected static int mLineColour;
    protected static float mLineWidth = 0.0f;

    protected static float mNotchHeight = 0.0f;

    protected static float mItemScale = 0.0f;
    protected static int mItemWidth = 0;
    protected static int mItemHeight = 0;
    protected static int mItemPadX = 0;
    protected static int mItemShadowOffset = 0;
    protected static int mItemShadowSize = 0;

    protected static int mItemFullWidth = 0;
    protected static int mItemFullHeight = 0;

    public static int mItemPadY = 0;
    public static int mLayoutAbovePadX = 0;
    public static int mLayoutBelowPadX = 0;

    protected static int mNoteLines;
    protected static float mNotePadding = 0.0f;
    protected static int mNoteFontSize;
    protected static float mNoteLineSpacing = 0.0f;
    protected static int mNoteFg;
    protected static int mNoteSh;
    protected static int mNoteBg;

    protected static ViewGroup.LayoutParams mLayoutParams;

    public static void loadStyleAttrs(Context context) {
        Resources res = context.getResources();

        // Screen dimensions
        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        Display display = wm.getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        int width = size.x;
        int height = size.y;

        // Get style settings
        //mOffsetY = res.getDimension(R.dimen.offsetY, 0.0f);

        mLineColour = res.getColor(R.color.timelineA);
        mLineWidth = res.getDimension(R.dimen.lineWidth);

        mNotchHeight = res.getDimension(R.dimen.notchHeight);

        mItemScale = res.getInteger(R.integer.itemScale) / 10f;

        float midY = (height / 2);
        float maxItemSize = (height - mLineWidth) / 2;
        float defItemSize = maxItemSize * mItemScale;
        float defItemPad = maxItemSize * ((1 - mItemScale) / 2);

        mItemWidth = (int) defItemSize;
        mItemHeight = (int) defItemSize;
        mItemPadY =  (int) (midY - mItemHeight) / 2;
        mItemPadX = (int) defItemPad;

        mItemFullWidth = mItemWidth + mItemPadX;
        mItemFullHeight = mItemHeight;

        mLayoutAbovePadX = mItemPadX;
        mLayoutBelowPadX = mItemFullWidth / 2;

        mNoteLines = res.getInteger(R.integer.noteLines);
        mNotePadding = res.getDimension(R.dimen.notePadding);
        mNoteFontSize = res.getInteger(R.integer.noteFontSize);
        mNoteLineSpacing = res.getInteger(R.integer.noteLineSpacing) / 10f;

        mItemShadowOffset = (int) res.getDimension(R.dimen.itemShadowOffset);
        mItemShadowSize = (int) res.getDimension(R.dimen.itemShadowSize);
        mNoteBg = res.getColor(R.color.noteBg);
        mNoteSh = res.getColor(R.color.noteSh);
        mNoteFg = res.getColor(R.color.noteFg);
    }

}
