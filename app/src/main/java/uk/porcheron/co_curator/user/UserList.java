package uk.porcheron.co_curator.user;

import android.graphics.Paint;
import android.view.SurfaceView;

import java.util.ArrayList;

import uk.porcheron.co_curator.item.Item;

/**
 * Created by map on 07/08/15.
 */
public class UserList extends ArrayList<User> {

    private SurfaceView mSurface;

    public UserList(SurfaceView surface) {
        mSurface = surface;
    }

    public User add() {
        return add(size());
    }

    public User add(int userId) {
        User user = new User(userId);
        add(userId, user);
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


}
