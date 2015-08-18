package uk.porcheron.co_curator.db;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.List;

import uk.porcheron.co_curator.TimelineActivity;
import uk.porcheron.co_curator.item.Item;
import uk.porcheron.co_curator.item.ItemImage;
import uk.porcheron.co_curator.item.ItemList;
import uk.porcheron.co_curator.item.ItemType;
import uk.porcheron.co_curator.item.ItemURL;
import uk.porcheron.co_curator.user.User;
import uk.porcheron.co_curator.util.SoUtils;
import uk.porcheron.co_curator.val.Collo;
import uk.porcheron.co_curator.val.Instance;
import uk.porcheron.co_curator.util.Image;
import uk.porcheron.co_curator.util.Web;
import uk.porcheron.co_curator.val.Phone;
import uk.porcheron.co_curator.val.Style;

/**
 * Utilities for loading resources from the web.
 */
public class WebLoader {
    private static final String TAG = "CC:WebLoader";

    public static void loadUsersFromWeb() {
        Log.d(TAG, "Load users from cloud");

        List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(2);
        nameValuePairs.add(new BasicNameValuePair("globalUserId", "" + Instance.globalUserId));
        nameValuePairs.add(new BasicNameValuePair("groupId", "" + Instance.groupId));
        nameValuePairs.add(new BasicNameValuePair("ip", SoUtils.getIPAddress(true)));

        JSONArray response = Web.requestArr(Web.GET_USERS, nameValuePairs);
        if (response != null) {
            Log.v(TAG, "Received " + response.length() + " users");

            for (int i = 0; i < response.length(); i++) {
                try {
                    JSONObject userJ = (JSONObject) response.get(i);
                    int gId = userJ.getInt("globalUserId");
                    int uId = userJ.getInt("userId");
                    String gIp = userJ.getString("ip");

                    User u = Instance.users.getByGlobalUserId(gId);
                    if(u == null) {
                        u = Instance.users.add(userJ.getInt("globalUserId"), uId, true);
                    }

                    Log.v(TAG, "User[" + gId + "] is at " + gIp);
                    u.ip = gIp;
                } catch (JSONException e) {
                    Log.e(TAG, "Could not get user from the cloud");
                    e.printStackTrace();
                }
            }
        } else {
            Log.e(TAG, "Null response loading users");
        }

    }

    protected static void loadItemsFromWeb(final int globalUserId) {
        Log.d(TAG, "Load items from cloud for User[" + globalUserId + "]");

        TimelineActivity activity = TimelineActivity.getInstance();

        List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(2);
        nameValuePairs.add(new BasicNameValuePair("globalUserId", "" + globalUserId));

        JSONArray response = Web.requestArr(Web.GET_ITEMS, nameValuePairs);
        if (response != null) {
            for (int i = 0; i < response.length(); i++) {
                try {
                    processItem(globalUserId, (JSONObject) response.get(i));
                } catch (JSONException e) {
                    Log.e(TAG, "Could not process item");
                    e.printStackTrace();
                }
            }
        }
    }

    public static void loadItemFromWeb(final int globalUserId, final int itemId) {
        Log.d(TAG, "Get Item[" + globalUserId + ":" + itemId + "] from cloud");

        List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(2);
        nameValuePairs.add(new BasicNameValuePair("globalUserId", "" + globalUserId));
        nameValuePairs.add(new BasicNameValuePair("itemId", "" + itemId));

        JSONObject response = Web.requestObj(Web.GET_ITEM, nameValuePairs);
        if (response != null) {
            try {
                processItem(globalUserId, response);
            } catch (JSONException e) {
                Log.e(TAG, "Could not process Item[" + itemId + "] from the cloud");
                e.printStackTrace();
            }
        } else {
            Log.e(TAG, "Could not get Item[" + itemId + "] from the cloud");
        }
    }

    private static void processItem(final int globalUserId, JSONObject response) throws JSONException {
        TimelineActivity activity = TimelineActivity.getInstance();

        final int itemId = response.getInt("id");
        final ItemType type = ItemType.get(response.getInt("type"));
        final boolean deleted = response.getInt("deleted") == TableItem.VAL_ITEM_DELETED;
        String nonFinalData = response.getString("data");

        boolean contains = Instance.items.containsByItemId(globalUserId, itemId, true);
        if (!contains) {
            final User user = Instance.users.getByGlobalUserId(globalUserId);

            if (type == ItemType.PHOTO) {
                String url = Web.IMAGE_DIR + nonFinalData;

                nonFinalData = ItemImage.urlToFile(url, globalUserId);

                if(nonFinalData == null) {
                    Log.e(TAG, "Failed to download image " + nonFinalData);
                    return;
                }
            }

            final String data = nonFinalData;
            final int dateTime = response.getInt("dateTime");

            Instance.items.registerForthcomingItem(globalUserId, itemId);
            activity.runOnUiThread(new Runnable() {
                public void run() {
                    Instance.items.add(itemId, type, user, data, dateTime, deleted, true, false);
                }
            });
        } else {
            Log.e(TAG, "Item already exists, update it");

            final String data = nonFinalData;
            activity.runOnUiThread(new Runnable() {
                public void run() {
                    final Item item = Instance.items.getByItemId(globalUserId, itemId);
                    if (deleted && !item.isDeleted()) {
                        Instance.items.remove(item, true, false, false);
                    }
                    if (type != ItemType.PHOTO && !item.getData().equals(data)) {
                        Instance.items.update(item, data, false, false);
                    }
                }
            });
        }
    }
}
