package ayp.aug.photogallery;

/**
 * Created by Hattapong on 8/16/2016.
 */
public class GalleryItem {
    private String mId;
    private String mTitle;
    private String mUrl;

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
}
