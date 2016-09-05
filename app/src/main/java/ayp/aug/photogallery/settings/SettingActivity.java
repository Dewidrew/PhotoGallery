package ayp.aug.photogallery.settings;

import android.content.Context;
import android.content.Intent;
import android.support.v4.app.Fragment;

import ayp.aug.photogallery.SingleFragmentActivity;

/**
 * Created by Hattapong on 9/5/2016.
 */
public class SettingActivity extends SingleFragmentActivity {


    public static Intent newIntent(Context c) {
        return new Intent(c, SettingActivity.class);
    }

    @Override
    protected Fragment onCreateFragment() {
        return PhotoSettingFragment.newInstance();
    }


}
