package david.com.popularmovies.adapters;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Color;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;

import david.com.popularmovies.R;
import david.com.popularmovies.db.FavMoviesContract;
import david.com.popularmovies.model.Movie;

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
    private ListItemClickListener onClickListener;
    private String[] mPosterPaths;
    private Cursor mCursor;
    private boolean isFavAdapter;
    private ArrayList<Movie> mMovieList;

    public MovieAdapter(String[] posterPaths, int numItems, ListItemClickListener clickListener){
        mNumItems = numItems;
        mPosterPaths = posterPaths;
        onClickListener = clickListener;
        isFavAdapter = false;
    }

    public MovieAdapter(Context context, Cursor cursor, ListItemClickListener clickListener, String[] posterPaths){
        mNumItems = cursor.getCount();
        mPosterPaths = posterPaths;
        this.context = context;
        mCursor = cursor;
        isFavAdapter = true;
        onClickListener = clickListener;
    }

    public MovieAdapter(ArrayList<Movie> newMovieList, String[] posterPaths, Context context, ListItemClickListener clickListener) {
        mNumItems = posterPaths.length;
        mMovieList = newMovieList;
        mPosterPaths = posterPaths;
        this.context = context;
        isFavAdapter = true;
        onClickListener = clickListener;
    }

    @Override
    public MovieAdapterViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        Log.d(TAG, "entering onCreateViewHolder");
        context = parent.getContext();
        int layoutIdForListItem = 0;

        if(!isFavAdapter){
            layoutIdForListItem = R.layout.thumbnail_layout;
        }else if(isFavAdapter){
            layoutIdForListItem = R.layout.fav_layout;
        }

        LayoutInflater inflater = LayoutInflater.from(context);

        View view = inflater.inflate(layoutIdForListItem, parent, false);
        Log.d(TAG, "exiting onCreateViewHolder");
        return new MovieAdapterViewHolder(view);
    }


    @Override
    public void onBindViewHolder(MovieAdapterViewHolder holder, int position) {
        Log.d(TAG, "entering onBindViewHolder. Position is: " + position + ", isFavAdapter ==  " + isFavAdapter);

        if(!isFavAdapter){
            Log.d(TAG, mPosterPaths[position]);
            Picasso.with(context).load(mPosterPaths[position]).into(holder.mImageView);
        }else if(isFavAdapter){
            if(mCursor == null){
                holder.mFavImageView.setImageResource(R.mipmap.movie_projector);
                holder.mTextView.setText(mMovieList.get(position).getmTitle());
                holder.mTextView.setBackgroundColor(context.getResources().getColor(R.color.colorAccent));
                //TODO SUGGESTION Try avoid using deprecated code it may break your app in the future, consider using getColor(Color, Theme)
            }else{
                if(!mCursor.moveToPosition(position)){
                    return;
                }
                String title = mCursor.getString(mCursor.getColumnIndex(FavMoviesContract.FavMovieEntry.COLUMN_TITLE));
                String rating = mCursor.getString(mCursor.getColumnIndex(FavMoviesContract.FavMovieEntry.COLUMN_RATING));
                String year = mCursor.getString(mCursor.getColumnIndex(FavMoviesContract.FavMovieEntry.COLUMN_YEAR));
                String summary = mCursor.getString(mCursor.getColumnIndex(FavMoviesContract.FavMovieEntry.COLUMN_SUMMARY));
                String trailer = mCursor.getString(mCursor.getColumnIndex(FavMoviesContract.FavMovieEntry.COLUMN_TRAILER));
                String review = mCursor.getString(mCursor.getColumnIndex(FavMoviesContract.FavMovieEntry.COLUMN_REVIEW));

                Log.d(TAG, "all details retrieved from DB are: " + title + " : " + rating + " : " + year + " : " + summary + " : " + trailer + " : " + review);

                holder.mFavImageView.setImageResource(R.mipmap.movie_projector);
                holder.mTextView.setText(title);
                holder.mTextView.setBackgroundColor(context.getResources().getColor(R.color.colorAccent));
            }
        }
        Log.d(TAG, "exiting onBindViewHolder");
    }

    @Override
    public int getItemCount() {
        if(isFavAdapter){
            Log.d(TAG, "entering getItemCount in fav adapater. itemCount is: " + mNumItems);
            return mNumItems;
        }else{
            Log.d(TAG, "entering getItemCount in normal adapater. itemCount is: " + mPosterPaths.length);
            return mNumItems;
        }
    }

    public class MovieAdapterViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        public ImageView mImageView;
        public ImageView mFavImageView;
        public TextView mTextView;

        public MovieAdapterViewHolder(View itemView) {
            super(itemView);
            if(!isFavAdapter){
                mImageView = (ImageView) itemView.findViewById(R.id.item_imageView);
            }else if(isFavAdapter){
                mFavImageView = (ImageView) itemView.findViewById(R.id.fav_layout_img);
                mTextView = (TextView) itemView.findViewById(R.id.fav_layout_txt);
            }
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
