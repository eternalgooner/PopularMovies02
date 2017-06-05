package david.com.popularmovies;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.GridLayout;
import android.widget.GridView;
import android.widget.ImageView;

import com.squareup.picasso.Picasso;

/**
 * Created by David on 13-May-17.
 *
 * Class that creates an Adapter & a ViewHolder to bind data & hold view objects to recycle
 * - has inner class MovieAdapterViewHolder
 * - has inner interface ListItemClickListener
 *
 * ATTRIBUTION:
 * - some code was implemented with help from Udacity Android course
 *
 */

public class MovieAdapter extends RecyclerView.Adapter<MovieAdapter.MovieAdapterViewHolder> {

    private Context context;
    private int mNumItems;
    private static final String TAG = MovieAdapter.class.getSimpleName();
    private final ListItemClickListener onClickListener;
    private String[] mPosterPaths;

    public MovieAdapter(String[] posterPaths, int numItems, ListItemClickListener clickListener){
        mNumItems = numItems;
        mPosterPaths = posterPaths;
        onClickListener = clickListener;
    }

    @Override
    public MovieAdapterViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        Log.d(TAG, "entering onCreateViewHolder");
        context = parent.getContext();
        int layoutIdForListItem = R.layout.thumbnail_layout;
        LayoutInflater inflater = LayoutInflater.from(context);

        View view = inflater.inflate(layoutIdForListItem, parent, false);
        Log.d(TAG, "exiting onCreateViewHolder");
        return new MovieAdapterViewHolder(view);
    }


    @Override
    public void onBindViewHolder(MovieAdapterViewHolder holder, int position) {
        Log.d(TAG, "entering onBindViewHolder");
        Log.d(TAG, mPosterPaths[position]);
        //int width = context.getResources().getDisplayMetrics().widthPixels;
        Picasso.with(context).load(mPosterPaths[position]).into(holder.mImageView);
        Log.d(TAG, "exiting onBindViewHolder");
    }

    @Override
    public int getItemCount() {
        Log.d(TAG, "entering getItemCount. itemCount is: " + mPosterPaths.length);
        return mNumItems;
    }

    public class MovieAdapterViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        public final ImageView mImageView;
        public final FrameLayout frameLayout;

        public MovieAdapterViewHolder(View itemView) {
            super(itemView);
            frameLayout = (FrameLayout) itemView.findViewById(R.id.gv_item_view);
            mImageView = (ImageView) itemView.findViewById(R.id.item_imageView);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            Log.d(TAG, "entering onClick in MovieAdapterViewHolder");
            int clickedPosition = getAdapterPosition();
            onClickListener.onListItemClick(clickedPosition);
        }
    }

    public interface ListItemClickListener{
        void onListItemClick(int clickedItem);
    }
}
