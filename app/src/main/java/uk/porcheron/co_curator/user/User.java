package uk.porcheron.co_curator.user;

import android.graphics.Paint;
import android.util.Log;

import uk.porcheron.co_curator.val.Instance;
import uk.porcheron.co_curator.val.Style;

/**
 * User and the relevant UI effects for the user.
 */
public class User {
    private static final String TAG = "CC:User";

    public final int userId;
    public final int globalUserId;
    public String ip = null;
    public final int bgColor;
    public final int fgColor;
    public final float offset;
    public final float centrelineOffset;
    public final boolean above;
    public final Paint bgPaint;

    public User(int globalUserId, int userId) {
        this.userId = userId;
        this.globalUserId = globalUserId;
        this.offset = Style.userOffsets[Instance.addedUsers];

        this.centrelineOffset = Instance.addedUsers * (Style.lineWidth + Style.lineCentreGap);
        this.above = offset <= 0;

        this.bgColor = Instance.userId == userId ? Style.userMeBgColors[userId] : Style.userBgColors[userId];
        this.fgColor = Style.userFgColors[userId];

        Paint bgPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        bgPaint.setStyle(Paint.Style.FILL);
        bgPaint.setColor(bgColor);
        this.bgPaint = bgPaint;

        Instance.addedUsers++;
    }
}