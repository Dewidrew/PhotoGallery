package ayp.aug.photogallery;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class PollStarterReceiver extends BroadcastReceiver {
    private static final String TAG = "PollStarterReceiver";
    public PollStarterReceiver() {
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d(TAG,"Receive some message");
        Boolean isOn = PhotoGalleryPreference.getStoredIsAlarmOn(context);
        PollService.setServiceAlarm(context,isOn);

        Log.d(TAG,"Status of service alarm is : " + isOn);

    }
}
