package com.example.iamareebjamal.feddup.ui.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;

import com.example.iamareebjamal.feddup.FeddupApp;
import com.example.iamareebjamal.feddup.R;
import com.example.iamareebjamal.feddup.ui.fragment.DetailFragment;
import com.squareup.leakcanary.RefWatcher;

public class DetailActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

        DetailFragment detailFragment = (DetailFragment) getSupportFragmentManager()
                .findFragmentById(R.id.detail_fragment);

        setSupportActionBar(detailFragment.getToolbar());
        ActionBar ab = getSupportActionBar();
        if (ab != null) {
            ab.setDefaultDisplayHomeAsUpEnabled(true);
            ab.setDisplayHomeAsUpEnabled(true);
        }

        Intent extra = getIntent();
        if (extra.hasExtra(DetailFragment.KEY)) {
            String key = extra.getStringExtra(DetailFragment.KEY);

            detailFragment.setKey(key);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                break;
            default:
                // Do nothing
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        RefWatcher refWatcher = FeddupApp.getRefWatcher(this);
        refWatcher.watch(this);
    }

}
