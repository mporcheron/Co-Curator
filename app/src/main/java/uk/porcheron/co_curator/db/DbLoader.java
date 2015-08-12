package uk.porcheron.co_curator.db;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.util.Log;

import uk.porcheron.co_curator.TimelineActivity;
import uk.porcheron.co_curator.item.ItemList;
import uk.porcheron.co_curator.item.ItemType;
import uk.porcheron.co_curator.user.User;
import uk.porcheron.co_curator.val.Instance;

/**
 * Class for loading items from the local and cloud databases.
 */
public class DbLoader extends AsyncTask<Void, Void, Boolean> {
    private static final String TAG = "CC:DbLoader";

    private TimelineActivity mActivity = TimelineActivity.getInstance();
    private DbHelper mDbHelper = DbHelper.getInstance();
    private WebLoader mWebLoader = new WebLoader();

    public DbLoader() {
    }

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
        if (Instance.items.isEmpty()) {
            mActivity.promptAdd();
        }
    }

    protected void loadUsersFromDb() throws Exception {
        Log.d(TAG, "Initial load of users from DB");

        try (SQLiteDatabase db = mDbHelper.getWritableDatabase()) {
            String[] projection = {
                    TableUser.COL_GLOBAL_USER_ID,
                    TableUser.COL_USER_ID,
            };

            String sortOrder =
                    TableUser.COL_USER_ID + " ASC";

            Cursor c = db.query(
                    TableUser.TABLE_NAME,
                    projection,
                    null,
                    null,
                    null,
                    null,
                    sortOrder
            );

            c.moveToFirst();
            for (int i = 0; i < c.getCount(); i++) {
                int cGlobalUserId = c.getInt(0);
                int cUserId = c.getInt(1);

                Instance.users.add(cGlobalUserId, cUserId, true);

                c.moveToNext();
            }
            c.close();
        }

        // Current user doesn't exist?
        if (Instance.user() == null) {
            Log.d(TAG, "Get users from cloud...");
            mWebLoader.loadUsersFromWeb();
        }
    }

    protected ItemList loadItemsFromDb(int globalUserId) throws Exception {
        Log.d(TAG, "Initial load of items from DB");

        boolean allUsers = globalUserId < 0;

        try (SQLiteDatabase db = mDbHelper.getReadableDatabase()) {
            String[] projection = {
                    TableItem.COL_ITEM_ID,
                    TableItem.COL_GLOBAL_USER_ID,
                    TableItem.COL_ITEM_TYPE,
                    TableItem.COL_ITEM_DATA
            };

            String selection = "";
            if (!allUsers) {
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
            for (int i = 0; i < c.getCount(); i++) {
                int cItemId = c.getInt(0);
                int cGlobalUserId = c.getInt(1);
                int cTypeId = c.getInt(2);

                user = Instance.users.getByGlobalUserId(cGlobalUserId);
                if (user == null) {
                    Instance.users.add(cGlobalUserId, Instance.users.size(), false);
                }

                type = ItemType.get(cTypeId);

                String cData = c.getString(3);

                if (cData != null) {
                    Log.v(TAG, "Save Item[" + i + "] (itemId=" + cItemId + ",type=" + type.toString() + ",data='" + cData + "')");
                    Instance.items.add(cItemId, type, user, cData, false, false);
                } else {
                    Log.e(TAG, "Error: Item[" + i + "] (itemId=" + cItemId + ",type=" + type.toString() + ") is NULL");
                }

                c.moveToNext();
            }

            c.close();
        }

        if (allUsers) {
            for (User u : Instance.users) {
                mWebLoader.loadItemsFromWeb(u.globalUserId);
            }
        } else {
            mWebLoader.loadItemsFromWeb(globalUserId);
        }

        return Instance.items;
    }
}
