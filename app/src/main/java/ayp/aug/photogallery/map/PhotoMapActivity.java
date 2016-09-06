package ayp.aug.photogallery.map;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.location.Location;
import android.support.v4.app.Fragment;

import ayp.aug.photogallery.SingleFragmentActivity;

/**
 * Created by Hattapong on 9/5/2016.
 */
public class PhotoMapActivity extends SingleFragmentActivity {
    private static final String KEY_LOCATION = "G1";
    private static final String KEY_GALLERYITEM = "G2" ;
    private static final String KEY_BITMAP = "G3";
    public static Intent newIntent(Context c, Location location, Location galleryItemLoc, String url){
        Intent i = new Intent(c,PhotoMapActivity.class);
        i.putExtra(KEY_LOCATION,location);
        i.putExtra(KEY_GALLERYITEM,galleryItemLoc);
        i.putExtra(KEY_BITMAP,url);

        return i;
    }

    @Override
    protected Fragment onCreateFragment() {

        if(getIntent() != null){
            Location location = getIntent().getParcelableExtra(KEY_LOCATION);
            Location galleryLoc = getIntent().getParcelableExtra(KEY_GALLERYITEM);
            String url = getIntent().getStringExtra(KEY_BITMAP);
            return PhotoMapFragment.newInstance(location,galleryLoc,url);
        }

        return PhotoMapFragment.newInstance();
    }
}
