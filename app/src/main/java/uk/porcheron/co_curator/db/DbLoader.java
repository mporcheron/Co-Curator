package uk.porcheron.co_curator.db;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.util.Log;

import uk.porcheron.co_curator.TimelineActivity;
import uk.porcheron.co_curator.item.ItemList;
import uk.porcheron.co_curator.item.ItemType;
import uk.porcheron.co_curator.item.NewItem;
import uk.porcheron.co_curator.user.User;
import uk.porcheron.co_curator.user.UserList;
import uk.porcheron.co_curator.util.UData;

/**
 * Created by map on 10/08/15.
 */
public class DbLoader extends AsyncTask<Object, Void, Boolean> {
    private static final String TAG = "CC:InitialDB";

    private TimelineActivity mActivity;
    private DbHelper mDbHelper;

    public DbLoader(TimelineActivity activity) {
        mActivity = activity;
        mDbHelper = activity.getDbHelper();
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
        mActivity.finishLoading();
        if (UData.items.isEmpty()) {
            mActivity.promptAdd();
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

                UData.users.add(cGlobalUserId, cUserId, true);

                c.moveToNext();
            }
            c.close();
        } finally {
            db.close();
        }

        // Current user doesn't exist?
        if(UData.user() == null) {
            Log.d(TAG, "Create current user in DB");
            UData.users.add(UData.globalUserId, UData.userId, false);
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

                user = UData.users.getByGlobalUserId(cGlobalUserId);
                if (user == null) {
                    UData.users.add(cGlobalUserId, UData.users.size(), false);
                }

                type = ItemType.get(cTypeId);

                String cData = c.getString(3);

                if(cData != null) {
                    Log.v(TAG, "Save Item[" + i + "] (itemId=" + cItemId + ",type=" + type.toString() + ",data='" + cData + "')");
                    UData.items.add(cItemId, type, user, cData, true);
                } else {
                    Log.e(TAG, "Error: Item[" + i + "] (itemId=" + cItemId + ",type=" + type.toString() + ") is NULL");
                }

                c.moveToNext();
            }

            Log.d(TAG, i + " items loaded from DB");

            c.close();
        } finally {
            db.close();
        }

        return UData.items;
    }
}
