package uk.porcheron.co_curator;

import android.app.Activity;
import android.content.pm.ActivityInfo;
import android.graphics.Paint;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.SurfaceView;
import android.widget.LinearLayout;

import java.util.ArrayList;
import java.util.List;

import uk.porcheron.co_curator.centreline.CentrelineHandler;
import uk.porcheron.co_curator.item.Item;
import uk.porcheron.co_curator.item.ItemList;
import uk.porcheron.co_curator.item.ItemType;
import uk.porcheron.co_curator.item.NoteItem;
import uk.porcheron.co_curator.user.User;
import uk.porcheron.co_curator.user.UserList;
import uk.porcheron.co_curator.util.Style;

public class TimelimeActivity extends Activity {
    private static final String TAG = "CC:TimelineActivity";

    private UserList mUsers;
    private ItemList mItems;

    private CentrelineHandler mCentrelineHandler;
    private SurfaceView mSurface;
    private LinearLayout mLayoutAbove;
    private LinearLayout mLayoutBelow;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        setContentView(R.layout.activity_timelime);

        Style.loadStyleAttrs(this);

        mSurface = (SurfaceView) findViewById(R.id.surface);
        mLayoutAbove = (LinearLayout) findViewById(R.id.layoutAboveCentre);
        mLayoutBelow = (LinearLayout) findViewById(R.id.layoutBelowCentre);

        mUsers = new UserList(mSurface);
        mItems = new ItemList(this, mLayoutAbove, mLayoutBelow);

        mCentrelineHandler = new CentrelineHandler(mUsers);
        mSurface.getHolder().addCallback(mCentrelineHandler);
        mLayoutAbove.setPadding(Style.layoutAbovePadX, 0, 0, 0);
        mLayoutBelow.setPadding(Style.layoutBelowPadX, 0, 0, 0);

        //testing
        User user0 = mUsers.add(0, Style.userColours[0], 0);
        mItems.add(ItemType.NOTE, user0, "testing1");
        mItems.add(ItemType.NOTE, user0, "testing2");
        mItems.add(ItemType.NOTE, user0, "testing3");
        mItems.add(ItemType.NOTE, user0, "testing4");
        mItems.add(ItemType.NOTE, user0, "testing5");
        mItems.add(ItemType.NOTE, user0, "testing6");
        mItems.add(ItemType.NOTE, user0, "testing7");
        mItems.add(ItemType.NOTE, user0, "testing1");
        mItems.add(ItemType.NOTE, user0, "testing2");
        mItems.add(ItemType.NOTE, user0, "testing3");
        mItems.add(ItemType.NOTE, user0, "testing4");
        mItems.add(ItemType.NOTE, user0, "testing5");
        mItems.add(ItemType.NOTE, user0, "testing6");
        mItems.add(ItemType.NOTE, user0, "testing7");
        mItems.add(ItemType.NOTE, user0, "testing1");
        mItems.add(ItemType.NOTE, user0, "testing2");
        mItems.add(ItemType.NOTE, user0, "testing3");
        mItems.add(ItemType.NOTE, user0, "testing4");
        mItems.add(ItemType.NOTE, user0, "testing5");
        mItems.add(ItemType.NOTE, user0, "testing6");
        mItems.add(ItemType.NOTE, user0, "testing7");
    }
    
}
