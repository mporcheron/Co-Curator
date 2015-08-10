package uk.porcheron.co_curator.user;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Paint;
import android.util.Log;
import android.view.SurfaceView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import uk.porcheron.co_curator.TimelineActivity;
import uk.porcheron.co_curator.db.DbHelper;
import uk.porcheron.co_curator.db.TableItem;
import uk.porcheron.co_curator.db.TableUser;
import uk.porcheron.co_curator.item.Item;

/**
 * Created by map on 07/08/15.
 */
public class UserList extends ArrayList<User> {
    private static final String TAG = "CC:UserList";

    private TimelineActivity mActivity;
    private DbHelper mDbHelper;

    private int mGlobalUserId;
    private UserList mUsers;

    private SurfaceView mSurface;
    private Map<Integer,User> mGlobalUserIds = new HashMap<Integer,User>();

    public UserList(TimelineActivity activity, SurfaceView surface) {
        mSurface = surface;
        mActivity = activity;
        mDbHelper = activity.getDbHelper();
    }

    public User add(int globalUserId, int userId) {
        return add(globalUserId, userId, false);
    }

    public User add(int globalUserId, int userId, boolean localOnly) {
        Log.d(TAG, "Add User (globalUserId = " + globalUserId + ", userId = " + userId + ")");

        User user = new User(globalUserId, userId);
        add(userId, user);
        mGlobalUserIds.put(globalUserId, user);

        mSurface.invalidate();

        // Local Database
        if(localOnly) {
            return user;
        }

        SQLiteDatabase db = mDbHelper.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(TableUser.COL_GLOBAL_USER_ID, user.globalUserId);
        values.put(TableUser.COL_USER_ID, userId);

        long newRowId;
        newRowId = db.insert(
                TableUser.TABLE_NAME,
                null,
                values);

        db.close();

        if(newRowId >= 0) {
            Log.d(TAG, "User " + newRowId + " created in DB");
        } else {
            Log.e(TAG, "Could not create user in DB");
        }

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
