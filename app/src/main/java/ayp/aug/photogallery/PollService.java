package ayp.aug.photogallery;

import android.app.AlarmManager;
import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.LightingColorFilter;
import android.net.ConnectivityManager;
import android.os.Build;
import android.os.SystemClock;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Hattapong on 8/22/2016.
 */
public class PollService extends IntentService {
    private static final String TAG = "PollService";
    private static final int POLL_INTERVAL = 1000 * 60; // 60 second

    public static Intent newIntent(Context context){
        return new Intent(context,PollService.class);
    }

    public static boolean isServiceAlarmOn(Context ctx){
        if(Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            Intent i = PollService.newIntent(ctx);
            PendingIntent pi = PendingIntent.getService(ctx, 0, i, PendingIntent.FLAG_NO_CREATE);

            return pi != null;
        }else{
            return PollJobService.isRun(ctx);
        }
    }

    public PollService() {
        super(TAG);
    }

    public static void setServiceAlarm(Context c,Boolean isOn){
        Intent i = PollService.newIntent(c);
        PendingIntent pi = PendingIntent.getService(c,0,i,0);

        AlarmManager am = (AlarmManager) c.getSystemService(Context.ALARM_SERVICE);

        if(isOn){
            if(Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
                am.setInexactRepeating(AlarmManager.ELAPSED_REALTIME, // MODE
                        SystemClock.elapsedRealtime(),                // Start Time
                        POLL_INTERVAL,                                // Interval
                        pi);                                          // Pending action(Intent)
                Log.d(TAG,"Run by Alarm Manager");
            }else {
                PollJobService.start(c);
                Log.d(TAG,"Run by Scheduler");
            }
        }else{
            if(Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
                am.cancel(pi);
                pi.cancel();
            }else{
                PollJobService.start(c);
            }
        }
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        Log.i(TAG,"Receive a call from intent: " + intent);
        if(!isNetworkAvailableAndConnected()){
            return ;
        }
        Log.i(TAG,"Active Network!!");

        String query = PhotoGalleryPreference.getStoredSearchKey(this);
        String storedLastId = PhotoGalleryPreference.getStoredLastId(this);

        List<GalleryItem>  galleryItemList = new ArrayList<>();

        FlickerFetcher flickrFetcher = new FlickerFetcher();
        if(query == null){
            flickrFetcher.getRecentPhotos(galleryItemList);
        }else{
            flickrFetcher.searchPhotos(galleryItemList,query);
        }

        // In Case flickFetcher ERROR
        if(galleryItemList.size() == 0){
            return;
        }

        Log.i(TAG,"Found search or recent item");

        String newestId = galleryItemList.get(0).getId(); //Newest Picture

        //Check that Newest Image is Update or NOT???
        if(newestId.equals(storedLastId)){
            Log.i(TAG,"No new item");
        }else{
            Log.i(TAG,"New item found");

            Resources res = getResources();
            Intent i = PhotoGalleryActivity.newIntent(this);
            PendingIntent pi = PendingIntent.getActivity(this,0,i,0);

            NotificationCompat.Builder notiBuilder = new NotificationCompat.Builder(this);
            notiBuilder.setTicker(res.getString(R.string.new_picture_arriving));
            notiBuilder.setSmallIcon(android.R.drawable.ic_menu_report_image);
            notiBuilder.setContentTitle(res.getString(R.string.new_picture_title));
            notiBuilder.setContentText(res.getString(R.string.new_picure_content));
            notiBuilder.setContentIntent(pi);
            notiBuilder.setContentInfo("Hello");
            notiBuilder.setAutoCancel(true);

            Notification notification = notiBuilder.build();
            NotificationManagerCompat nm = NotificationManagerCompat.from(this);
            nm.notify(0,notification);
        }

    }
    private boolean isNetworkAvailableAndConnected(){
        ConnectivityManager cm = (ConnectivityManager)getSystemService(CONNECTIVITY_SERVICE);
        boolean isActiveNetwork = cm.getActiveNetworkInfo() != null;
        boolean isActiveNetworkConneted = isActiveNetwork && cm.getActiveNetworkInfo().isConnected();
        return isActiveNetworkConneted;
    }
}
