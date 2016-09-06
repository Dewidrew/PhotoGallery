package ayp.aug.photogallery;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.util.Log;
import android.util.LruCache;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationAvailability;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

import java.util.ArrayList;
import java.util.List;

import ayp.aug.photogallery.map.PhotoMapActivity;
import ayp.aug.photogallery.settings.SettingActivity;

/**
 * Created by Hattapong on 8/16/2016.
 */
public class PhotoGalleryFragment extends VisibleFragment {
    private static final String TAG = "PhotoGalleryFragment";
    private static final int REQUEST_PERMISSION_LOCATION = 21321;

    public static PhotoGalleryFragment newInstance() {
        Bundle args = new Bundle();

        PhotoGalleryFragment fragment = new PhotoGalleryFragment();
        fragment.setArguments(args);
        return fragment;
    }

    private Boolean mUseGPS;
    private RecyclerView mRecyclerView;
    private String mSearchKey;
    private PhotoGalleryAdapter mAdapter;
    private ThumbnailDownloader<PhotoHolder> mThumbnailDownloaderThread;
    private FetcherTask mFetcherTask;
    private List<GalleryItem> mItem;
    private LruCache<String, Bitmap> mMemoryCache;
    private Location mLocation;
    final int maxMemory = (int) (Runtime.getRuntime().maxMemory() / 1024);

    private GoogleApiClient mGoogleApiClient;
    private GoogleApiClient.ConnectionCallbacks mConnectionCallbacks =
            new GoogleApiClient.ConnectionCallbacks() {
                @Override
                @SuppressWarnings("all")
                public void onConnected(@Nullable Bundle bundle) {
                    Log.i(TAG, "Google API Connected");
                    mLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
                    Log.i(TAG, "Get Last Location "+mLocation);

                    if (mUseGPS) {
                        findLocation();
                        loadPhotos();
                    }
                }

                @Override
                public void onConnectionSuspended(int i) {
                    Log.i(TAG, "Google API Suspended");
                }
            };

    private LocationListener mLocationListener = new LocationListener() {
        @Override
        public void onLocationChanged(Location location) {
            mLocation = location;
            if (mUseGPS) {
                loadPhotos();
            }
            Toast.makeText(getActivity(), location.getLatitude() + "," + location.getLongitude(),
                    Toast.LENGTH_LONG).show();
        }
    };
    final int cacheSize = maxMemory / 8;

    @Override
    public void onStart() {
        super.onStart();
        mGoogleApiClient.connect();
    }

    @Override
    public void onStop() {
        super.onStop();
        mGoogleApiClient.disconnect();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        mThumbnailDownloaderThread.quit();
        Log.i(TAG, "Stop background thread");
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.mnu_reload:
                loadPhotos();
                return true;

            case R.id.mnu_clear_search:
                mSearchKey = null;
                loadPhotos();
                return true;

            case R.id.mnu_toggle_pollling:
                boolean shouldStartAlarm = !PhotoGalleryPreference.getStoredIsAlarmOn(getActivity());
                Log.d(TAG, ((shouldStartAlarm) ? "Start" : "Stop") + " Intent service");
                PollService.setServiceAlarm(getActivity(), shouldStartAlarm);
                getActivity().invalidateOptionsMenu(); //Refresh menu
                return true;

            case R.id.mnu_manual_check:
                Intent pollIntent = PollService.newIntent(getActivity());
                getActivity().startService(pollIntent);
                return true;

            case R.id.mnu_setting:
                Intent i = SettingActivity.newIntent(getActivity());
                startActivity(i);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onPause() {
        super.onPause();
        PhotoGalleryPreference.setStoredSearchKey(getActivity(), mSearchKey);
        unFindLocation();
    }

    @Override
    public void onResume() {
        super.onResume();
        String searchKey = PhotoGalleryPreference.getStoredSearchKey(getActivity());
        if (searchKey != null) {
            mSearchKey = searchKey;
            loadPhotos();
        }
        mUseGPS = PhotoGalleryPreference.getUseGPS(getActivity());
        if(!mUseGPS)
        {
            loadPhotos();
        }

    }

    private void findLocation() {
        if (hasPermission()) {
            requestLocation();
        }
    }

    private boolean hasPermission() {
        int permissionStatus =
                ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION);

        // Permission is already allow
        if (permissionStatus == PackageManager.PERMISSION_GRANTED) {
            return true;

        }

        // Permission still not allow
        // Send request Permission to user
        requestPermissions(
                new String[]{
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_COARSE_LOCATION
                }, REQUEST_PERMISSION_LOCATION);

        return false;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == REQUEST_PERMISSION_LOCATION) {
            //User click 'Allow'
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                requestLocation();
            }
        }
    }

    @SuppressWarnings("all")
    private void requestLocation() {
        if (GooglePlayServicesUtil.isGooglePlayServicesAvailable(getActivity()) == ConnectionResult.SUCCESS) {


            LocationRequest request = LocationRequest.create();

            request.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
            request.setNumUpdates(50);
            request.setInterval(1000);

            LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, request,
                    // Need to  @SuppressWarnings("all")
                    mLocationListener);

            LocationAvailability locationAvailability = LocationServices.FusedLocationApi.getLocationAvailability(mGoogleApiClient);
            if(locationAvailability.isLocationAvailable()) {
                // Call Location Services
                Log.d(TAG,"Available");
            } else {
                // Do something when Location Provider not available
                Log.d(TAG,"Not Available");
            }

        }
    }

    private void unFindLocation() {
        if (GooglePlayServicesUtil.isGooglePlayServicesAvailable(getActivity()) == ConnectionResult.SUCCESS) {

            LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, mLocationListener);
        }
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setHasOptionsMenu(true);
        setRetainInstance(true);

        mUseGPS = PhotoGalleryPreference.getUseGPS(getActivity());
        mSearchKey = PhotoGalleryPreference.getStoredSearchKey(getActivity());


        mMemoryCache = new LruCache<String, Bitmap>(cacheSize) {
            @Override
            protected int sizeOf(String key, Bitmap bitmap) {
                return bitmap.getByteCount() / 1024;
            }
        };

        Handler responseUIHandler = new Handler();

        ThumbnailDownloader.ThumbnailDownloaderListener<PhotoHolder> listener = new ThumbnailDownloader.ThumbnailDownloaderListener<PhotoHolder>() {
            @Override
            public void onThumbnailDownloaded(PhotoHolder target, Bitmap thumbnail, String url) {
                if (null == mMemoryCache.get(url)) {
                    mMemoryCache.put(url, thumbnail);
                }
                Drawable drawable = new BitmapDrawable(getResources(), thumbnail);
                target.bindDrawable(drawable, url);
            }
        };

        mThumbnailDownloaderThread = new ThumbnailDownloader<>(responseUIHandler);
        mThumbnailDownloaderThread.setmThumbnailDownloaderListener(listener);
        mThumbnailDownloaderThread.start();
        mThumbnailDownloaderThread.getLooper();

        Log.i(TAG, "Start background thread");

        mGoogleApiClient = new GoogleApiClient.Builder(getActivity())
                .addApi(LocationServices.API)
                .addConnectionCallbacks(mConnectionCallbacks)
                .build();


    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.menu_item, menu);

        final MenuItem menuItem = menu.findItem(R.id.mnu_search);
        final SearchView searchView = (SearchView) menuItem.getActionView();
        searchView.setQuery(mSearchKey, false);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {

            @Override
            public boolean onQueryTextSubmit(String query) {
                Log.d(TAG, "Query text submitted: " + query);
                mSearchKey = query;
                loadPhotos();
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                Log.d(TAG, "Query text changing: " + newText);
                return false;
            }
        });
        searchView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                searchView.setQuery(mSearchKey, false);
            }

        });
        searchView.setOnSearchClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                searchView.setQuery(mSearchKey, false);
            }
        });
        MenuItem mnuPolling = menu.findItem(R.id.mnu_toggle_pollling);
        Log.d(TAG, "Boolean : " + PhotoGalleryPreference.getStoredIsAlarmOn(getActivity()));
        if (PhotoGalleryPreference.getStoredIsAlarmOn(getActivity())) {
            mnuPolling.setTitle(R.string.stop_polling);
        } else {
            mnuPolling.setTitle(R.string.start_polling);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mThumbnailDownloaderThread.clearQueue();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_photo_gallery, container, false);

        mRecyclerView = (RecyclerView) v.findViewById(R.id.photo_gallery_recycler_view);
        mRecyclerView.setLayoutManager(new GridLayoutManager(getActivity(), 3));

        mItem = new ArrayList<>();
        mRecyclerView.setAdapter(new PhotoGalleryAdapter(mItem));

        mSearchKey = PhotoGalleryPreference.getStoredSearchKey(getActivity());


        return v;
    }

    public void loadPhotos() {
        if (mFetcherTask == null) {
            mFetcherTask = new FetcherTask();
            if (mSearchKey != null) {
                mFetcherTask.execute(mSearchKey);
            } else {
                mFetcherTask.execute();
            }
        }else{
            Log.d(TAG,"Fetch task is running now");
        }
    }

    class PhotoHolder extends RecyclerView.ViewHolder implements View.OnClickListener, View.OnCreateContextMenuListener, MenuItem.OnMenuItemClickListener {
        ImageView mPhoto;
        String url;
        GalleryItem mGalleryItem;

        public ImageView getmPhoto() {
            return mPhoto;
        }

        public PhotoHolder(View itemView) {
            super(itemView);
            mPhoto = (ImageView) itemView.findViewById(R.id.image_photo);
            itemView.setOnCreateContextMenuListener(this);
        }

        public void bindGalleryItem(GalleryItem galleryItem) {
            mGalleryItem = galleryItem;
        }

        public void bindDrawable(@NonNull Drawable drawable, final String url) {
            this.url = url;
            mPhoto.setImageDrawable(drawable);
            mPhoto.setOnClickListener(this);

        }

        @Override
        public void onClick(View view) {
            FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
            ShowPictureDialog showPictureDialog = ShowPictureDialog.newInstance(url);
            showPictureDialog.show(fragmentManager, "Show Picture");
            Log.d(TAG, "Photo Url: " + url);

        }

        @Override
        public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
            menu.setHeaderTitle(url);
            MenuItem menuItem1 = menu.add(0, 1, 0, R.string.open_with_external_broswer);
            menuItem1.setOnMenuItemClickListener(this);
            MenuItem menuItem2 = menu.add(0, 2, 0, R.string.open_in_app_browser);
            menuItem2.setOnMenuItemClickListener(this);
            MenuItem menuItem3 = menu.add(0, 3, 0, R.string.open_in_map);
            menuItem3.setOnMenuItemClickListener(this);

        }

        @Override
        public boolean onMenuItemClick(MenuItem item) {
            switch (item.getItemId()) {
                case 1:
                    Intent i = new Intent(Intent.ACTION_VIEW, mGalleryItem.getPhotoUri());
                    startActivity(i); // call external broswer by implicit intent
                    return true;
                case 2:
                    startActivity(PhotoPageActivity.newIntent(getActivity(), mGalleryItem.getPhotoUri())); // Open in app browser
                    return true;
                case 3:
                    Location itemLoc = null;
                    if (mGalleryItem.isGeoCorrect()) {
                        itemLoc = new Location("");
                        itemLoc.setLatitude(Double.valueOf(mGalleryItem.getLat()));
                        itemLoc.setLongitude(Double.valueOf(mGalleryItem.getLong()));
                    }


                    Intent i3 = PhotoMapActivity.newIntent(getActivity(), mLocation, itemLoc, mGalleryItem.getUrl());
                    startActivity(i3);
                    return true;
                default:

            }
            return false;
        }
    }

    class PhotoGalleryAdapter extends RecyclerView.Adapter<PhotoHolder> {
        List<GalleryItem> mGalleryItemList;

        public PhotoGalleryAdapter(List<GalleryItem> mGalleryItemList) {
            this.mGalleryItemList = mGalleryItemList;
        }

        @Override
        public PhotoHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(getActivity()).inflate(R.layout.item_photo, parent, false);
            return new PhotoHolder(v);
        }


        @Override
        public void onBindViewHolder(PhotoHolder holder, int position) {
            Drawable smileyDrawable =
                    ResourcesCompat.getDrawable(getResources(), R.drawable.loading_image, null);
            GalleryItem galleryItem = mGalleryItemList.get(position);
            holder.bindGalleryItem(galleryItem);
            holder.bindDrawable(smileyDrawable, galleryItem.getBigUrl());
//            if (mMemoryCache.get(galleryItem.getUrl()) != null) {
//                Bitmap bitmap = mMemoryCache.get(galleryItem.getUrl());
//                holder.bindDrawable(new BitmapDrawable(getResources(), bitmap), galleryItem.getUrl());
//            } else {
//
//                mThumbnailDownloaderThread.queueThumbnailDownload(holder, galleryItem.getUrl());
//            }

            Glide.with(getActivity()).load(galleryItem.getUrl()).into(holder.getmPhoto());

        }

        @Override
        public int getItemCount() {
            return mGalleryItemList.size();
        }
    }

    class FetcherTask extends AsyncTask<String, Void, List<GalleryItem>> {


        @Override
        protected List<GalleryItem> doInBackground(String... params) {

            try {

                List<GalleryItem> itemList = new ArrayList<>();
                //mFlickerFetcher.getRecentPhotos(itemList);
                FlickerFetcher flickrFetcher = new FlickerFetcher();
                if (params.length > 0) {
                    if (mUseGPS && mLocation != null) {
                        flickrFetcher.searchPhotos(itemList, params[0],
                                String.valueOf(mLocation.getLatitude()), String.valueOf(mLocation.getLongitude()));
                    } else {
                        flickrFetcher.searchPhotos(itemList, params[0]);
                    }
                } else {
                    flickrFetcher.getRecentPhotos(itemList);
                }

                return itemList;
            } finally {

            }
        }



        @Override
        protected void onProgressUpdate(Void... values) {
            super.onProgressUpdate(values);

            String formatString = getResources().getString(R.string.photo_progress_loaded);
            Snackbar.make(mRecyclerView, formatString, Snackbar.LENGTH_SHORT).show();

        }

        @Override
        protected void onPostExecute(List<GalleryItem> galleryItems) {

            mAdapter = new PhotoGalleryAdapter(galleryItems);
            mRecyclerView.setAdapter(mAdapter);

            mFetcherTask = null;

        }
    }
}
