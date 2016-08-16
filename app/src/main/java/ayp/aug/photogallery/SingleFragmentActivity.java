package ayp.aug.photogallery;

import android.os.Bundle;
import android.support.annotation.LayoutRes;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;

/**
 * Created by Hattapong on 8/16/2016.
 */
public abstract class SingleFragmentActivity extends AppCompatActivity {
        private static final String TAG = "SingleFragmentActivity";
            @LayoutRes
            protected int getLayoutResId() {
                return R.layout.activity_single_fragment;
            }
            @Override
            protected void onCreate(Bundle savedInstanceState) {
                super.onCreate(savedInstanceState);
                setContentView(getLayoutResId());


                //Create Fragment Container
                FragmentManager fm = getSupportFragmentManager();
                Fragment f = fm.findFragmentById(R.id.fragment_container);


                if (f == null) {
                    f = onCreateFragment(); // Create Fragment

                    // Set Fragment to Fragment Container
                    fm.beginTransaction()
                            .add(R.id.fragment_container, f)
                            .commit();

                }

            }

            protected abstract Fragment onCreateFragment();
}
