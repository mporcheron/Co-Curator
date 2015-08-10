package uk.porcheron.co_curator;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.SurfaceView;
import android.view.View;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;

import uk.porcheron.co_curator.db.InitialLoadFromDb;
import uk.porcheron.co_curator.db.TableItem;
import uk.porcheron.co_curator.item.NewItem;
import uk.porcheron.co_curator.item.NewItemCreator;
import uk.porcheron.co_curator.line.Centrelines;
import uk.porcheron.co_curator.item.ItemList;
import uk.porcheron.co_curator.item.ItemType;
import uk.porcheron.co_curator.user.User;
import uk.porcheron.co_curator.user.UserList;
import uk.porcheron.co_curator.util.Style;
import uk.porcheron.co_curator.db.DbHelper;

public class TimelineActivity extends Activity implements View.OnLongClickListener {
    private static final String TAG = "CC:TimelineActivity";

    private boolean mCreated = false;
    private int mGlobalUserId = 2;

    private User mUser;
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

        //mProgressDialog = ProgressDialog.show(this, "", getText(R.string.dialog_loading), true);

        Style.loadStyleAttrs(this);

        mSurface = (SurfaceView) findViewById(R.id.surface);
        mScrollView = (HorizontalScrollView) findViewById(R.id.horizontalScrollView);
        mLayoutAbove = (LinearLayout) findViewById(R.id.layoutAboveCentre);
        mLayoutCentre = (LinearLayout) findViewById(R.id.layoutCentre);
        mLayoutBelow = (LinearLayout) findViewById(R.id.layoutBelowCentre);

        mDbHelper = new DbHelper(this);
        mUsers = new UserList(this, mSurface);
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

        NewItem.setLists(mGlobalUserId, mUsers, mItems);
        new InitialLoadFromDb(this).execute("Go");

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
        NewItem.prompt(this, v, mItems.isEmpty());
        return true;
    }

    public User getUser() {
        return mUsers.getByGlobalUserId(mGlobalUserId);
    }

    public int getGlobalUserId() {
        return mGlobalUserId;
    }

    public DbHelper getDbHelper() {
        return mDbHelper;
    }

    public UserList getUserList() {
        return mUsers;
    }

    public ItemList getItemList() {
        return mItems;
    }

}
