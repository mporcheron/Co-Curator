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
import uk.porcheron.co_curator.item.ItemList;
import uk.porcheron.co_curator.item.ItemType;
import uk.porcheron.co_curator.user.User;
import uk.porcheron.co_curator.util.SoUtils;
import uk.porcheron.co_curator.val.Collo;
import uk.porcheron.co_curator.val.Instance;
import uk.porcheron.co_curator.util.Image;
import uk.porcheron.co_curator.util.Web;

/**
 * Utilities for loading resources from the web.
 */
public class WebLoader {
    private static final String TAG = "CC:WebLoader";

    public static Bitmap getBitmapFromURL(String src) {
        try {
            java.net.URL url = new java.net.URL(src);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setDoInput(true);
            connection.connect();
            InputStream input = connection.getInputStream();
            return BitmapFactory.decodeStream(input);
        } catch (IOException e) {
            Log.e(TAG, "Failed to get image from URL: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

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
                    JSONObject itemJ = (JSONObject) response.get(i);

                    final int itemId = itemJ.getInt("id");

                    if (!Instance.items.containsByItemId(globalUserId, itemId, true)) {
                        final User user = Instance.users.getByGlobalUserId(globalUserId);
                        if(user == null) {
                            continue;
                        }
                        String jsonData = itemJ.getString("data");
                        final ItemType type = ItemType.get(itemJ.getInt("type"));
                        final boolean deleted = itemJ.getInt("deleted") == TableItem.VAL_ITEM_DELETED;

                        if (type == ItemType.PHOTO) {
                            Bitmap b = getBitmapFromURL(Web.IMAGE_DIR + jsonData);
                            try {
                                jsonData = Image.save(activity, b);
                            } catch (IOException e) {
                                Log.e(TAG, "Failed to download image " + jsonData);
                                e.printStackTrace();
                            }
                        }

                        final String data = jsonData;
                        final int dateTime = itemJ.getInt("dateTime");

                        Instance.items.registerForthcomingItem(globalUserId,itemId);
                        activity.runOnUiThread(new Runnable() {
                            public void run() {
                                Instance.items.add(itemId, type, user, data, dateTime, deleted, true, false);
                            }
                        });
                    } else {
                        Log.v(TAG, "Item already exists locally, compare deleted state");
                        final int deleted = itemJ.getInt("deleted");

                        activity.runOnUiThread(new Runnable() {
                            public void run() {
                                Item item = Instance.items.getByItemId(globalUserId, itemId);
                                if (deleted == TableItem.VAL_ITEM_DELETED && !item.isDeleted()) {
                                    Instance.items.remove(globalUserId, itemId, true, false, false);
                                }
                            }
                        });
                    }
                } catch (JSONException e) {
                    Log.e(TAG, "Could not get items from the cloud");
                    e.printStackTrace();
                }

            }
        }

    }

    public static void loadItemFromWeb(int globalUserId, int itemId) {
        Log.d(TAG, "Get Item[" + globalUserId + ":" + itemId + "] from cloud");

        TimelineActivity activity = TimelineActivity.getInstance();

        List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(2);
        nameValuePairs.add(new BasicNameValuePair("globalUserId", "" + globalUserId));
        nameValuePairs.add(new BasicNameValuePair("itemId", "" + itemId));

        JSONObject response = Web.requestObj(Web.GET_ITEM, nameValuePairs);
        if (response != null) {
            try {
                if (Instance.items.getByItemId(globalUserId, itemId) == null) {
                    final int cItemId = response.getInt("id");
                    final User user = Instance.users.getByGlobalUserId(globalUserId);
                    String jsonData = response.getString("data");
                    final ItemType type = ItemType.get(response.getInt("type"));
                    final boolean deleted = response.getInt("deleted") == TableItem.VAL_ITEM_DELETED;

                    if (type == ItemType.PHOTO) {
                        Bitmap b = getBitmapFromURL(Web.IMAGE_DIR + jsonData);
                        try {
                            jsonData = Image.save(activity, b);
                        } catch (IOException e) {
                            Log.e(TAG, "Failed to download image " + jsonData);
                            e.printStackTrace();
                        }
                    }

                    final String data = jsonData;
                    final int dateTime = response.getInt("dateTime");

                    Instance.items.registerForthcomingItem(globalUserId,cItemId);
                    activity.runOnUiThread(new Runnable() {
                        public void run() {
                            Instance.items.add(cItemId, type, user, data, dateTime, deleted, true, false);
                        }
                    });
                } else {
                    //TODO: change to update
                    Log.e(TAG, "Item already exists!");
                }
            } catch (JSONException e) {
                Log.e(TAG, "Could not get Item[" + itemId + "] from the cloud");
                e.printStackTrace();
            }
        } else {
            Log.e(TAG, "Could not get Item[" + itemId + "] from the cloud");
        }
    }
}
