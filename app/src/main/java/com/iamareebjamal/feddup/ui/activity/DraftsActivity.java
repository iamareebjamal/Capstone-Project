package com.iamareebjamal.feddup.ui.activity;

import android.content.Intent;
import android.content.res.Configuration;
import android.database.Cursor;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;

import com.iamareebjamal.feddup.FeddupApp;
import com.iamareebjamal.feddup.R;
import com.iamareebjamal.feddup.data.db.DatabaseProvider;
import com.iamareebjamal.feddup.data.db.schema.DraftColumns;
import com.iamareebjamal.feddup.data.db.utils.DraftsHelper;
import com.iamareebjamal.feddup.data.models.PostDraft;
import com.iamareebjamal.feddup.ui.adapter.DraftsAdapter;
import com.squareup.leakcanary.RefWatcher;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

public class DraftsActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    private static final String TAG = "DraftsActivity";

    @BindView(R.id.toolbar) Toolbar toolbar;
    @BindView(R.id.draft_list) RecyclerView recyclerView;
    @BindView(R.id.fab) FloatingActionButton fab;

    private List<PostDraft> drafts = new ArrayList<>();
    private DraftsAdapter draftsAdapter = new DraftsAdapter(drafts);

    private boolean moveUp;
    private Subscription subscription;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_drafts);

        ButterKnife.bind(this);

        setSupportActionBar(toolbar);
        ActionBar ab = getSupportActionBar();
        if (ab != null) {
            ab.setDisplayHomeAsUpEnabled(true);
            ab.setDefaultDisplayHomeAsUpEnabled(true);
        }

        getSupportLoaderManager().initLoader(1, null, this);

        setupList();
    }

    private void setupList() {
        GridLayoutManager gridLayoutManager = new GridLayoutManager(this, 1);

        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(gridLayoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());

        if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE)
            gridLayoutManager.setSpanCount(2);

        recyclerView.setAdapter(draftsAdapter);

        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
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

    private void loadData(Cursor cursor) {
        drafts.clear();
        draftsAdapter.notifyDataSetChanged();

        if (cursor == null) return;

        if (subscription != null) subscription.unsubscribe();

        subscription = DraftsHelper.getDraftsFromCursor(cursor)
                .subscribeOn(Schedulers.computation())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(postService -> {
                    drafts.add(postService);
                    draftsAdapter.notifyItemInserted(drafts.size());
                }, throwable -> Log.d(TAG, "Cursor has closed"));

    }

    @OnClick(R.id.fab)
    public void startPostActivity() {
        startActivity(new Intent(this, PostActivity.class));
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
            default:
                // Do nothing
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        return new CursorLoader(this,
                DatabaseProvider.Drafts.CONTENT_URI,
                null,
                null,
                null,
                DraftColumns._ID + " DESC");
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        loadData(data);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        // No need to close cursor. LoaderManager handles it itself
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        if (subscription != null) subscription.unsubscribe();

        RefWatcher refWatcher = FeddupApp.getRefWatcher(this);
        refWatcher.watch(this);
    }
}
