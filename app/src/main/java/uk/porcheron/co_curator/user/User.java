package uk.porcheron.co_curator.user;

import android.graphics.Paint;
import android.text.TextPaint;

import uk.porcheron.co_curator.util.Style;

/**
 * Created by map on 08/08/15.
 */
public class User {
    private static final String TAG = "CC:User";

    public final int userId;
    public final int globalUserId;
    public final int bgColor;
    public final int fgColor;
    public final float offset;
    public final float centrelineOffset;
    public final boolean above;
    public final Paint bgPaint;

    public User(int globalUserId, int userId) {
        this.userId = userId;
        this.globalUserId = globalUserId;
        this.offset = Style.userOffsets[userId];

        int clOffset = 0, selectedOffset = 0;
        for(int pos : Style.userPositions) {
            if(pos == userId) {
                selectedOffset = clOffset;
                break;
            }
            clOffset += Style.lineWidth + Style.lineCentreGap;
        }
        this.centrelineOffset = selectedOffset;
        this.above = offset <= 0;

        this.bgColor = Style.userBgColors[userId];
        this.fgColor = Style.userFgColors[userId];

        Paint bgPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        bgPaint.setStyle(Paint.Style.FILL);
        bgPaint.setColor(bgColor);
        this.bgPaint = bgPaint;
    }
}