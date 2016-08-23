package ayp.aug.photogallery;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.v4.app.DialogFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import java.io.IOException;

import ayp.aug.photogallery.PhotoGalleryFragment.PhotoHolder;

/**
 * Created by Hattapong on 8/22/2016.
 */


public class ShowPictureDialog extends DialogFragment {
    private static final String TAG = "URL_PICTURE";
    private static final String SIZE = "b";

    public static ShowPictureDialog newInstance(String url) {

        Bundle args = new Bundle();
        args.putString(TAG, url);
        ShowPictureDialog fragment = new ShowPictureDialog();
        fragment.setArguments(args);
        return fragment;
    }

    private String url;
    private ImageView img;

    @Override
    public void onStart() {
        super.onStart();
        Dialog dialog = getDialog();
        if (dialog != null)
        {
            int width = ViewGroup.LayoutParams.MATCH_PARENT;
            int height = ViewGroup.LayoutParams.MATCH_PARENT;
            dialog.getWindow().setLayout(width, height);
        }

    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        url = transferUrl(getArguments().getString(TAG, null));
        Log.d("NEW TAGE PHOTO",url);
        View v = LayoutInflater.from(getActivity()).inflate(R.layout.show_photo, null);
        img = (ImageView) v.findViewById(R.id.image_photo);
        new AsyncTask<String, Void ,Bitmap>(){

            @Override
            protected Bitmap doInBackground(String... strings) {
                FlickerFetcher flickerFetcher = new FlickerFetcher();
                Bitmap bm = null;
                try{
                    byte[] bitMapBytes = flickerFetcher.getUrlBytes(url);
                    bm = BitmapFactory.decodeByteArray(bitMapBytes,0,bitMapBytes.length);

                }catch (IOException e){
                    e.printStackTrace();
                }
                return bm;
            }

            @Override
            protected void onPostExecute(Bitmap bitmap) {
                super.onPostExecute(bitmap);
                img.setImageBitmap(bitmap);
            }
        }.execute(url);





        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setView(v);
        builder.setPositiveButton("CLOSE", null);


        return builder.create();
    }



    private String transferUrl(String url) {
        return url.substring(0, url.length() - 5) + SIZE + url.substring(url.length() - 4, url.length());
    }

}
