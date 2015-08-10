package uk.porcheron.co_curator;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.pm.ActivityInfo;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.SurfaceView;
import android.view.View;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;
import android.widget.Toast;

import java.util.Random;

import uk.porcheron.co_curator.db.TableItem;
import uk.porcheron.co_curator.db.TableUser;
import uk.porcheron.co_curator.item.NewItem;
import uk.porcheron.co_curator.item.NewItemCreator;
import uk.porcheron.co_curator.line.Centrelines;
import uk.porcheron.co_curator.item.ItemList;
import uk.porcheron.co_curator.item.ItemType;
import uk.porcheron.co_curator.user.User;
import uk.porcheron.co_curator.user.UserList;
import uk.porcheron.co_curator.util.Style;
import uk.porcheron.co_curator.db.DbHelper;

public class TimelineActivity extends Activity implements View.OnLongClickListener, NewItemCreator {
    private static final String TAG = "CC:TimelineActivity";

    private boolean mCreated = false;
    private int mGlobalUserId = 1;

    private UserList mUsers;
    private ItemList mItems;

    private DbHelper mDbHelper;
    private ProgressDialog mProgressDialog;
    private Centrelines mCentrelines;
    private SurfaceView mSurface;
    private HorizontalScrollView mScrollView;
    private LinearLayout mLayoutAbove;
    private LinearLayout mLayoutCentre;
    private LinearLayout mLayoutBelow;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if(mCreated) {
            return;
        }
        mCreated = true;

        //setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        setContentView(R.layout.activity_timelime);

        mProgressDialog = ProgressDialog.show(this, "", getText(R.string.dialog_loading), true);

        Style.loadStyleAttrs(this);

        mSurface = (SurfaceView) findViewById(R.id.surface);
        mScrollView = (HorizontalScrollView) findViewById(R.id.horizontalScrollView);
        mLayoutAbove = (LinearLayout) findViewById(R.id.layoutAboveCentre);
        mLayoutCentre = (LinearLayout) findViewById(R.id.layoutCentre);
        mLayoutBelow = (LinearLayout) findViewById(R.id.layoutBelowCentre);

        mUsers = new UserList(mSurface);
        mItems = new ItemList(this, mLayoutAbove, mLayoutCentre, mLayoutBelow);

        mCentrelines = new Centrelines(mUsers);
        mSurface.getHolder().addCallback(mCentrelines);

        mLayoutAbove.setPadding(Style.layoutAbovePadX, 0, 0, 0);
        mLayoutCentre.setPadding(Style.layoutAbovePadX, 0, 0, 0);
        mLayoutBelow.setPadding(Style.layoutBelowPadX, 0, 0, 0);

        mSurface.setOnLongClickListener(this);
        mLayoutAbove.setOnLongClickListener(this);
        mLayoutCentre.setOnLongClickListener(this);
        mLayoutBelow.setOnLongClickListener(this);

        mDbHelper = new DbHelper(this);

        if(mItems.isEmpty()) {
            AsyncTask dbLoad = new InitialLoadFromDb();
            dbLoad.execute("Go");
        }

        //testing
//        User[] users = new User[1];
//        for(int i = 0; i < users.length; i++) {
//            users[i] = mUsers.add(i, i);
//        }

//        Random r = new Random();
//        int i = 0;
//        for(int j = 0; j < r.nextInt(5) + 5; j++) {
//            for (int k = 0; k <  users.length; k++) {
//                User user = users[k];
//                for (int l = 0; l < r.nextInt(10) + 2; l++) {
//                    if (r.nextInt(2) == 0) {
//                        mItems.add(i++,ItemType.NOTE,  user, "User = " + k + "; Test = " + i);
//                    } else {
//                        mItems.add(i++,ItemType.URL, user, "http://www.google.com");
//                    }
//                }
//            }
//        }
//        mProgressDialog.hide();
    }

    @Override
    public boolean onLongClick(View v) {
        NewItem.prompt(this, v, this, mItems.isEmpty());
        return true;
    }

    @Override
    public boolean newNote(String text) {

        SQLiteDatabase db = mDbHelper.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(TableItem.COL_GLOBAL_USER_ID, mGlobalUserId);
        values.put(TableItem.COL_ITEM_TYPE, ItemType.NOTE.getTypeId());
        values.put(TableItem.COL_ITEM_DATA, text);

        long newRowId;
        newRowId = db.insert(
                TableItem.TABLE_NAME,
                null,
                values);

        db.close();

        if(newRowId >= 0) {
            Log.d(TAG, "Note " + newRowId + " created in DB");
            return true;
        } else {
            Log.e(TAG, "Could not create note in DB");
            return false;
        }
    }

    @Override
    public boolean newPhoto() {
        return false;
    }

    @Override
    public boolean newURL(String url) {
        return false;
    }

    private class InitialLoadFromDb extends AsyncTask<Object, Void, Boolean> {

        /**
         * @param params If first is > 0, returns that user.
         * @return
         */
        @Override
        protected Boolean doInBackground(Object... params) {
            try {
                loadUsersFromDb();
                loadItemsFromDb(mGlobalUserId);
            } catch(Exception e) {
                return Boolean.FALSE;
            }
            return Boolean.TRUE;
        }

        @Override
        protected void onPreExecute() {}

        @Override
        protected void onProgressUpdate(Void... values) {}

        @Override
        protected void onPostExecute(Boolean result) {
            mProgressDialog.hide();
            if(!result.booleanValue()) {
                //NewItem.prompt(TimelineActivity.this, mLayoutAbove, true);
            }
        }

        protected void loadUsersFromDb() throws Exception {
            Log.d(TAG, "Initial load of user from DB");

            SQLiteDatabase db = mDbHelper.getWritableDatabase();

            try {
                String[] projection = {
                        TableUser.COL_GLOBAL_USER_ID,
                        TableUser.COL_USER_ID,
                };

                String selection = "";

                String sortOrder =
                        TableUser.COL_USER_ID + " DESC";

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
                while (c.moveToNext()) {
                    mUsers.add(c.getInt(0), c.getInt(1));
                }
                c.close();
            } finally {
                db.close();
            }
        }

        protected ItemList loadItemsFromDb(int globalUserId)  throws Exception {
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

                Log.v(TAG, "a");

                String selection = "";
//                if(!allUsers) {
//                    selection = TableItem.COL_GLOBAL_USER_ID + " = '" + globalUserId + "'";
//                }

                String sortOrder =
                        TableItem.COL_ITEM_DATETIME + " DESC";

                Cursor c = db.query(
                        TableItem.TABLE_NAME,
                        projection,
                        selection,
                        null,
                        null,
                        null,
                        sortOrder
                );

                Log.v(TAG, "b=" + c.getCount());
                User user;
                ItemType type;

                int n = c.getCount();

                int i = 0;
                c.moveToFirst();
                while(c.moveToNext()) {
                    user = mUsers.getByGlobalUserId(c.getInt(1));
                    type = ItemType.get(2);

                    i++;
                    mItems.add(c.getInt(0), type, user, c.getString(3));
                    Log.v(TAG, "Add item");
                }

                Log.d(TAG, i + " items loaded from DB");

                c.close();
            } finally {
                db.close();
            }

            return mItems;
        }
    }
}
