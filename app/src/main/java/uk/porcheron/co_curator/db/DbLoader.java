package uk.porcheron.co_curator.db;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.util.Log;

import uk.porcheron.co_curator.TimelineActivity;
import uk.porcheron.co_curator.item.ItemList;
import uk.porcheron.co_curator.item.ItemType;
import uk.porcheron.co_curator.user.User;
import uk.porcheron.co_curator.util.IData;

/**
 * Created by map on 10/08/15.
 */
public class DbLoader extends AsyncTask<Void, Void, Boolean> {
    private static final String TAG = "CC:DbLoader";

    private TimelineActivity mActivity;
    private DbHelper mDbHelper;
    private WebLoader mWebLoader;

    public DbLoader(TimelineActivity activity) {
        mActivity = activity;
        mDbHelper = activity.getDbHelper();
        mWebLoader = new WebLoader(mActivity);
    }

    /**
     * @param params If first is > 0, returns that user.
     * @return
     */
    @Override
    protected Boolean doInBackground(Void... params) {
        try {
            loadUsersFromDb();
            loadItemsFromDb(-1);
        } catch (Exception e) {
            Log.e(TAG, "Error loading data: " + e.getMessage());
            return Boolean.FALSE;
        }
        return Boolean.TRUE;
    }

    @Override
    protected void onPostExecute(Boolean result) {
        mActivity.hideLoadingDialog();
        if (IData.items.isEmpty()) {
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

                IData.users.add(cGlobalUserId, cUserId, true);

                c.moveToNext();
            }
            c.close();
        } finally {
            db.close();
        }

        // Current user doesn't exist?
        if(IData.user() == null) {
            Log.d(TAG, "Get users from cloud...");
            mWebLoader.loadUsersFromWeb();
        }
    }

    protected ItemList loadItemsFromDb(int globalUserId) throws Exception {
        Log.d(TAG, "Initial load of items from DB");

        boolean allUsers = globalUserId < 0;

        SQLiteDatabase db = mDbHelper.getReadableDatabase();
        try {
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

                user = IData.users.getByGlobalUserId(cGlobalUserId);
                if (user == null) {
                    IData.users.add(cGlobalUserId, IData.users.size(), false);
                }

                type = ItemType.get(cTypeId);

                String cData = c.getString(3);

                if(cData != null) {
                    Log.v(TAG, "Save Item[" + i + "] (itemId=" + cItemId + ",type=" + type.toString() + ",data='" + cData + "')");
                    IData.items.add(cItemId, type, user, cData, false, false);
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

        if(allUsers) {
            for(User u : IData.users) {
                mWebLoader.loadItemsFromWeb(u.globalUserId);
            }
        } else {
            mWebLoader.loadItemsFromWeb(globalUserId);
        }

        return IData.items;
    }
}
