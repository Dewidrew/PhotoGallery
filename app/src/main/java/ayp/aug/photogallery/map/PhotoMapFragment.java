package ayp.aug.photogallery.map;

import android.graphics.Bitmap;
import android.location.Location;
import android.os.Bundle;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;

import org.xml.sax.Locator;

import ayp.aug.photogallery.GalleryItem;

/**
 * Created by Hattapong on 9/5/2016.
 */
public class PhotoMapFragment extends SupportMapFragment {

    private static final String KEY_LOCATION = "G1";
    private static final String KEY_GALLERYITEM = "G2" ;
    private static final String KEY_BITMAP = "G3";
    private GoogleMap mGoogleMap;

    public static PhotoMapFragment newInstance(Location location, Location galleryItemLoc, Bitmap bitmap) {
        Bundle args = new Bundle();
        args.putParcelable(KEY_LOCATION,location);
        args.putParcelable(KEY_GALLERYITEM,galleryItemLoc);
        args.putParcelable(KEY_BITMAP,bitmap);

        
        PhotoMapFragment fragment = new PhotoMapFragment();
        fragment.setArguments(args);
        return fragment;
    }
    @Override
    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);

        setHasOptionsMenu(true);
        getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(GoogleMap googleMap) {
                mGoogleMap = googleMap;
            }
        });
    }
}
