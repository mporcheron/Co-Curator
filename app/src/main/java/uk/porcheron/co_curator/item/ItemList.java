package uk.porcheron.co_curator.item;

import android.content.ContentValues;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;

import org.apache.http.NameValuePair;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONObject;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import uk.porcheron.co_curator.TimelineActivity;
import uk.porcheron.co_curator.collo.ClientManager;
import uk.porcheron.co_curator.collo.ColloDict;
import uk.porcheron.co_curator.collo.ResponseHandler;
import uk.porcheron.co_curator.collo.ResponseManager;
import uk.porcheron.co_curator.db.DbHelper;
import uk.porcheron.co_curator.db.TableItem;
import uk.porcheron.co_curator.db.WebLoader;
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
public class ItemList extends ArrayList<Item> implements ResponseHandler {
    private static final String TAG = "CC:ItemList";

    private DbHelper mDbHelper;

    private Map<String, Item> mItemIds = new HashMap<String, Item>();

    private HorizontalScrollView mScrollView;
    private LinearLayout mLayoutAbove;
    private LinearLayout mLayoutBelow;

    public ItemList(HorizontalScrollView scrollView, LinearLayout layoutAbove, LinearLayout layoutBelow) {
        mDbHelper = DbHelper.getInstance();
        mScrollView = scrollView;
        mLayoutAbove = layoutAbove;
        mLayoutBelow = layoutBelow;

        ResponseManager.registerHandler(ColloDict.ACTION_NEW, this);
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
            if(j.getDateTime() > dateTime) {
                break;
            }

            insertAt++;
            if(user.above && user.draw) {
                insertAtAbove++;
            } else if(user.draw) {
                insertAtBelow++;
            }
        }

        mItemIds.put(user.globalUserId + "-" + item.getItemId(), item);
        add(insertAt, item);


        // Drawing
        if(!user.draw) {
            Log.v(TAG, "Item[" + uniqueItemId + "]: Won't draw as user is not connected or us");
        } else {
            drawItem(user, item, user.above ? insertAtAbove : insertAtBelow);
        }

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

        Log.v(TAG, "Item[" + uniqueItemId + "]: Created in Db (rowId=" + newRowId + ")");

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

    private void drawItem(User user, Item item, int pos) {
        int minWidth = Phone.screenWidth;
        if (user.above) {
            mLayoutAbove.addView(item, Math.min(mLayoutAbove.getChildCount(), pos));
            minWidth = Math.max(mLayoutAbove.getWidth() + item.getMeasuredWidth(), minWidth);
        } else {
            mLayoutBelow.addView(item, Math.min(mLayoutBelow.getChildCount(), pos));
            minWidth = Math.max(mLayoutBelow.getWidth() + item.getMeasuredWidth(), minWidth);
        }

        mLayoutAbove.setMinimumWidth(minWidth);
        mLayoutBelow.setMinimumWidth(minWidth);

        float minAutoScrollWidth = minWidth - Style.autoscrollSlack - Phone.screenWidth;
        if (user.globalUserId == Instance.globalUserId || minAutoScrollWidth <= mScrollView.getScrollX()) {
            final int targetX = minWidth;
            mScrollView.postDelayed(new Runnable() {
                @Override
                public void run() {
                    mScrollView.smoothScrollTo(targetX, 0);
                }
            }, 500);
        }

        item.setDrawn(true);
    }

    public void retestDrawing() {
        int insertAtAbove = 0;
        int insertAtBelow = 0;
        for(Item item : this) {
            User user = item.getUser();

            if(user.draw) {
                if(!item.isDrawn()) {
                    drawItem(user, item, user.above ? insertAtAbove : insertAtBelow);
                    // draw
                }
            } else {
                if(item.isDrawn()) {
                    // remove from view
                }
            }

            if(user.draw) {
                if (user.above) {
                    insertAtAbove++;
                } else {
                    insertAtBelow++;
                }
            }

        }
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

    @Override
    public boolean respond(String action, int globalUserId, String... data) {
        try {
            WebLoader.loadItemFromWeb(globalUserId, Integer.parseInt(data[0]));
            return true;
        } catch (NumberFormatException e) {
            Log.e(TAG, "Invalid Ids received");
            return false;
        }
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
                Log.v(TAG, "Item successfully uploaded, notify clients");
                ClientManager.postMessage(ColloDict.ACTION_NEW, mItemId);
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
                    ClientManager.postMessage(ColloDict.ACTION_NEW, mItemId);
                    return true;
                }
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }

            return Boolean.FALSE;
        }
    }
}
