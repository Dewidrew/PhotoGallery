package ayp.aug.photogallery;

import android.net.Uri;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Calendar;
import java.util.List;

/**
 * Created by Hattapong on 8/16/2016.
 */
public class FlickerFetcher {
    private static final String TAG = "FlickrFetcher";


    /**
     * get <b>data</b> from web service(<b>urlSpec</b>)
     * @param urlSpec url target(<b>String</b>)
     * @return data (<b>bytes</b>)
     * @throws IOException
     */
    public byte[] getUrlBytes(String urlSpec) throws IOException {
        URL url = new URL(urlSpec);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        try {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            InputStream in = connection.getInputStream();

            //if connection is not OK throw new IOException
            if (connection.getResponseCode() != HttpURLConnection.HTTP_OK) {
                throw new IOException(connection.getResponseMessage() + ":with " + urlSpec);
            }
            int byteRead = 0;
            byte[] buffer = new byte[2048];
            while ((byteRead = in.read(buffer)) > 0) {
                out.write(buffer, 0, byteRead);
            }
            out.close();
            return out.toByteArray();
        } finally {
            connection.disconnect();
        }
    }

    /**
     * transfer bytes to String
     * @param urlSpec url target(<b>String</b>)
     * @return data(<b>String</b>)
     * @throws IOException
     */
    public String getUrlString(String urlSpec) throws IOException {
        return new String(getUrlBytes(urlSpec));
    }

    //
    private static final String FLICK_URL = "https://api.flickr.com/services/rest/";
    private static final String API_KEY = "0eae29c79033fd52e932785bed5353a6";

    private static final String METHOD_GET_RECENT = "flickr.photos.getRecent";
    private static final String METHOD_SEARCH = "flickr.photos.search";


    /**
     * build <b>URL</b> and add parameter
     * @param method kind of search <b>getPhotos</b> or <b>search</b>
     * @param param parameter of <b>URL</b>
     * @return URL (<b>String</b>)
     * @throws IOException
     */
    private String buildUri(String method,String ... param) throws IOException {
        Uri baseUrl = Uri.parse(FLICK_URL);
        Uri.Builder builder = baseUrl.buildUpon();
        builder.appendQueryParameter("method", method);
        builder.appendQueryParameter("api_key", API_KEY);
        builder.appendQueryParameter("format", "json");
        builder.appendQueryParameter("nojsoncallback", "1");
        builder.appendQueryParameter("extras", "url_s");
        //equals without case (insensitive).
        if(METHOD_SEARCH.equalsIgnoreCase(method)){
            builder.appendQueryParameter("text",param[0]);
        }

        Uri completeUrl = builder.build();
        String url = completeUrl.toString();

        Log.i(TAG,"Run URL: "+ url);

        return url;
    }

    /**
     * Query <b>data</b> from URL
     * @param url url target
     * @return data (<b>String</b>)
     * @throws IOException
     */
    private String queryItem(String url) throws IOException{
        Log.i(TAG,"Run URL: "+ url);
        String jsonString = getUrlString(url);
        Log.i(TAG, "Receive JSON " + jsonString);

        return jsonString;
    }

    /**
     * Search photo then put into <b>items</b>
     * @param items array target
     * @param key to search
     */
    public void searchPhotos(List<GalleryItem> items,String key) {
        try {
            String url = buildUri(METHOD_SEARCH,key);
            String jsonStr = queryItem(url);
            if (jsonStr != null) {
                parseJSON(items, jsonStr);
            }
        } catch (Exception e) {
            e.printStackTrace();
            Log.e(TAG, "Failed to fetchItems ", e);
        }
    }

    /**
     * call method that get <b>RecentPhoto</b> in list
     * @param items target list
     */
    public void getRecentPhotos(List<GalleryItem> items) {
        try {
            String url = buildUri(METHOD_GET_RECENT);
            String jsonStr = queryItem(url);
            if (jsonStr != null) {
                parseJSON(items, jsonStr);
            }
        } catch (Exception e) {
            e.printStackTrace();
            Log.e(TAG, "Failed to fetchItems ", e);
        }
    }

    /**
     * add Photo from URL target in List
     * @param newGalleryItemList target List
     * @param jsonBodyStr URL target
     * @throws IOException
     * @throws JSONException
     */
    private void parseJSON(List<GalleryItem> newGalleryItemList, String jsonBodyStr) throws IOException, JSONException {
        JSONObject jsonBody = new JSONObject(jsonBodyStr);
        JSONObject photosJson = jsonBody.getJSONObject("photos");
        JSONArray photoListJson = photosJson.getJSONArray("photo");

//        JSONArray photoListJson = new JSONObject(jsonBodyStr).getJSONObject("photos").getJSONArray("photo");

        for (int i = 0; i < photoListJson.length(); i++) {
            JSONObject jsonPhotoItem = photoListJson.getJSONObject(i);
            GalleryItem item = new GalleryItem();
            item.setId(jsonPhotoItem.getString("id"));
            item.setTitle(jsonPhotoItem.getString("title"));
            item.setOwner(jsonPhotoItem.getString("owner"));
            if (!jsonPhotoItem.has("url_s")) {
                continue;
            }

            item.setUrl(jsonPhotoItem.getString("url_s"));
            newGalleryItemList.add(item);
        }
    }

}