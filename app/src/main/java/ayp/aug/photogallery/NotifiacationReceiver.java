package ayp.aug.photogallery;

import android.app.Activity;
import android.app.Notification;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationManagerCompat;
import android.util.Log;

public class NotifiacationReceiver extends BroadcastReceiver {

    private static final String TAG = "NotifiacationReceiver";

    public NotifiacationReceiver() {
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d(TAG, "Notification calling");

        if(getResultCode() != Activity.RESULT_OK){
            return;
        }


        Notification notification = (Notification)
                intent.getParcelableExtra(PollService.NOTIFICATION);

        int requestCode = intent.getIntExtra(PollService.REQUEST_CODE,0);

        NotificationManagerCompat.from(context).notify(0,notification);

        Log.d(TAG,"Notify new item displayed");
    }
}
