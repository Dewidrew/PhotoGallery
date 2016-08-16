package ayp.aug.photogallery;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Hattapong on 8/16/2016.
 */
public class PhotoGalleryFragment extends Fragment{
    public static PhotoGalleryFragment newInstance() {

        Bundle args = new Bundle();

        PhotoGalleryFragment fragment = new PhotoGalleryFragment();
        fragment.setArguments(args);
        return fragment;
    }

    private RecyclerView mRecyclerView;
    private FlickerFetcher mFlickerFetcher;
    private PhotoGalleryAdapter mAdapter;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_photo_gallery,container,false);

        mRecyclerView = (RecyclerView) v.findViewById(R.id.photo_gallery_recycler_view);
        mRecyclerView.setLayoutManager(new GridLayoutManager(getActivity(),3));
        mFlickerFetcher = new FlickerFetcher();

        new FetcherTask().execute();

        return v;
    }
    class PhotoGalleryViewHolder extends RecyclerView.ViewHolder{
        TextView mText;

        public PhotoGalleryViewHolder(View itemView) {
            super(itemView);
            mText = (TextView)itemView;
        }

        public void bindGalleryItem(GalleryItem galleryItem) {
            mText.setText(galleryItem.getTitle());
        }
    }
    class PhotoGalleryAdapter extends RecyclerView.Adapter<PhotoGalleryViewHolder>{
        List<GalleryItem> mGalleryItemList;

        public PhotoGalleryAdapter(List<GalleryItem> mGalleryItemList) {
            this.mGalleryItemList = mGalleryItemList;
        }

        @Override
        public PhotoGalleryViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(getActivity()).inflate(android.R.layout.simple_list_item_1,parent,false);
            return new PhotoGalleryViewHolder(v);
        }

        @Override
        public void onBindViewHolder(PhotoGalleryViewHolder holder, int position) {
            holder.bindGalleryItem(mGalleryItemList.get(position));

        }

        @Override
        public int getItemCount() {
            return mGalleryItemList.size();
        }
    }

    class FetcherTask extends AsyncTask<Void, Void, List<GalleryItem>>{

        @Override
        protected List<GalleryItem> doInBackground(Void... voids) {
            List<GalleryItem> itemList = new ArrayList<>();
            mFlickerFetcher.fetchItems(itemList);
            return itemList;
        }

        @Override
        protected void onPostExecute(List<GalleryItem> galleryItems) {

                mAdapter = new PhotoGalleryAdapter(galleryItems);
                mRecyclerView.setAdapter(mAdapter);

        }
    }
}
