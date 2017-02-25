package com.example.iamareebjamal.feddup.ui.fragment;

import android.content.Context;
import android.content.res.Configuration;
import android.database.Cursor;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import com.example.iamareebjamal.feddup.R;
import com.example.iamareebjamal.feddup.data.db.DatabaseProvider;
import com.example.iamareebjamal.feddup.data.db.utils.DownvotesHelper;
import com.example.iamareebjamal.feddup.data.db.utils.FavoritesHelper;
import com.example.iamareebjamal.feddup.data.models.Post;
import com.example.iamareebjamal.feddup.ui.viewholder.PostHolder;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;

import butterknife.BindView;
import butterknife.ButterKnife;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

public class MainFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {
    private static final String TAG = "MainFragment";

    private FragmentInteractionListener mListener;

    private static final int FAVORITE_LOADER = 1;
    private static final int DOWNVOTES_CURSOR = 2;

    private Cursor favoriteCursor;
    private Cursor downvotesCursor;

    private FavoritesHelper favoritesHelper;
    private DownvotesHelper downvotesHelper;

    @BindView(R.id.post_list) RecyclerView recyclerView;
    @BindView(R.id.empty_layout) FrameLayout emptyLayout;

    private FirebaseRecyclerAdapter<Post, PostHolder> postAdapter;

    public MainFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_main, container, false);

        ButterKnife.bind(this, root);

        setupList();

        return root;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        favoritesHelper = new FavoritesHelper(getContext());
        downvotesHelper = new DownvotesHelper(getContext());

        getActivity().getSupportLoaderManager().initLoader(FAVORITE_LOADER, null, this);
        getActivity().getSupportLoaderManager().initLoader(DOWNVOTES_CURSOR, null, this);
    }

    public boolean isTablet() {
        boolean xlarge = ((getContext().getResources().getConfiguration().screenLayout & Configuration.SCREENLAYOUT_SIZE_MASK) == 4);
        boolean large = ((getContext().getResources().getConfiguration().screenLayout & Configuration.SCREENLAYOUT_SIZE_MASK) == Configuration.SCREENLAYOUT_SIZE_LARGE);
        return (xlarge || large);
    }

    private boolean moveUp, started;
    public void setupList(){
        GridLayoutManager gridLayoutManager = new GridLayoutManager(getContext(), 1);

        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(gridLayoutManager);

        //if(getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE && !isTablet())
        //    gridLayoutManager.setSpanCount(2);

        Query postReference = FirebaseDatabase.getInstance().getReference("posts").orderByChild("downvotes").limitToFirst(10);

        PostHolder.setFragmentInteractionListener(mListener);
        postAdapter = new FirebaseRecyclerAdapter<Post, PostHolder>(Post.class, R.layout.item_card, PostHolder.class, postReference) {
            @Override
            protected void populateViewHolder(PostHolder viewHolder, Post post, int position) {
                if(!started) {
                    started = true;
                    if (mListener != null) mListener.onPostSelect(getRef(position).getKey());
                    emptyLayout.setVisibility(View.GONE);
                }

                viewHolder.setPost(post, getRef(position));
            }
        };

        recyclerView.setAdapter(postAdapter);
        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener(){
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy){
                if (dy > 0 || dy < 0)
                    onFabHide();

                moveUp = dy < 0;
            }

            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {

                if (newState == RecyclerView.SCROLL_STATE_IDLE && moveUp)
                    onFabShow();

                super.onScrollStateChanged(recyclerView, newState);
            }
        });
    }

    private void loadFavorites() {
        if(favoriteCursor == null) return;

        PostHolder.clearFavorites();
        favoritesHelper
                .getFavoritesFromCursor(favoriteCursor)
                .subscribeOn(Schedulers.computation())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(key -> {
                    PostHolder.addFavorite(key);
                    postAdapter.notifyDataSetChanged();
                }, throwable -> Log.d(TAG, "Cursor has closed"));

    }

    private void loadDownvotes() {
        if(downvotesCursor == null) return;

        PostHolder.clearDownVoted();
        downvotesHelper
                .getDowvotesFromCursor(downvotesCursor)
                .subscribeOn(Schedulers.computation())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(key -> {
                    PostHolder.addDownVoted(key);
                    postAdapter.notifyDataSetChanged();
                }, throwable -> Log.d(TAG, "Cursor has closed"));

    }

    private void onFabShow() {
        if (mListener != null) {
            mListener.onFabDisplay(true);
        }
    }

    private void onFabHide() {
        if (mListener != null) {
            mListener.onFabDisplay(false);
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof FragmentInteractionListener) {
            mListener = (FragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement FragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        if(id == FAVORITE_LOADER)
            return new CursorLoader(getContext(), DatabaseProvider.Favorites.CONTENT_URI, null, null, null, null);
        else
            return new CursorLoader(getContext(), DatabaseProvider.Downvotes.CONTENT_URI, null, null, null, null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        switch (loader.getId()) {
            case FAVORITE_LOADER:
                if(favoriteCursor != null) favoriteCursor.close();

                favoriteCursor = data;

                loadFavorites();
                break;
            case DOWNVOTES_CURSOR:
                if(downvotesCursor != null) downvotesCursor.close();

                downvotesCursor = data;

                loadDownvotes();
                break;
            default:
                // Do Nothing
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        if(favoriteCursor != null) favoriteCursor.close();
        if(downvotesCursor != null) downvotesCursor.close();
    }

    public interface FragmentInteractionListener {
        void onFabDisplay(boolean show);
        void onPostSelect(String key);
        void onPostStart(String key);
    }
}
