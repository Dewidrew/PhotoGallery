package ayp.aug.photogallery;

import android.support.test.filters.SmallTest;
import android.support.test.runner.AndroidJUnit4;
import android.util.Log;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;

/**
 * Created by Hattapong on 8/16/2016.
 */
@RunWith(AndroidJUnit4.class)
@SmallTest
public class FlickerFetcherAndroidTest {
    private FlickerFetcher mFlickrFetcher;
    private static final String TAG = "FetcherTest";

    @Before
    public void setUp() throws Exception {
        mFlickrFetcher = new FlickerFetcher();
    }

    @Test
    public void testGetUrlString() throws Exception {
        String htmlResult = mFlickrFetcher.getUrlString("https://www.augmentis.biz/");
        System.out.println(htmlResult);
        assertThat(htmlResult,containsString("IT Professional Services"));
    }
    //    @Test
//    public void testFetch() throws  Exception{
//        String json = mFlickrFetcher.fetchItem();
//        assertThat(json,containsString("perpage"));
//    }
    @Test
    public void testSearch() throws  Exception{
        List<GalleryItem> galleryItemsList = new ArrayList<>();
        mFlickrFetcher.searchPhotos(galleryItemsList,"bird");

        Log.d(TAG,"test Search: size = " + galleryItemsList.size());
        assertThat(galleryItemsList.size(),not(0));
    }

    @Test
    public void testGetRecent() throws Exception{
        List<GalleryItem> galleryItemsList = new ArrayList<>();
        mFlickrFetcher.getRecentPhotos(galleryItemsList);
        Log.d(TAG,"test Search: size = " + galleryItemsList.size());
        assertThat(galleryItemsList.size(),not(0));
        assertThat(galleryItemsList.get(0).getOwner(),notNullValue());
    }
}