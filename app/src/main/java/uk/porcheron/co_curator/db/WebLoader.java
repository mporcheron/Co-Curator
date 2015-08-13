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
import uk.porcheron.co_curator.item.ItemType;
import uk.porcheron.co_curator.user.User;
import uk.porcheron.co_curator.val.Instance;
import uk.porcheron.co_curator.util.Image;
import uk.porcheron.co_curator.util.Web;

/**
 * Utilities for loading resources from the web.
 */
public class WebLoader {
    private static final String TAG = "CC:WebLoader";

    private TimelineActivity mActivity = TimelineActivity.getInstance();

    WebLoader() {
    }

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

    protected void loadUsersFromWeb() {
        Log.d(TAG, "Load users from cloud");

        List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(2);
        nameValuePairs.add(new BasicNameValuePair("groupId", "" + Instance.groupId));

        JSONArray response = Web.requestArr(Web.GET_USERS, nameValuePairs);
        if (response != null) {
            for (int i = 0; i < response.length(); i++) {
                try {
                    JSONObject userJ = (JSONObject) response.get(i);
                    int gId = userJ.getInt("globalUserId");
                    if(Instance.users.getByGlobalUserId(gId) == null) {
                        Instance.users.add(userJ.getInt("globalUserId"), true);
                    }
                } catch (JSONException e) {
                    Log.e(TAG, "Could not get user from the cloud");
                    e.printStackTrace();
                }

            }
        }

    }

    protected void loadItemsFromWeb(int globalUserId) {
        Log.d(TAG, "Load items from cloud");

        List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(2);
        nameValuePairs.add(new BasicNameValuePair("globalUserId", "" + globalUserId));

        JSONArray response = Web.requestArr(Web.GET_ITEMS, nameValuePairs);
        if (response != null) {
            for (int i = 0; i < response.length(); i++) {
                try {
                    JSONObject itemJ = (JSONObject) response.get(i);

                    final int itemId = itemJ.getInt("id");

                    if (Instance.items.getByItemId(globalUserId, itemId) == null) {
                        final User user = Instance.users.getByGlobalUserId(globalUserId);
                        String jsonData = itemJ.getString("data");
                        final ItemType type = ItemType.get(itemJ.getInt("type"));

                        if (type == ItemType.PHOTO) {
                            Bitmap b = getBitmapFromURL(jsonData);
                            try {
                                jsonData = Image.save(mActivity, b);
                            } catch (IOException e) {
                                Log.e(TAG, "Failed to download image " + jsonData);
                                e.printStackTrace();
                            }
                        }

                        final String data = jsonData;

                        mActivity.runOnUiThread(new Runnable() {
                            public void run() {
                                Instance.items.add(itemId, type, user, data, true, false);
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
}
