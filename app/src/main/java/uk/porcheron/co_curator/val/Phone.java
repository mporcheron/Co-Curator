package uk.porcheron.co_curator.val;

import android.graphics.Point;
import android.view.Display;

import uk.porcheron.co_curator.TimelineActivity;
import uk.porcheron.co_curator.util.CCLog;
import uk.porcheron.co_curator.util.Event;

/**
 * Data about the current phone.
 */
public class Phone {
    public static int screenWidth;
    public static int screenHeight;

    public static void collectAttrs() {
        Display display = TimelineActivity.getInstance().getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        screenWidth = size.x;
        screenHeight = size.y;

        CCLog.write(Event.APP_PHONE_ATTRS, "{screenWidth=" + screenWidth + ",screenHeight=" + screenHeight + "}");
    }
}
