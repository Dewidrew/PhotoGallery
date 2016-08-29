package ayp.aug.photogallery;

import android.net.Uri;

/**
 * Created by Hattapong on 8/16/2016.
 */
public class GalleryItem {
    private String mId;
    private String mTitle;
    private String mUrl;
    private String owner;

    public String getTitle() {
        return mTitle;
    }

    public void setTitle(String mTitle) {
        this.mTitle = mTitle;
    }

    public String getUrl() {
        return mUrl;
    }

    public void setUrl(String mUrl) {
        this.mUrl = mUrl;
    }

    public String getId() {
        return mId;
    }

    public void setId(String mId) {
        this.mId = mId;
    }

    public String getName() {
        return getTitle();
    }

    public void setName(String name) {
        setTitle(name);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof GalleryItem) {
            GalleryItem that = (GalleryItem) obj;
            return that.mId.equals(mId) && that.mId != null && mId != null;
        }
        return false;
    }

    public void setOwner(String owner) {
        this.owner = owner;
    }

    public String getOwner() {
        return owner;
    }

    private static final String PHOTO_URL_PREFIX = "http://www.flickr.com/photos/";
    public Uri getPhotoUri(){
        return Uri.parse(PHOTO_URL_PREFIX).buildUpon()
                .appendPath(owner)
                .appendPath(mId)
                .build();
    }
}
