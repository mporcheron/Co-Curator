package uk.porcheron.co_curator.item;

import android.content.ContentValues;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.LinearLayout;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.UnsupportedEncodingException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import uk.porcheron.co_curator.TimelineActivity;
import uk.porcheron.co_curator.collo.ClientManager;
import uk.porcheron.co_curator.db.DbHelper;
import uk.porcheron.co_curator.db.TableItem;
import uk.porcheron.co_curator.user.User;
import uk.porcheron.co_curator.val.Instance;
import uk.porcheron.co_curator.val.Phone;
import uk.porcheron.co_curator.val.Style;
import uk.porcheron.co_curator.util.Web;

/**
 * List of items
 * <p/>
 * Created by map on 07/08/15.
 */
public class ItemList extends ArrayList<Item> {
    private static final String TAG = "CC:ItemList";

    private DbHelper mDbHelper;

    private Map<String, Item> mItemIds = new HashMap<String, Item>();

    private LinearLayout mLayoutAbove;
    private LinearLayout mLayoutBelow;

    public ItemList(LinearLayout layoutAbove, LinearLayout layoutBelow) {
        mDbHelper = DbHelper.getInstance();
        mLayoutAbove = layoutAbove;
        mLayoutBelow = layoutBelow;
    }

    public boolean add(ItemType type, User user, String data, boolean addToLocalDb, boolean addToCloud) {
        return add(size(), type, user, data, addToLocalDb, addToCloud);
    }

    public boolean add(int itemId, ItemType type, User user, String data, boolean addToLocalDb, boolean addToCloud) {
        return add(itemId, type, user, data, (int) (System.currentTimeMillis() / 1000L), addToLocalDb, addToCloud);
    }

    public synchronized boolean add(int itemId, ItemType type, User user, String data, int dateTime, boolean addToLocalDb, boolean addToCloud) {
        String uniqueItemId = user.globalUserId + "-" + itemId;

        Log.v(TAG, "Item[" + uniqueItemId + "]: Add to List (type=" + type + ",user=" + user.globalUserId +
                ",data=" + data + ",dateTime=" + dateTime + ",addToLocalDb=" + addToLocalDb +
                ",addToCloud=" + addToCloud + ")");

        // Create the item
        Item item = null;
        if (type == ItemType.NOTE) {
            item = createNote(itemId, user, dateTime, data);
        } else if (type == ItemType.URL) {
            item = createURL(itemId, user, dateTime, data);
        } else if (type == ItemType.PHOTO) {
            item = createImage(itemId, user, dateTime, data);
        }

        if (item == null) {
            Log.e(TAG, "Item[" + uniqueItemId + "]: Unsupported type: " + type.getLabel());
            return false;
        }

        int insertAt = 0, insertAtAbove = 0, insertAtBelow = 0;
        Iterator<Item> it = iterator();
        while(it.hasNext()) {
            Item j = it.next();
            if(j.getDateTime() < dateTime) {
                Log.d(TAG, "New item is newer, stop counting");
                break;
            }

            insertAt++;
            if(user.above) {
                insertAtAbove++;
            } else {
                insertAtBelow++;
            }
        }

        mItemIds.put(user.globalUserId + "-" + item.getItemId(), item);
        add(insertAt, item);

        int minWidth = Phone.screenWidth;

        // Drawing
        if (user.above) {
            mLayoutAbove.addView(item, Math.min(mLayoutAbove.getChildCount(), insertAtAbove));
            minWidth = Math.max(mLayoutAbove.getWidth(), minWidth);
        } else {
            mLayoutBelow.addView(item, Math.min(mLayoutBelow.getChildCount(), insertAtBelow));
            minWidth = Math.max(mLayoutBelow.getWidth(), minWidth);
        }

        mLayoutAbove.setMinimumWidth(minWidth);
        mLayoutBelow.setMinimumWidth(minWidth);

        // Save to the Local Database or just draw?
        if (!addToLocalDb) {
            return true;
        }

        SQLiteDatabase db = mDbHelper.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(TableItem.COL_GLOBAL_USER_ID, user.globalUserId);
        values.put(TableItem.COL_ITEM_ID, itemId);
        values.put(TableItem.COL_ITEM_TYPE, type.getTypeId());
        values.put(TableItem.COL_ITEM_DATA, data);
        values.put(TableItem.COL_ITEM_DATETIME, dateTime);

        long newRowId;
        newRowId = db.insert(
                TableItem.TABLE_NAME,
                null,
                values);

        db.close();

        if (newRowId < 0) {
            Log.e(TAG, "Item[" + uniqueItemId + "]: Could not create in Db");
            return false;
        }

        Log.d(TAG, "Item[" + uniqueItemId + "]: Created in Db (rowId=" + newRowId + ")");

        // Save to the Local Database or just draw?
        if (addToCloud) {
            if(type == ItemType.PHOTO) {
                new PostImageToCloud(user.globalUserId, item.getItemId(), type, dateTime).execute(data);
            } else {
                new PostTextToCloud(user.globalUserId, item.getItemId(), type, dateTime).execute(data);
            }
        }

        return true;
    }

    private ItemNote createNote(int itemId, User user, int dateTime, String text) {
        ItemNote note = new ItemNote(itemId, user, dateTime);
        note.setText(text);
        return note;
    }

    private ItemURL createURL(int itemId, User user, int dateTime, String url) {
        ItemURL note = new ItemURL(itemId, user, dateTime);
        note.setURL(url);
        return note;
    }

    private ItemImage createImage(int itemId, User user, int dateTime, String imagePath) {
        ItemImage image = new ItemImage(itemId, user, dateTime);
        image.setImagePath(imagePath);
        return image;
    }

    public Item getByItemId(int globalUserId, int itemId) {
        return mItemIds.get(globalUserId + "-" + itemId);
    }

    private class PostTextToCloud extends AsyncTask<String, Void, Boolean> {

        private int mGlobalUserId;
        private int mItemId;
        private ItemType mItemType;
        private int mDateTime;

        PostTextToCloud(int globalUserId, int itemId, ItemType itemType, int dateTime) {
            mGlobalUserId = globalUserId;
            mItemId = itemId;
            mItemType = itemType;
            mDateTime = dateTime;
        }

        @Override
        protected Boolean doInBackground(String... params) {
            List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(2);
            nameValuePairs.add(new BasicNameValuePair("globalUserId", "" + mGlobalUserId));
            nameValuePairs.add(new BasicNameValuePair("itemId", "" + mItemId));
            nameValuePairs.add(new BasicNameValuePair("itemType", "" + mItemType.getTypeId()));
            nameValuePairs.add(new BasicNameValuePair("itemData", "" + params[0]));
            nameValuePairs.add(new BasicNameValuePair("itemDateTime", "" + mDateTime));

            JSONObject obj = Web.requestObj(Web.POST_ITEMS, nameValuePairs);
            if(obj != null && obj.has("success")) {
                Log.d(TAG, "Item successfully uploaded, notify clients");
                ClientManager.postMessage("newitem|" + Instance.globalUserId + "|"+ mItemId);
                return true;
            } else {
                Log.e(TAG, "Could not post item to cloud");
            }

            return Boolean.FALSE;
        }
    }

    private class PostImageToCloud extends AsyncTask<String, Void, Boolean> {

        private int mGlobalUserId;
        private int mItemId;
        private ItemType mItemType;
        private int mDateTime;

        PostImageToCloud(int globalUserId, int itemId, ItemType itemType, int dateTime) {
            mGlobalUserId = globalUserId;
            mItemId = itemId;
            mItemType = itemType;
            mDateTime = dateTime;
        }

        @Override
        protected Boolean doInBackground(String... params) {
            MultipartEntity entity = new MultipartEntity();

            try {
                entity.addPart("globalUserId", new StringBody("" + mGlobalUserId));
                entity.addPart("itemId", new StringBody("" + mItemId));
                entity.addPart("itemType", new StringBody("" + mItemType.getTypeId()));
                entity.addPart("itemDateTime", new StringBody("" + mDateTime));

                File file = TimelineActivity.getInstance().getFileStreamPath(params[0] + ".png");
                entity.addPart("itemData", new FileBody(file));


                JSONObject obj = Web.requestObj(Web.POST_ITEMS, entity);
                if(obj != null && obj.has("success")) {
                    ClientManager.postMessage("newitem|" + Instance.globalUserId + "|"+ mItemId);
                    return true;
                }
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }

            return Boolean.FALSE;
        }
    }
}
