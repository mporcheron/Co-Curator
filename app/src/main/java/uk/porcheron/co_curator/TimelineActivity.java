package uk.porcheron.co_curator;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.pm.ActivityInfo;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.SurfaceView;
import android.view.View;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.Toast;

import java.util.Random;

import uk.porcheron.co_curator.line.Centrelines;
import uk.porcheron.co_curator.item.ItemList;
import uk.porcheron.co_curator.item.ItemType;
import uk.porcheron.co_curator.user.User;
import uk.porcheron.co_curator.user.UserList;
import uk.porcheron.co_curator.util.Style;
import uk.porcheron.co_curator.util.TimelineContract;
import uk.porcheron.co_curator.util.TimelineDbHelper;

public class TimelineActivity extends Activity implements View.OnLongClickListener {
    private static final String TAG = "CC:TimelineActivity";

    private UserList mUsers;
    private ItemList mItems;

    private TimelineDbHelper mDbHelper;
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
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
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

        mLayoutAbove.setOnLongClickListener(this);
        mLayoutCentre.setOnLongClickListener(this);
        mLayoutBelow.setOnLongClickListener(this);

        mDbHelper = new TimelineDbHelper(this);

        AsyncTask dbLoad = new InitialLoadFromDb();
        dbLoad.execute("Go");
        //testing
//        User[] users = new User[5];
//        for(int i = 0; i < 5; i++) {
//            users[i] = mUsers.add();
//        }
//
//        Random r = new Random();
//        int i = 0;
//        for(int j = 0; j < r.nextInt(5) + 5; j++) {
//            for (int k = 0; k <  users.length; k++) {
//                User user = users[k];
//                for (int l = 0; l < r.nextInt(10) + 2; l++) {
//                    if (r.nextInt(2) == 0) {
//                        mItems.add(ItemType.NOTE, user, "User = " + k + "; Test = " + i);
//                    } else {
//                        mItems.add(ItemType.URL, user, "http://www.google.com");
//                    }
//                    i++;
//                }
//            }
//        }
    }

    @Override
    public boolean onLongClick(View v) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.dialog_add_message);

        if(!mItems.isEmpty()) {
            builder.setNegativeButton(R.string.dialog_add_negative, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    // User cancelled the dialog
                }
            });
        } else {
            final View v2 = v;
            builder.setOnCancelListener(new DialogInterface.OnCancelListener() {
                @Override
                public void onCancel(DialogInterface dialog) {
                    TimelineActivity.this.onLongClick(v2);
                }
            });
        }

        final ItemType[] types = ItemType.values();
        CharSequence[] typeLabels = new CharSequence[types.length];
        for(int i = 0; i < typeLabels.length; i++) {
            typeLabels[i] = getResources().getString(types[i].getLabel());
        }

        builder.setItems(typeLabels, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                ItemType type = types[which];
                switch (type) {
                    case PHOTO:
                        Toast.makeText(TimelineActivity.this, "Photo", Toast.LENGTH_SHORT).show();
                        break;

                    case NOTE:

                        break;

                    case URL:
                        Toast.makeText(TimelineActivity.this, "URL", Toast.LENGTH_SHORT).show();
                        break;
                }
            }
        });

        AlertDialog dialog = builder.create();
        dialog.show();

        // TODO Auto-generated method stub
        return true;
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
                loadItemsFromDb(1);
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
                onLongClick(mLayoutAbove);
            }
        }

        protected void loadUsersFromDb() throws Exception {
            SQLiteDatabase db = mDbHelper.getReadableDatabase();

            String[] projection = {
                    TimelineContract.UserEntry.COLUMN_NAME_GLOBAL_USER_ID,
                    TimelineContract.UserEntry.COLUMN_NAME_USER_ID,
            };

            String selection = "";

            String sortOrder =
                    TimelineContract.UserEntry.COLUMN_NAME_USER_ID + " DESC";

            Cursor c = db.query(
                    TimelineContract.UserEntry.TABLE_NAME,
                    projection,
                    selection,
                    null,
                    null,
                    null,
                    sortOrder
            );

            c.moveToFirst();
            while(c.moveToNext()) {
                mUsers.add(c.getInt(0), c.getInt(1));
            }
        }

        protected ItemList loadItemsFromDb(int globalUserId)  throws Exception {
            SQLiteDatabase db = mDbHelper.getReadableDatabase();

            boolean allUsers = globalUserId < 0;

            String[] projection = {
                    TimelineContract.ItemEntry.COLUMN_NAME_ITEM_ID,
                    TimelineContract.ItemEntry.COLUMN_NAME_GLOBAL_USER_ID,
                    TimelineContract.ItemEntry.COLUMN_NAME_ITEM_TYPE,
                    TimelineContract.ItemEntry.COLUMN_NAME_ITEM_DATA
            };

            String selection = "";
            if(!allUsers) {
                selection = TimelineContract.ItemEntry.COLUMN_NAME_GLOBAL_USER_ID + " = '" + globalUserId + "'";
            }

            String sortOrder =
                    TimelineContract.ItemEntry.COLUMN_NAME_ITEM_DATETIME + " DESC";

            Cursor c = db.query(
                    TimelineContract.ItemEntry.TABLE_NAME,
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
            while(c.moveToNext()) {
                user = mUsers.getByGlobalUserId(c.getInt(1));
                type = ItemType.get(2);

                mItems.add(c.getInt(0), type, user, c.getString(3));
            }

            return mItems;
        }
    }
}
