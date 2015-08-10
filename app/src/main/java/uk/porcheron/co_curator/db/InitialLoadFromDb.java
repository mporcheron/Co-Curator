package uk.porcheron.co_curator.db;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.util.Log;

import uk.porcheron.co_curator.TimelineActivity;
import uk.porcheron.co_curator.item.ItemList;
import uk.porcheron.co_curator.item.ItemType;
import uk.porcheron.co_curator.user.User;
import uk.porcheron.co_curator.user.UserList;

/**
 * Created by map on 10/08/15.
 */
public class InitialLoadFromDb extends AsyncTask<Object, Void, Boolean> {
    private static final String TAG = "CC:InitialDB";

    private TimelineActivity mActivity;
    private DbHelper mDbHelper;

    private int mGlobalUserId;
    private UserList mUsers;
    private ItemList mItems;

    public InitialLoadFromDb(TimelineActivity activity) {
        mActivity = activity;
        mDbHelper = activity.getDbHelper();
        mGlobalUserId = activity.getGlobalUserId();
        mUsers = activity.getUserList();
        mItems = activity.getItemList();
    }

    /**
     * @param params If first is > 0, returns that user.
     * @return
     */
    @Override
    protected Boolean doInBackground(Object... params) {
        try {
            loadUsersFromDb();
            loadItemsFromDb(-1);
        } catch (Exception e) {
            Log.e(TAG, "Error loading data from local DB: " + e.getMessage());
            return Boolean.FALSE;
        }
        return Boolean.TRUE;
    }

    @Override
    protected void onPreExecute() {
    }

    @Override
    protected void onProgressUpdate(Void... values) {
    }

    @Override
    protected void onPostExecute(Boolean result) {
        //mActivity.mProgressDialog.hide();
        if (!result.booleanValue()) {
            //NewItem.prompt(TimelineActivity.this, mLayoutAbove, true);
        }
    }

    protected void loadUsersFromDb() throws Exception {
        Log.d(TAG, "Initial load of users from DB");

        SQLiteDatabase db = mDbHelper.getWritableDatabase();

        try {
            String[] projection = {
                    TableUser.COL_GLOBAL_USER_ID,
                    TableUser.COL_USER_ID,
            };

            String selection = null;

            String sortOrder =
                    TableUser.COL_USER_ID + " ASC";

            Cursor c = db.query(
                    TableUser.TABLE_NAME,
                    projection,
                    selection,
                    null,
                    null,
                    null,
                    sortOrder
            );

            c.moveToFirst();
            int i = 0;
            for (i = 0; i < c.getCount(); i++) {
                int cGlobalUserId = c.getInt(0);
                int cUserId = c.getInt(1);

                mUsers.add(cGlobalUserId, cUserId, true);

                c.moveToNext();
            }
            c.close();
        } finally {
            db.close();
        }

        // Current user doesn't exist?
        if(mUsers.getByGlobalUserId(mGlobalUserId) == null) {
            mUsers.add(mGlobalUserId, mGlobalUserId-1, false);
        }
    }

    protected ItemList loadItemsFromDb(int globalUserId) throws Exception {
        Log.d(TAG, "Initial load of items from DB");

        SQLiteDatabase db = mDbHelper.getReadableDatabase();
        try {
            boolean allUsers = globalUserId < 0;

            String[] projection = {
                    TableItem.COL_ITEM_ID,
                    TableItem.COL_GLOBAL_USER_ID,
                    TableItem.COL_ITEM_TYPE,
                    TableItem.COL_ITEM_DATA
            };

            String selection = "";
                if(!allUsers) {
                    selection = TableItem.COL_GLOBAL_USER_ID + " = '" + globalUserId + "'";
                }

            String sortOrder =
                    TableItem.COL_ITEM_DATETIME + " ASC";

            Cursor c = db.query(
                    TableItem.TABLE_NAME,
                    projection,
                    selection,
                    null,
                    null,
                    null,
                    sortOrder
            );

            User user;
            ItemType type;

            c.moveToFirst();
            int i = 0;
            for (i = 0; i < c.getCount(); i++) {
                int cItemId = c.getInt(0);
                int cGlobalUserId = c.getInt(1);
                int cTypeId = c.getInt(2);
                String cData = c.getString(3);

                user = mUsers.getByGlobalUserId(cGlobalUserId);
                if (user == null) {
                    mUsers.add(cGlobalUserId, mUsers.size(), false);
                    Log.e(TAG, "Could not find user with globalUserId " + cGlobalUserId + ", creating");
                    continue;
                }

                type = ItemType.get(cTypeId);

                Log.v(TAG, "Save Item[" + i + "] with itemId = " + cItemId + ", data = '" + cData + "'");
                mItems.add(c.getInt(0), type, user, cData, true);

                c.moveToNext();
            }

            Log.d(TAG, i + " items loaded from DB");

            c.close();
        } finally {
            db.close();
        }

        return mItems;
    }
}
