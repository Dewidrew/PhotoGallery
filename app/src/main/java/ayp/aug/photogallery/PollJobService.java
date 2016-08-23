package ayp.aug.photogallery;

import android.annotation.TargetApi;
import android.app.Notification;
import android.app.PendingIntent;
import android.app.job.JobInfo;
import android.app.job.JobParameters;
import android.app.job.JobScheduler;
import android.app.job.JobService;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.os.AsyncTask;
import android.os.Build;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Hattapong on 8/23/2016.
 */
@TargetApi(Build.VERSION_CODES.LOLLIPOP)
public class PollJobService extends JobService {
    private static final String TAG = "PollJobService";
    private static final int POLL_INTERVAL = 1000 * 1;
    private PollTask mPollTask;

    @Override
    public boolean onStartJob(JobParameters params) {
        mPollTask = new PollTask();
        mPollTask.execute(params);
        return true; // false mean "Stop Running"
    }

    @Override
    public boolean onStopJob(JobParameters params) {
        if(mPollTask != null){
            mPollTask.cancel(true);
        }

        return true;
    }

    private static final int JOB_ID = 2186;
    public static void start(Context ctx){
        JobScheduler sch = (JobScheduler) ctx.getSystemService(Context.JOB_SCHEDULER_SERVICE);

        JobInfo.Builder builder = new JobInfo.Builder(JOB_ID,new ComponentName(ctx,PollJobService.class));
        builder.setRequiredNetworkType(JobInfo.NETWORK_TYPE_UNMETERED);
        builder.setPeriodic(POLL_INTERVAL);
//        builder.setPersisted(true);
        JobInfo jobInfo = builder.build();

        sch.schedule(jobInfo);
    }

    public static boolean isRun(Context ctx){
        JobScheduler sch = (JobScheduler) ctx.getSystemService(Context.JOB_SCHEDULER_SERVICE);

        List<JobInfo> jobInfoList = sch.getAllPendingJobs();
        for(JobInfo jobinfo : jobInfoList){
            if(jobinfo.getId() == JOB_ID){
                return true;
            }
        }
        return false;
    }

    public static void stop(Context ctx){
        JobScheduler sch = (JobScheduler)ctx.getSystemService(JOB_SCHEDULER_SERVICE);
        sch.cancel(JOB_ID);
    }

    public class PollTask extends AsyncTask<JobParameters,Void,Void>{

        @Override
        protected Void doInBackground(JobParameters... params) {
            //Do whatever job

            String query = PhotoGalleryPreference.getStoredSearchKey(PollJobService.this);
            String storedLastId = PhotoGalleryPreference.getStoredLastId(PollJobService.this);

            List<GalleryItem>  galleryItemList = new ArrayList<>();

            FlickerFetcher flickrFetcher = new FlickerFetcher();
            if(query == null){
                flickrFetcher.getRecentPhotos(galleryItemList);
            }else{
                flickrFetcher.searchPhotos(galleryItemList,query);
            }

            // In Case flickFetcher ERROR
            if(galleryItemList.size() == 0){
                return null;
            }

            Log.i(TAG,"Found search or recent item");

            String newestId = galleryItemList.get(0).getId(); //Newest Picture

            //Check that Newest Image is Update or NOT???
            if(newestId.equals(storedLastId)){
                Log.i(TAG,"No new item");
            }else{
                Log.i(TAG,"New item found");

                Resources res = getResources();
                Intent i = PhotoGalleryActivity.newIntent(PollJobService.this);
                PendingIntent pi = PendingIntent.getActivity(PollJobService.this,0,i,0);

                NotificationCompat.Builder notiBuilder = new NotificationCompat.Builder(PollJobService.this);
                notiBuilder.setTicker(res.getString(R.string.new_picture_arriving));
                notiBuilder.setSmallIcon(android.R.drawable.ic_menu_report_image);
                notiBuilder.setContentTitle(res.getString(R.string.new_picture_title));
                notiBuilder.setContentText(res.getString(R.string.new_picure_content));
                notiBuilder.setContentIntent(pi);
                notiBuilder.setContentInfo("Hello");
                notiBuilder.setAutoCancel(true);

                Notification notification = notiBuilder.build();
                NotificationManagerCompat nm = NotificationManagerCompat.from(PollJobService.this);
                nm.notify(0,notification);

                new Screen().on(PollJobService.this);
            }

            // Finish
            jobFinished(params[0],false);
            return null;
        }
    }
}
