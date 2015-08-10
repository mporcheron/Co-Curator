package uk.porcheron.co_curator.item;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;
import android.widget.LinearLayout;

import java.util.ArrayList;

import uk.porcheron.co_curator.TimelineActivity;
import uk.porcheron.co_curator.db.DbHelper;
import uk.porcheron.co_curator.db.TableItem;
import uk.porcheron.co_curator.user.User;
import uk.porcheron.co_curator.user.UserList;
import uk.porcheron.co_curator.util.Style;

/**
 * List of items
 *
 * Created by map on 07/08/15.
 */
public class ItemList extends ArrayList<ItemContainer> {
    private static final String TAG = "CC:ItemList";

    private Context mContext;
    private DbHelper mDbHelper;

    private int mGlobalUserId;
    private UserList mUsers;

    private LinearLayout mLayoutAbove;
    private LinearLayout mLayoutCentre;
    private LinearLayout mLayoutBelow;

    public ItemList(TimelineActivity context, LinearLayout layoutAbove,  LinearLayout layoutCentre, LinearLayout layoutBelow) {
        mContext = context;
        mDbHelper = context.getDbHelper();
        mLayoutAbove = layoutAbove;
        mLayoutCentre = layoutCentre;
        mLayoutBelow = layoutBelow;
    }

    public boolean add(int itemId, ItemType type, User user, String data) {
        return add(itemId, type, user, data, false);
    }

    public boolean add(int itemId, ItemType type, User user, String data, boolean localOnly) {
        Log.d(TAG, "Add item " + itemId + " to the view");

        // Instance
        Item item = null;
        if(type == ItemType.NOTE) {
            item = createNote(itemId, user, data);
        } else if(type == ItemType.URL) {
            item = createURL(itemId, user, data);
        }

        if(item == null) {
            Log.e(TAG, "Unsupported item type: " + type.getLabel());
            return false;
        }

        boolean above = size() % 2 == 0;
        ItemContainer container = new ItemContainer(mContext, item, user, above);

        add(container);

        // Drawing
        mLayoutCentre.addView(container.getNotch());
        if (above) {
            mLayoutAbove.addView(container);
        } else {
            mLayoutBelow.addView(container);
        }

        // Local Database
        if(localOnly) {
            return true;
        }

        SQLiteDatabase db = mDbHelper.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(TableItem.COL_GLOBAL_USER_ID, user.globalUserId);
        values.put(TableItem.COL_ITEM_TYPE, type.getTypeId());
        values.put(TableItem.COL_ITEM_DATA, data);

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
