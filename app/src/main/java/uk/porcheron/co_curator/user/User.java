package uk.porcheron.co_curator.user;

import android.graphics.Paint;
import android.util.Log;

import java.util.Arrays;

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
    public float offset;
    public float centrelineOffset;
    public boolean above;
    public final Paint bgPaint;
    private boolean draw = false;

    public User(int globalUserId, int userId) {
        this.userId = userId;
        this.globalUserId = globalUserId;

        this.offset = Style.userOffsets[Instance.addedUsers];
        this.centrelineOffset = Style.userPositions[Instance.addedUsers] * (Style.lineWidth + Style.lineCentreGap);
        this.above = offset <= 0;

        this.bgColor = Instance.userId == userId ? Style.userMeBgColors[userId] : Style.userBgColors[userId];
        this.fgColor = Style.userFgColors[userId];

        Paint bgPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        bgPaint.setStyle(Paint.Style.FILL);
        bgPaint.setColor(bgColor);
        this.bgPaint = bgPaint;

        if(globalUserId == Instance.globalUserId) {
            draw = true;
        }

        Instance.addedUsers++;
    }

    void willDraw() {
        int drawn = Instance.drawnUsers;

        Log.e(TAG, "Will now also draw User[" + this.globalUserId + "], they are the " + Instance.drawnUsers + "th user to be drawn" );
        Log.e(TAG, Arrays.toString(Style.userOffsets));

        this.offset = Style.userOffsets[drawn];
        this.centrelineOffset = Style.userPositions[drawn] * (Style.lineWidth + Style.lineCentreGap);
        this.above = this.offset <= 0;
        this.draw = true;
    }

    void willUnDraw() {
        this.draw = false;
    }

    public boolean draw() {
        return this.draw;
    }
}