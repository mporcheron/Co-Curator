package uk.porcheron.co_curator.user;

import android.graphics.Paint;
import android.util.Log;

import java.util.Arrays;
import java.util.Comparator;

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
    private int position = 0;

    public User(int globalUserId, int userId) {
        this.userId = userId;
        this.globalUserId = globalUserId;

        this.bgColor = Instance.userId == userId ? Style.userMeBgColors[userId] : Style.userBgColors[userId];
        this.fgColor = Style.userFgColors[userId];

        Paint bgPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        bgPaint.setStyle(Paint.Style.FILL);
        bgPaint.setColor(bgColor);
        this.bgPaint = bgPaint;

        if(globalUserId == Instance.globalUserId) {
            willDraw();
        }

        Instance.addedUsers++;
    }

    void willDraw() {
        boolean[] positions = new boolean[4];
        for(User user : Instance.users) {
            if(user.draw) {
                positions[user.position] = true;
            }
        }

        int pos;
        for(pos = 0; pos < positions.length; pos++) {
            if(!positions[pos]) {
                break;
            }
        }

        this.position = pos;
        this.offset = Style.userOffsets[pos];
        this.centrelineOffset = Style.userPositions[pos] * (Style.lineWidth + Style.lineCentreGap);
        this.above = this.offset <= 0;
        this.draw = true;
    }

    void willUnDraw() {
        this.draw = false;
    }

    public boolean draw() {
        return this.draw;
    }

    @Override
    public boolean equals(Object another) {
        if(!(another instanceof User)) {
            return false;
        }

        User u = (User) another;
        return u.globalUserId == globalUserId && u.userId == userId;
    }
}