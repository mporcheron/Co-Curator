package uk.porcheron.co_curator;

import android.app.Activity;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.widget.LinearLayout;

import java.util.List;

import uk.porcheron.co_curator.item.Item;
import uk.porcheron.co_curator.item.ItemList;
import uk.porcheron.co_curator.item.ItemType;
import uk.porcheron.co_curator.item.NoteItem;
import uk.porcheron.co_curator.item.Style;

public class TimelimeActivity extends Activity {

    private static final String TAG = "CC:TimelineActivity";

    private List<Item> mItems = new ItemList();
    private LinearLayout mLayoutAbove;
    private LinearLayout mLayoutBelow;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        setContentView(R.layout.activity_timelime);

        Style.loadStyleAttrs(this);

        mLayoutAbove = (LinearLayout) findViewById(R.id.layoutAboveCentre);
        mLayoutBelow = (LinearLayout) findViewById(R.id.layoutBelowCentre);

        mLayoutAbove.setPadding(Style.mLayoutAbovePadX, Style.mItemPadY, 0, Style.mItemPadY);
        mLayoutBelow.setPadding(Style.mLayoutBelowPadX, Style.mItemPadY, 0, Style.mItemPadY);

        //testing
        addItem(ItemType.NOTE, "testing1");
        addItem(ItemType.NOTE, "testing2");
        addItem(ItemType.NOTE, "testing3");
        addItem(ItemType.NOTE, "testing4");
        addItem(ItemType.NOTE, "testing5");
        addItem(ItemType.NOTE, "testing6");
        addItem(ItemType.NOTE, "testing7");
        addItem(ItemType.NOTE, "testing1");
        addItem(ItemType.NOTE, "testing2");
        addItem(ItemType.NOTE, "testing3");
        addItem(ItemType.NOTE, "testing4");
        addItem(ItemType.NOTE, "testing5");
        addItem(ItemType.NOTE, "testing6");
        addItem(ItemType.NOTE, "testing7");
        addItem(ItemType.NOTE, "testing1");
        addItem(ItemType.NOTE, "testing2");
        addItem(ItemType.NOTE, "testing3");
        addItem(ItemType.NOTE, "testing4");
        addItem(ItemType.NOTE, "testing5");
        addItem(ItemType.NOTE, "testing6");
        addItem(ItemType.NOTE, "testing7");
    }

    /**
     * @param type Type of item
     * @param data Resource information
     */
    public void addItem(ItemType type, String data) {
        Item item = null;
        if(type == ItemType.NOTE) {
            item = createNote(data);
        } else {
            //...
        }

        if(item == null) {
            Log.e(TAG, "Unsupported item type: " + type.mLabel);
            return;
        }

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        params.weight = 1.0f;

        mItems.add(item);
        if (mItems.size() % 2 == 1) {
            params.gravity = Gravity.BOTTOM;
            item.setLayoutParams(params);
            mLayoutAbove.addView(item);
        } else {
            params.gravity = Gravity.TOP;
            item.setLayoutParams(params);
            mLayoutBelow.addView(item);
        }
    }

    private NoteItem createNote(String text) {
        NoteItem note = new NoteItem(this);
        note.setText(text);
        return note;
    }


}
