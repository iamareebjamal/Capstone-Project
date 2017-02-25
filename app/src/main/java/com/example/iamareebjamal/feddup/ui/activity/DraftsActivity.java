package com.example.iamareebjamal.feddup.ui.activity;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;

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

    @BindView(R.id.toolbar) Toolbar toolbar;
    @BindView(R.id.draft_list) RecyclerView recyclerView;

    private DraftsHelper db = new DraftsHelper(this);
    private Cursor cursor;

    private List<PostDraft> drafts = new ArrayList<>();
    private DraftsAdapter draftsAdapter = new DraftsAdapter(this, drafts);

    private CompositeSubscription compositeSubscription = new CompositeSubscription();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_drafts);

        ButterKnife.bind(this);

        setSupportActionBar(toolbar);

        getSupportLoaderManager().initLoader(1, null, this);

        setupList();
    }

    private void setupList() {
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setItemAnimator(new DefaultItemAnimator());

        recyclerView.setAdapter(draftsAdapter);
    }

    private void loadData() {
        drafts.clear();
        draftsAdapter.notifyDataSetChanged();

        Subscription dbSubscription = db.getDraftsFromCursor(cursor)
                .subscribeOn(Schedulers.computation())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(postService -> {
                    drafts.add(postService);
                    draftsAdapter.notifyDataSetChanged();
                });

        compositeSubscription.add(dbSubscription);
    }

    @OnClick(R.id.fab)
    public void startPostActivity() {
       startActivity(new Intent(this, PostActivity.class));
    }

    @Override
    protected void onDestroy() {
        if(cursor != null) cursor.close();
        if(compositeSubscription != null) compositeSubscription.unsubscribe();

        super.onDestroy();
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
