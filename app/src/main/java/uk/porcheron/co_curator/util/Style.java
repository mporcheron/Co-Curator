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

    public static float layoutCentreHeight;

    // deprecated
    public static float notchHeight;

    public static int[] userPositions = new int[4];
    public static int[] userBgColors = new int[4];
    public static int[] userFgColors = new int[4];
    public static int[] userOffsets = new int[4];

    public static float lineCentreGap;
    public static float lineWidth;

    // deprecated
    public static int layoutAbovePadX;
    public static int layoutBelowPadX;

    public static float itemXGapMin;
    public static float itemXGapMax;
    public static float itemXGapOffset;
    public static float itemOutlineSize;
    public static int itemRoundedCorners;
    public static float itemStemNarrowBy;
    public static float itemFullHeight;

    public static float imageWidth;
    public static float imageHeight;
    public static float imagePadding;

    public static float urlWidth;
    public static float urlHeight;
    public static float urlPadding;

    public static float noteWidth;
    public static float noteHeight;
    public static float notePadding;
    public static int noteLines;
    public static int noteFontSize;
    public static float noteLineSpacing;

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
        layoutCentreHeight = res.getDimension(R.dimen.layoutCentreHeight);

        userPositions = res.getIntArray(R.array.userPositions);

        userBgColors[0] = res.getColor(R.color.userBg0);
        userBgColors[1] = res.getColor(R.color.userBg1);
        userBgColors[2] = res.getColor(R.color.userBg2);
        userBgColors[3] = res.getColor(R.color.userBg3);

        userFgColors[0] = res.getColor(R.color.userFg0);
        userFgColors[1] = res.getColor(R.color.userFg1);
        userFgColors[2] = res.getColor(R.color.userFg2);
        userFgColors[3] = res.getColor(R.color.userFg3);

        userOffsets = res.getIntArray(R.array.userOffsets);

        lineCentreGap = res.getDimension(R.dimen.lineCentreGap);
        lineWidth = res.getDimension(R.dimen.lineWidth);


        // deprecated
        notchHeight = res.getDimension(R.dimen.notchHeight);

        itemXGapMin = res.getDimension(R.dimen.itemXGapMin);
        itemXGapMax = res.getDimension(R.dimen.itemXGapMax);
        itemXGapOffset = itemXGapMax - itemXGapMin;
        itemOutlineSize = res.getDimension(R.dimen.itemOutlineSize);
        itemRoundedCorners = res.getInteger(R.integer.itemRoundedCorners);
        itemStemNarrowBy = res.getDimension(R.dimen.itemStemNarrowBy);
        itemFullHeight = ((height / 2) - (res.getDimension(R.dimen.layoutCentreHeight) / 2));

        imageWidth = res.getDimension(R.dimen.imageWidth);
        imageHeight = res.getDimension(R.dimen.imageHeight);
        imagePadding = res.getDimension(R.dimen.imagePadding);

        urlWidth = res.getDimension(R.dimen.urlWidth);
        urlHeight = res.getDimension(R.dimen.urlHeight);
        urlPadding = res.getDimension(R.dimen.urlPadding);

        noteWidth = res.getDimension(R.dimen.noteWidth);
        noteHeight = res.getDimension(R.dimen.noteHeight);
        notePadding = res.getDimension(R.dimen.notePadding);
        noteLines = res.getInteger(R.integer.noteLines);
        noteFontSize = res.getInteger(R.integer.noteFontSize);
        noteLineSpacing  = res.getInteger(R.integer.noteLineSpacing) / 10f;
    }
}
