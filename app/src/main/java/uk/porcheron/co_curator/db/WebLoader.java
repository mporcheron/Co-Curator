package uk.porcheron.co_curator.db;

import android.util.Log;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import uk.porcheron.co_curator.TimelineActivity;
import uk.porcheron.co_curator.item.Item;
import uk.porcheron.co_curator.item.ItemPhoto;
import uk.porcheron.co_curator.item.ItemType;
import uk.porcheron.co_curator.item.ItemUrl;
import uk.porcheron.co_curator.user.User;
import uk.porcheron.co_curator.util.Image;
import uk.porcheron.co_curator.util.SoUtils;
import uk.porcheron.co_curator.val.Instance;
import uk.porcheron.co_curator.util.Web;

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
        final int dateTime = response.getInt("dateTime");
        final String data = response.getString("data");

        boolean contains = Instance.items.containsByItemId(globalUserId, itemId, true);
        if (!contains) {
            final User user = Instance.users.getByGlobalUserId(globalUserId);

            if (type == ItemType.PHOTO) {
                String url = Web.IMAGE_DIR + data;
                String filename = data;

                int width = ItemPhoto.getThumbnailWidth();
                int height = ItemPhoto.getThumbnailHeight();

                newPhotoItem(globalUserId, itemId, type, user, data, url, filename, width, height, dateTime, deleted);
            } else if (type == ItemType.URL) {
                String b64Url = Web.b64encode(data);
                String filename = itemId + "-" + b64Url;
                String url = Web.GET_URL_SCREENSHOT + b64Url;

                boolean isVideo = ItemUrl.isVideo(data);
                int width = ItemUrl.getThumbnailWidth(isVideo);
                int height = ItemUrl.getThumbnailHeight(isVideo);

                newPhotoItem(globalUserId, itemId, type, user, data, url, filename, width, height, dateTime, deleted);
            } else {
                newTextItem(globalUserId, itemId, type, user, data, dateTime, deleted);
            }
        } else {
            Log.e(TAG, "Item already exists, update it");

            activity.runOnUiThread(new Runnable() {
                public void run() {
                    final Item item = Instance.items.getByItemId(globalUserId, itemId);

                    if (deleted && !item.isDeleted()) {
                        Instance.items.remove(item, true, false, false);
                    } else if (!deleted && item.isDeleted()) {
                        Instance.items.unremove(item);
                    }

                    if (!item.getData().equals(data)) {
                        if (type == ItemType.NOTE) {
                            updateTextItem(item, data);
                            Instance.items.update(item, data, false, false);
                        } else if (type == ItemType.PHOTO) {
//                            String url = Web.IMAGE_DIR + data;
//                            String filename = data;
//
//                            int width = ItemPhoto.getThumbnailWidth();
//                            int height = ItemPhoto.getThumbnailHeight();
//
//                            updatePhotoItem(globalUserId, item, data, url, filename, width, height);
                        } else if (type == ItemType.URL) {
                            String b64Url = Web.b64encode(data);
                            String filename = itemId + "-" + b64Url;
                            String url = Web.GET_URL_SCREENSHOT + b64Url;

                            boolean isVideo = ItemUrl.isVideo(data);
                            int width = ItemUrl.getThumbnailWidth(isVideo);
                            int height = ItemUrl.getThumbnailHeight(isVideo);

                            updatePhotoItem(globalUserId, item, data, url, filename, width, height);
                        }
                    }
                }
            });
        }
    }

    private static void newTextItem(final int globalUserId, final int itemId, final ItemType type, final User user, final String data, final int dateTime, final boolean deleted) {
        Instance.items.registerForthcomingItem(globalUserId, itemId);
        TimelineActivity.getInstance().runOnUiThread(new Runnable() {
            public void run() {
                Log.v(TAG, "Item[" + itemId + "]: Save (globalUserId=" + globalUserId + ",itemId=" + itemId + ",type=" + type.toString() + ",dateTime=" + dateTime + ",data='" + data + "',deleted=" + deleted + ")");
                Instance.items.add(itemId, type, user, data, dateTime, deleted, true, false);
            }
        });
    }

    private static void newPhotoItem(final int globalUserId, final int itemId, final ItemType type, final User user, final String data, String url, String filename, final int width, final int height, final int dateTime, final boolean deleted) {
        Image.url2File(url, filename, width, height, new Runnable() {
            @Override
            public void run() {
                newTextItem(globalUserId, itemId, type, user, data, dateTime, deleted);
            }
        });
    }

    private static void updateTextItem(Item item, final String data) {
        Instance.items.update(item, data, false, false);
    }

    private static void updatePhotoItem(final int globalUserId, final Item item, final String data, String url, String filename, final int width, final int height) {
        Image.url2File(url, filename, width, height, new Runnable() {
            @Override
            public void run() {
                updateTextItem(item, data);
            }
        });
    }
}
