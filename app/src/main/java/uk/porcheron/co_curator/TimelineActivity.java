package uk.porcheron.co_curator;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.pm.ActivityInfo;
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

public class TimelineActivity extends Activity implements View.OnLongClickListener {
    private static final String TAG = "CC:TimelineActivity";

    private UserList mUsers;
    private ItemList mItems;

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

        //testing
        User[] users = new User[5];
        for(int i = 0; i < 5; i++) {
            users[i] = mUsers.add();
        }

        Random r = new Random();
        int i = 0;
        for(int j = 0; j < r.nextInt(5) + 5; j++) {
            for (int k = 0; k <  users.length; k++) {
                User user = users[k];
                for (int l = 0; l < r.nextInt(10) + 2; l++) {
                    if (r.nextInt(2) == 0) {
                        mItems.add(ItemType.NOTE, user, "User = " + k + "; Test = " + i);
                    } else {
                        mItems.add(ItemType.URL, user, "http://www.google.com");
                    }
                    i++;
                }
            }
        }
    }

    @Override
    public boolean onLongClick(View v) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.dialog_add_message);

        builder.setNegativeButton(R.string.dialog_add_negative, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User cancelled the dialog
            }
        });

        final ItemType[] types = ItemType.values();
        CharSequence[] typeLabels = new CharSequence[types.length];
        for(int i = 0; i < typeLabels.length; i++) {
            typeLabels[i] = getResources().getString(types[i].mLabel);
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
    
}
