package uk.porcheron.co_curator;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.SurfaceView;
import android.view.View;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;

import java.io.FileNotFoundException;
import java.io.InputStream;

import uk.porcheron.co_curator.db.DbLoader;
import uk.porcheron.co_curator.item.NewItem;
import uk.porcheron.co_curator.line.Centrelines;
import uk.porcheron.co_curator.item.ItemList;
import uk.porcheron.co_curator.user.UserList;
import uk.porcheron.co_curator.util.Style;
import uk.porcheron.co_curator.db.DbHelper;
import uk.porcheron.co_curator.util.UData;

public class TimelineActivity extends Activity implements View.OnLongClickListener {
    private static final String TAG = "CC:TimelineActivity";

    private boolean mCreated = false;

    private DbHelper mDbHelper;
    private ProgressDialog mProgressDialog;
    private Centrelines mCentrelines;
    private SurfaceView mSurface;
    private HorizontalScrollView mScrollView;
    private LinearLayout mLayoutAbove;
    private LinearLayout mLayoutCentre;
    private LinearLayout mLayoutBelow;

    public static final int PICK_IMAGE = 101;

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

        mDbHelper = new DbHelper(this);
        UData.users = new UserList(this, mSurface);
        UData.items = new ItemList(this, mLayoutAbove, mLayoutCentre, mLayoutBelow);

        mCentrelines = new Centrelines();
        mSurface.getHolder().addCallback(mCentrelines);

        mLayoutAbove.setPadding(Style.layoutAbovePadX, 0, 0, 0);
        mLayoutCentre.setPadding(Style.layoutAbovePadX, 0, 0, 0);
        mLayoutBelow.setPadding(Style.layoutBelowPadX, 0, 0, 0);

        mSurface.setOnLongClickListener(this);
        mLayoutAbove.setOnLongClickListener(this);
        mLayoutCentre.setOnLongClickListener(this);
        mLayoutBelow.setOnLongClickListener(this);

        new DbLoader(this).execute("Go");

        //testing
//        User[] users = new User[5];
//            for(int i = 1; i < 5; i++) {
//                UData.users.add(i, i);
//        }
//
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
        //mProgressDialog.hide();
    }

    @Override
    public boolean onLongClick(View v) {
        NewItem.prompt(this, v, UData.items.isEmpty());
        return true;
    }

    public void finishLoading() {
        mProgressDialog.hide();
    }

    public void promptAdd() {
        onLongClick(mLayoutAbove);
    }

    public DbHelper getDbHelper() {
        return mDbHelper;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.d(TAG, "Received ActivityResult (requestCode=" + requestCode + ",resultCode=" + resultCode + ")");

        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE && resultCode == Activity.RESULT_OK) {
            if (data == null) {
                Log.e(TAG, "No data retrieved...");
                return;
            }

            try {
                Log.v(TAG, "Opening input stream of photo");

                Uri selectedImage = data.getData();
                String[] filePathColumn = { MediaStore.Images.Media.DATA };

                Cursor cursor = getContentResolver().query(selectedImage, filePathColumn, null, null, null);
                cursor.moveToFirst();

                int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
                String picturePath = cursor.getString(columnIndex);

                cursor.close();
//                InputStream inputStream = getContentResolver().openInputStream(data.getData());

                Log.v(TAG, "Decoding the stream into a bitmap");
                NewItem.newImage(BitmapFactory.decodeFile(picturePath));
            } catch(Exception e) {
                Log.e(TAG, "Error loading image: " + e.getMessage());
            }
            //Now you can do whatever you want with your inpustream, save it as file, upload to a server, decode a bitmap...
        }
    }
}
