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

    public User add(int globalUserId, boolean addToLocalDb) {
        return add(globalUserId, size(), addToLocalDb);
    }

    public User add(int globalUserId, int userId, boolean addToLocalDb) {
        Log.v(TAG, "User[" + globalUserId + "]: Add to List (userId=" + userId + ",addToLocalDb=" + addToLocalDb + ")");

        User user = new User(globalUserId, userId);
        add(user);
        mGlobalUserIds.put(globalUserId, user);

        // Local Database
        if(!addToLocalDb) {
            Log.v(TAG, "User[" + globalUserId + "] not created in DB, as requested");
            return user;
        }

        SQLiteDatabase db = mDbHelper.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(TableUser.COL_GLOBAL_USER_ID, globalUserId);
        values.put(TableUser.COL_USER_ID, user.globalUserId);

        long newRowId;
        newRowId = db.insert(
                TableUser.TABLE_NAME,
                null,
                values);

        db.close();

        if(newRowId >= 0) {
            Log.d(TAG, "User[" + globalUserId + "]: Created in Db (rowId=" + newRowId + ")");
        } else {
            Log.d(TAG, "User[" + globalUserId + "]: Could not create in Db");
        }

        return user;
    }

    public User getByGlobalUserId(int globalUserId) {
        return mGlobalUserIds.get(globalUserId);
    }


}
