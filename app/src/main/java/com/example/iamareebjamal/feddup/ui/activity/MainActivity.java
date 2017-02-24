package com.example.iamareebjamal.feddup.ui.activity;

import android.content.Intent;
import android.content.res.Configuration;
import android.database.Cursor;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

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
import butterknife.OnClick;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

public class MainActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    private static final int FAVORITE_LOADER = 1;
    private static final int DOWNVOTES_CURSOR = 2;

    private Cursor favoriteCursor;
    private Cursor downvotesCursor;

    private FavoritesHelper favoritesHelper = new FavoritesHelper(this);
    private DownvotesHelper downvotesHelper = new DownvotesHelper(this);

    @BindView(com.example.iamareebjamal.feddup.R.id.toolbar) Toolbar toolbar;
    @BindView(R.id.post_list) RecyclerView recyclerView;
    @BindView(R.id.fab) FloatingActionButton fab;

    FirebaseRecyclerAdapter<Post, PostHolder> postAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ButterKnife.bind(this);

        setSupportActionBar(toolbar);

        getSupportLoaderManager().initLoader(FAVORITE_LOADER, null, this);
        getSupportLoaderManager().initLoader(DOWNVOTES_CURSOR, null, this);
        setupList();
    }

    private boolean moveUp;
    public void setupList(){
        GridLayoutManager gridLayoutManager = new GridLayoutManager(this, 1);

        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(gridLayoutManager);

        if(getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE)
            gridLayoutManager.setSpanCount(2);

        Query postReference = FirebaseDatabase.getInstance().getReference("posts").orderByChild("downvotes").limitToFirst(10);

        postAdapter = new FirebaseRecyclerAdapter<Post, PostHolder>(Post.class, R.layout.item_card, PostHolder.class, postReference) {
            @Override
            protected void populateViewHolder(PostHolder viewHolder, Post post, int position) {
                viewHolder.setPost(post, getRef(position));
            }
        };

        recyclerView.setAdapter(postAdapter);
        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener(){
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy){
                if (dy > 0 || dy < 0 && fab.isShown())
                    fab.hide();

                moveUp = dy < 0;
            }

            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {

                if (newState == RecyclerView.SCROLL_STATE_IDLE && moveUp)
                    fab.show();

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
                });
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
                });

    }


    private void loadDrafts() {
        startActivity(new Intent(this, DraftsActivity.class));
    }

    @OnClick(R.id.fab)
    public void startPostActivity(){
        startActivity(new Intent(this, PostActivity.class));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
            case R.id.drafts:
                loadDrafts();
                break;
            default:
                // Do nothing
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        if(id == FAVORITE_LOADER)
            return new CursorLoader(this, DatabaseProvider.Favorites.CONTENT_URI, null, null, null, null);
        else
            return new CursorLoader(this, DatabaseProvider.Downvotes.CONTENT_URI, null, null, null, null);
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
    }
}
