package com.iamareebjamal.feddup.ui.fragment;

import android.content.Context;
import android.content.res.Configuration;
import android.database.Cursor;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import com.iamareebjamal.feddup.FeddupApp;
import com.iamareebjamal.feddup.R;
import com.iamareebjamal.feddup.data.db.DatabaseProvider;
import com.iamareebjamal.feddup.data.db.utils.DatabaseHelper;
import com.iamareebjamal.feddup.data.db.utils.DownvotesHelper;
import com.iamareebjamal.feddup.data.db.utils.FavoritesHelper;
import com.iamareebjamal.feddup.data.models.Post;
import com.iamareebjamal.feddup.ui.FragmentInteractionListener;
import com.iamareebjamal.feddup.ui.viewholder.PostHolder;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.squareup.leakcanary.RefWatcher;
import com.squareup.picasso.Picasso;

import butterknife.BindView;
import butterknife.ButterKnife;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import rx.subscriptions.CompositeSubscription;

public class MainFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {
    private static final String TAG = "MainFragment";

    private FragmentInteractionListener mListener;

    private static final int FAVORITE_LOADER = 1;
    private static final int DOWNVOTES_CURSOR = 2;

    private Cursor favoriteCursor;
    private Cursor downvotesCursor;

    private CompositeSubscription compositeSubscription;

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

        ActionBar actionBar = ((AppCompatActivity)getActivity()).getSupportActionBar();
        if(actionBar != null) actionBar.setTitle(getString(R.string.app_name));

        setupList();

        return root;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        compositeSubscription = new CompositeSubscription();

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
                String key = getRef(position).getKey();

                if(!started) {
                    started = true;
                    if (mListener != null) mListener.onPostStart(key);
                    emptyLayout.setVisibility(View.GONE);
                }

                post.key = key;
                viewHolder.setPost(post);
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

        DatabaseHelper.clearFavorites();
        Subscription subscription = FavoritesHelper
                .getFavoritesFromCursor(favoriteCursor)
                .subscribeOn(Schedulers.computation())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(key -> {
                    DatabaseHelper.addFavorite(key);
                    postAdapter.notifyDataSetChanged();
                }, throwable -> Log.d(TAG, "Cursor has closed"));

        compositeSubscription.add(subscription);
    }

    private void loadDownvotes() {
        if(downvotesCursor == null) return;

        DatabaseHelper.clearDownVoted();
        Subscription subscription = DownvotesHelper
                .getDowvotesFromCursor(downvotesCursor)
                .subscribeOn(Schedulers.computation())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(key -> {
                    DatabaseHelper.addDownVoted(key);
                    postAdapter.notifyDataSetChanged();
                }, throwable -> Log.d(TAG, "Cursor has closed"));

        compositeSubscription.add(subscription);
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
        postAdapter.cleanup();
        PostHolder.setFragmentInteractionListener(null);
        Picasso.with(getContext()).cancelTag(PostHolder.TAG);

        if(compositeSubscription != null) compositeSubscription.unsubscribe();
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
        DatabaseHelper.clearDownVoted();
        DatabaseHelper.clearFavorites();
        // No need to close cursor. Handled by loader
    }

    @Override public void onDestroyView() {
        super.onDestroyView();
        RefWatcher refWatcher = FeddupApp.getRefWatcher(getActivity());
        refWatcher.watch(this);
    }

}
