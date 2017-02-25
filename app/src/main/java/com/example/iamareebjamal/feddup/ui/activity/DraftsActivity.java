package com.example.iamareebjamal.feddup.ui.activity;

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
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;

import com.example.iamareebjamal.feddup.R;
import com.example.iamareebjamal.feddup.data.db.DatabaseProvider;
import com.example.iamareebjamal.feddup.data.db.schema.DraftColumns;
import com.example.iamareebjamal.feddup.data.db.utils.DraftsHelper;
import com.example.iamareebjamal.feddup.data.models.PostDraft;
import com.example.iamareebjamal.feddup.ui.adapter.DraftsAdapter;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import rx.subscriptions.CompositeSubscription;

public class DraftsActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    private static final String TAG = "DraftsActivity";

    @BindView(R.id.toolbar) Toolbar toolbar;
    @BindView(R.id.draft_list) RecyclerView recyclerView;
    @BindView(R.id.fab) FloatingActionButton fab;

    private DraftsHelper db = new DraftsHelper(this);
    private Cursor cursor;

    private List<PostDraft> drafts = new ArrayList<>();
    private DraftsAdapter draftsAdapter = new DraftsAdapter(this, drafts);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_drafts);

        ButterKnife.bind(this);

        setSupportActionBar(toolbar);
        ActionBar ab = getSupportActionBar();
        if(ab != null) {
            ab.setDisplayHomeAsUpEnabled(true);
            ab.setDefaultDisplayHomeAsUpEnabled(true);
        }

        getSupportLoaderManager().initLoader(1, null, this);

        setupList();
    }

    private boolean moveUp;
    private void setupList() {
        GridLayoutManager gridLayoutManager = new GridLayoutManager(this, 1);

        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(gridLayoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());

        if(getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE)
            gridLayoutManager.setSpanCount(2);

        recyclerView.setAdapter(draftsAdapter);

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

    private void loadData() {
        drafts.clear();
        draftsAdapter.notifyDataSetChanged();

        db.getDraftsFromCursor(cursor)
                .subscribeOn(Schedulers.computation())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(postService -> {
                    drafts.add(postService);
                    draftsAdapter.notifyDataSetChanged();
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
        return new CursorLoader(this, DatabaseProvider.Drafts.CONTENT_URI, null, null, null, DraftColumns._ID + " DESC");
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        if(cursor != null) cursor.close();

        cursor = data;
        loadData();
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        if(cursor != null) cursor.close();
    }
}
