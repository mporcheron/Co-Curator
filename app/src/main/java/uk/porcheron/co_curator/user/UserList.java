package uk.porcheron.co_curator.user;

import android.graphics.Paint;
import android.view.SurfaceView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import uk.porcheron.co_curator.item.Item;

/**
 * Created by map on 07/08/15.
 */
public class UserList extends ArrayList<User> {
    private static final String TAG = "CC:UserList";

    private SurfaceView mSurface;
    private Map<Integer,User> mGlobalUserIds = new HashMap<Integer,User>();

    public UserList(SurfaceView surface) {
        mSurface = surface;
    }

    public User add(int globalUserId, int userId) {
        User user = new User(globalUserId, userId);
        add(userId, user);
        mGlobalUserIds.put(globalUserId, user);
        mSurface.invalidate();

        return user;
    }

    @Override
    public User remove(int userId) {
        User resp = remove(userId);
        if(resp != null) {
            mSurface.invalidate();
        }
        return resp;
    }

    public User getByGlobalUserId(int globalUserId) {
        return mGlobalUserIds.get(globalUserId);
    }


}
