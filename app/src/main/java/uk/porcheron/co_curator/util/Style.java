package uk.porcheron.co_curator.util;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Point;
import android.view.Display;
import android.view.WindowManager;

import uk.porcheron.co_curator.R;

/**
 * Style properties for the application.
 */
public class Style {
    private static final String TAG = "CC:Style";

    public static int backgroundColor;

    public static int layoutCentreHeight;
    public static float notchHeight;

    public static int[] userColors = new int[6];
    public static float userOffset;

    public static float lineWidth;

    public static int layoutAbovePadX;
    public static int layoutBelowPadX;

    public static float itemScale;
    public static int itemWidth;
    public static int itemHeight;
    public static int itemPadX;
    public static int itemPadY;
    public static int itemFullWidth;
    public static int itemFullHeight;

    public static int noteLines;
    public static float notePadding = 0.0f;
    public static int noteFontSize;
    public static float noteLineSpacing = 0.0f;
    public static int noteShadowOffset = 0;
    public static int noteShadowSize = 0;
    public static int noteBg;
    public static int noteSh;
    public static int noteFg;
    public static int urlBg;

    public static void loadStyleAttrs(Context context) {
        Resources res = context.getResources();

        // Screen dimensions
        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        Display display = wm.getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        int height = size.y;

        // Get style settings
        backgroundColor = res.getColor(R.color.background);

        userColors[0] = res.getColor(R.color.user0);
        userColors[1] = res.getColor(R.color.user1);
        userColors[2] = res.getColor(R.color.user2);
        userColors[3] = res.getColor(R.color.user3);
        userColors[4] = res.getColor(R.color.user4);
        userColors[5] = res.getColor(R.color.user5);

        layoutCentreHeight = (int) res.getDimension(R.dimen.layoutCentreHeight);
        notchHeight = res.getDimension(R.dimen.notchHeight);

        lineWidth = res.getDimension(R.dimen.lineWidth);
        userOffset = res.getDimension(R.dimen.userOffset);

        itemScale = res.getInteger(R.integer.itemScale) / 100f;

        float midY = (height / 2);
        float maxItemSize = (height - lineWidth) / 2;
        float defItemSize = maxItemSize * itemScale;
        float defItemPad = maxItemSize * ((1 - itemScale) / 2);

        itemWidth = (int) defItemSize;
        itemHeight = (int) defItemSize;
        itemFullWidth = itemWidth + itemPadX;
        itemFullHeight = (int) (midY - (layoutCentreHeight / 2));

        itemPadX = (int) defItemPad;
        itemPadY = (itemFullHeight - itemHeight) / 2;

        layoutAbovePadX = itemPadX;
        layoutBelowPadX = itemPadX + (itemFullWidth / 2);

        noteLines = res.getInteger(R.integer.noteLines);
        notePadding = res.getDimension(R.dimen.notePadding);
        noteFontSize = res.getInteger(R.integer.noteFontSize);
        noteLineSpacing = res.getInteger(R.integer.noteLineSpacing) / 10f;
        noteShadowOffset = (int) res.getDimension(R.dimen.itemShadowOffset);
        noteShadowSize = (int) res.getDimension(R.dimen.itemShadowSize);
        noteBg = res.getColor(R.color.noteBg);
        noteSh = res.getColor(R.color.noteSh);
        noteFg = res.getColor(R.color.noteFg);
        urlBg = res.getColor(R.color.urlBg);

    }
}
