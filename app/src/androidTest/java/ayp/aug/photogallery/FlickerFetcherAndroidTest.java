package ayp.aug.photogallery;

import android.support.test.filters.SmallTest;
import android.support.test.runner.AndroidJUnit4;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

/**
 * Created by Hattapong on 8/16/2016.
 */
@RunWith(AndroidJUnit4.class)
@SmallTest
public class FlickerFetcherAndroidTest {
    private FlickerFetcher mFlickerFetcher;
    @Before
    public void setUp() throws Exception {
        mFlickerFetcher = new FlickerFetcher();
    }

    @Test
    public void testGetUrlString() throws Exception {
        String htmlResult = mFlickerFetcher.getUrlString("https://www.augmentis.biz/");

        System.out.println(htmlResult);
        assertThat(htmlResult,containsString("IT Professional Services"));

    }

    @Test
    public void testFetch() throws Exception{
        String json = mFlickerFetcher.fetchItem();

        assertThat(json,containsString("perpage"));
    }

    @Test
    public void testFetchList() throws Exception{
        List<GalleryItem> galleryItemList = new ArrayList<>();
        mFlickerFetcher.fetchItems(galleryItemList);

        assertThat(galleryItemList.size(),is(100));
    }
}