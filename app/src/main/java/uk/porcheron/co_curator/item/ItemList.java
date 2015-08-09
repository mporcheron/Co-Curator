package uk.porcheron.co_curator.item;

import android.content.Context;
import android.util.Log;
import android.widget.LinearLayout;

import java.util.ArrayList;

import uk.porcheron.co_curator.user.User;
import uk.porcheron.co_curator.util.Style;

/**
 * List of items
 *
 * Created by map on 07/08/15.
 */
public class ItemList extends ArrayList<ItemContainer> {
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

    public void add(int itemId, ItemType type, User user, String data) {
        Item item = null;
        if(type == ItemType.NOTE) {
            item = createNote(itemId, user, data);
        } else if(type == ItemType.URL) {
            item = createURL(itemId, user, data);
        }

        if(item == null) {
            Log.e(TAG, "Unsupported item type: " + type.getLabel());
            return;
        }

        boolean above = size() % 2 == 0;
        ItemContainer container = new ItemContainer(mContext, item, user, above);

        add(container);
        mLayoutCentre.addView(container.getNotch());
        if (above) {
            mLayoutAbove.addView(container);
        } else {
            mLayoutBelow.addView(container);
        }
    }

    private ItemNote createNote(int itemId, User user, String text) {
        ItemNote note = new ItemNote(mContext, itemId, user);
        note.setText(text);
        return note;
    }

    private ItemURL createURL(int itemId, User user, String url) {
        ItemURL note = new ItemURL(mContext, itemId, user);
        note.setURL(url);
        return note;
    }
}
