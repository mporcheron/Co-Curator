package uk.porcheron.co_curator.item;

import android.content.Context;
import android.util.Log;
import android.widget.LinearLayout;

import java.util.ArrayList;

import uk.porcheron.co_curator.user.User;

/**
 * List of items
 *
 * Created by map on 07/08/15.
 */
public class ItemList extends ArrayList<Item> {
    private static final String TAG = "CC:ItemList";

    private Context mContext;
    private LinearLayout mLayoutAbove;
    private LinearLayout mLayoutCentre;
    private LinearLayout mLayoutBelow;

    public ItemList(Context context, LinearLayout layoutAbove,  LinearLayout layoutCentre, LinearLayout layoutBelow) {
        mContext = context;
        mLayoutAbove = layoutAbove;
        mLayoutCentre = layoutCentre;
        mLayoutBelow = layoutBelow;
    }

    public void add(ItemType type, User user, String data) {
        boolean above = size() % 2 == 0;

        Item item = null;
        if(type == ItemType.NOTE) {
            item = createNote(data, user, above);
        } else if(type == ItemType.URL) {
            item = createURL(data, user, above);
        }

        if(item == null) {
            Log.e(TAG, "Unsupported item type: " + type.mLabel);
            return;
        }

        add(item);
        mLayoutCentre.addView(item.mNotch);
        if (above) {
            mLayoutAbove.addView(item);
        } else {
            mLayoutBelow.addView(item);
        }
    }

    private ItemNote createNote(String text, User user, boolean above) {
        ItemNote note = new ItemNote(mContext, user, above);
        note.setText(text);
        return note;
    }

    private ItemURL createURL(String url, User user, boolean above) {
        ItemURL note = new ItemURL(mContext, user, above);
        note.setURL(url);
        return note;
    }
}
