package uk.porcheron.co_curator.user;

import android.content.ContentValues;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;
import android.util.SparseArray;

import java.util.ArrayList;

import uk.porcheron.co_curator.db.DbHelper;
import uk.porcheron.co_curator.db.TableUser;

/**
 * List of users within the user's group.
 */
public class UserList extends ArrayList<User> {
    private static final String TAG = "CC:UserList";

    private DbHelper mDbHelper;

    private SparseArray<User> mGlobalUserIds = new SparseArray<>();

    public UserList() {
        mDbHelper = DbHelper.getInstance();
    }

    public User add(int globalUserId, int userId, boolean localOnly) {
        Log.d(TAG, "Add User (globalUserId=" + globalUserId + ",userId=" + userId + ")");

        User user = new User(globalUserId, userId);
        add(user);
        mGlobalUserIds.put(globalUserId, user);

        // Local Database
        if(localOnly) {
            Log.v(TAG, "User not created in DB, as requested");
            return user;
        }

        SQLiteDatabase db = mDbHelper.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(TableUser.COL_GLOBAL_USER_ID, globalUserId);
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

    public User getByGlobalUserId(int globalUserId) {
        return mGlobalUserIds.get(globalUserId);
    }


}
