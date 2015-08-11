package uk.porcheron.co_curator.user;

import android.graphics.Paint;
import android.text.TextPaint;

import uk.porcheron.co_curator.util.Style;

/**
 * Created by map on 08/08/15.
 */
public class User {
    private static final String TAG = "CC:User";

    public int userId;
    public int globalUserId;
    public int bgColor;
    public int fgColor;
    public float offset;
    public float centrelineOffset;
    public boolean above;
    public Paint bgPaint;

    public User(int globalUserId, int userId) {
        this.userId = userId;
        this.globalUserId = globalUserId;
        this.offset = Style.userOffsets[userId];

        int clOffset = 0;
        for(int pos : Style.userPositions) {
            if(pos == userId) {
                this.centrelineOffset = clOffset;
                break;
            }
            clOffset += Style.lineWidth + Style.lineCentreGap;
        }
        this.above = offset <= 0;

        this.bgColor = Style.userBgColors[userId];
        this.fgColor = Style.userFgColors[userId];

        Paint bgPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        bgPaint.setStyle(Paint.Style.FILL);
        bgPaint.setColor(bgColor);
        this.bgPaint = bgPaint;
    }
}