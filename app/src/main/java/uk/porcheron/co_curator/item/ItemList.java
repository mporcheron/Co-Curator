package uk.porcheron.co_curator.item;

import android.content.ContentValues;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Canvas;
import android.graphics.Point;
import android.graphics.RectF;
import android.os.AsyncTask;
import android.util.Log;
import android.view.Display;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import uk.porcheron.co_curator.TimelineActivity;
import uk.porcheron.co_curator.db.DbHelper;
import uk.porcheron.co_curator.db.TableItem;
import uk.porcheron.co_curator.user.User;
import uk.porcheron.co_curator.util.Style;
import uk.porcheron.co_curator.util.IData;
import uk.porcheron.co_curator.util.Web;

/**
 * List of items
 *
 * Created by map on 07/08/15.
 */
public class ItemList extends ArrayList<Item> {
    private static final String TAG = "CC:ItemList";

    private TimelineActivity mActivity;
    private DbHelper mDbHelper;

    private Map<String,Item> mItemIds = new HashMap<String,Item>();

    private LinearLayout mLayoutAbove;
    private LinearLayout mLayoutBelow;

    private int mWidthAbove = 0;
    private int mWidthBelow = 0;

    public ItemList(TimelineActivity activity, LinearLayout layoutAbove, LinearLayout layoutBelow) {
        mActivity = activity;
        mDbHelper = activity.getDbHelper();
        mLayoutAbove = layoutAbove;
        mLayoutBelow = layoutBelow;
    }

    public boolean add(int itemId, ItemType type, User user, String data, boolean addToLocalDb, boolean addToCloud) {
        Log.d(TAG, "Add item " + itemId + " to the view (addToLocalDb=" + addToLocalDb + ",addToCloud=" + addToCloud + ")");

        // Create the item
        Item item = null;
        if(type == ItemType.NOTE) {
            item = createNote(itemId, user, data);
        } else if(type == ItemType.URL) {
            item = createURL(itemId, user, data);
        } else if(type == ItemType.PHOTO && data instanceof String) {
            item = createImage(itemId, user, data);
        }

        if(item == null) {
            Log.e(TAG, "Unsupported item type: " + type.getLabel());
            return false;
        }

        add(item);
        mItemIds.put(user.globalUserId + "-" + item.getItemId(), item);

        // Drawing
        int slotX = 0;
        if (user.above) {
            mLayoutAbove.addView(item, item.getWidth(), (int) Style.itemFullHeight);
            mWidthAbove += item.getSlotBounds().width();
        } else {
            mLayoutBelow.addView(item, item.getWidth(), (int) Style.itemFullHeight);
            mWidthBelow += item.getSlotBounds().width();
        }

        Display display = mActivity.getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);

        // Save to the Local Database or just draw?
        if(!addToLocalDb) {
            return true;
        }

        SQLiteDatabase db = mDbHelper.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(TableItem.COL_GLOBAL_USER_ID, user.globalUserId);
        values.put(TableItem.COL_ITEM_ID, itemId);
        values.put(TableItem.COL_ITEM_TYPE, type.getTypeId());
        values.put(TableItem.COL_ITEM_DATA, data.toString());

        long newRowId;
        newRowId = db.insert(
                TableItem.TABLE_NAME,
                null,
                values);

        db.close();

        if(newRowId < 0) {
            Log.e(TAG, "Could not create item in DB");
            return false;
        }

        Log.d(TAG, "Item " + newRowId + " created in DB");

        // Save to the Local Database or just draw?
        if(addToCloud) {
            new PostTextToCloud(user.globalUserId, item.getItemId(), type).execute(data.toString());
        }
        return true;
    }

    private ItemNote createNote(int itemId, User user, String text) {
        ItemNote note = new ItemNote(mActivity, itemId, user);
        note.setText(text);
        return note;
    }

    private ItemURL createURL(int itemId, User user, String url) {
        ItemURL note = new ItemURL(mActivity, itemId, user);
        note.setURL(url);
        return note;
    }

    private ItemImage createImage(int itemId, User user, String imagePath) {
        ItemImage image = new ItemImage(mActivity, itemId, user);
        image.setImagePath(imagePath);
        return image;
    }

    public Item getByItemId(int globalUserId, int itemId) {
        return mItemIds.get(globalUserId + "-" + itemId);
    }

    private class PostTextToCloud extends AsyncTask<String,Void,Boolean> {

        private int mGlobalUserId;
        private int mItemId;
        private ItemType mItemType;

        PostTextToCloud(int globalUserId, int itemId, ItemType itemType) {
            mGlobalUserId = globalUserId;
            mItemId = itemId;
            mItemType = itemType;
        }

        @Override
        protected Boolean doInBackground(String... params) {
            List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(2);
            nameValuePairs.add(new BasicNameValuePair("globalUserId", "" + mGlobalUserId));
            nameValuePairs.add(new BasicNameValuePair("itemId", "" + mItemId));
            nameValuePairs.add(new BasicNameValuePair("itemType", "" + mItemType.getTypeId()));
            nameValuePairs.add(new BasicNameValuePair("itemData", "" + params[0]));

            JSONObject obj = Web.requestObj(Web.POST_ITEMS, nameValuePairs);
            return obj.has("success");
        }
    }
}
