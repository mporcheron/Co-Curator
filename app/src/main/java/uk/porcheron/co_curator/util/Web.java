package uk.porcheron.co_curator.util;

import android.util.Base64;
import android.util.Log;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.AbstractHttpEntity;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.List;

import uk.porcheron.co_curator.val.Instance;

/**
 * Utilities for posting and receiving information from the web.
 */
public class Web {
    private static final String TAG = "CC:Web";

    public static final String IMAGE_DIR = Instance.serverAddress + "/uploads/";

    public static final String LOGIN = Instance.serverAddress + "/login.php";
    public static final String GET_USERS = Instance.serverAddress + "/getUsers.php";
    public static final String GET_ITEMS = Instance.serverAddress + "/getItems.php";
    public static final String GET_ITEM = Instance.serverAddress + "/getItem.php";
    public static final String POST_ITEMS = Instance.serverAddress + "/post.php";
    public static final String DELETE = Instance.serverAddress + "/delete.php";
    public static final String UPDATE = Instance.serverAddress + "/update.php";

    public static final String GET_URL_SCREENSHOT = Instance.serverAddress + "/getScreenshot.php?url=";
    public static final String GET_URL_SCREENSHOT_STORE = Instance.serverAddress + "/url/";

    public static JSONObject requestObj(String uri, List<NameValuePair> data) {
        String response = request(uri, data);

        if (response == null) {
            return null;
        }

        try {
            return new JSONObject(response);
        } catch (JSONException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static JSONObject requestObj(String uri, MultipartEntity data) {
        String response = request(uri, data);

        if (response == null) {
            return null;
        }

        try {
            return new JSONObject(response);
        } catch (JSONException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static JSONArray requestArr(String uri, List<NameValuePair> data) {
        String response = request(uri, data);

        if (response == null) {
            return null;
        }

        try {
            return new JSONArray(response);
        } catch (JSONException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static JSONArray requestArr(String uri, MultipartEntity data) {
        String response = request(uri, data);

        if (response == null) {
            return null;
        }

        try {
            return new JSONArray(response);
        } catch (JSONException e) {
            e.printStackTrace();
            return null;
        }
    }

    private static String request(String uri, List<NameValuePair> data) {
        Log.v(TAG, "Send request to " + uri);
        try {
            return request(uri, new UrlEncodedFormEntity(data));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            Log.e(TAG, "Could not turn NameValuePairs into encoded form entity");
            return null;
        }
    }

    private static String request(String uri, HttpEntity data) {
        HttpClient httpClient = new DefaultHttpClient();
        HttpPost httpPost = new HttpPost(uri);

        try {
            httpPost.setEntity(data);

            HttpResponse httpResponse = httpClient.execute(httpPost);
            HttpEntity messageEntity = httpResponse.getEntity();
            InputStream is = messageEntity.getContent();

            BufferedReader br = new BufferedReader(new InputStreamReader(is));
            StringBuilder buf = new StringBuilder();
            String line;
            while ((line = br.readLine()) != null) {
                buf.append(line);
            }

            Log.v(TAG, buf.toString());
            return buf.toString();
        } catch (ClientProtocolException e) {
            e.printStackTrace();
            return null;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static String b64encode(String text) {
        byte[] data = null;
        try {
            data = text.getBytes("UTF-8");
        } catch (UnsupportedEncodingException e1) {
            e1.printStackTrace();
        }
        return Base64.encodeToString(data, Base64.DEFAULT);
    }
}
