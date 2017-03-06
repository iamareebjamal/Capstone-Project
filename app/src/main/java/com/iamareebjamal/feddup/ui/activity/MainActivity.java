package com.iamareebjamal.feddup.ui.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import com.iamareebjamal.feddup.FeddupApp;
import com.iamareebjamal.feddup.R;
import com.iamareebjamal.feddup.ui.FragmentInteractionListener;
import com.iamareebjamal.feddup.ui.fragment.DetailFragment;
import com.iamareebjamal.feddup.ui.fragment.FavoriteFragment;
import com.iamareebjamal.feddup.ui.fragment.MainFragment;
import com.iamareebjamal.feddup.utils.Utils;
import com.squareup.leakcanary.RefWatcher;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class MainActivity extends AppCompatActivity implements FragmentInteractionListener {

    @BindView(com.iamareebjamal.feddup.R.id.toolbar) Toolbar toolbar;
    @BindView(R.id.fab) FloatingActionButton fab;

    private static final String FAVORITE = "favorite";
    private boolean showFavorite = false;

    private DetailFragment detailFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ButterKnife.bind(this);

        setSupportActionBar(toolbar);

        if(savedInstanceState != null)
            showFavorite = savedInstanceState.getBoolean(FAVORITE, true);

        toggleFragments();

        detailFragment = (DetailFragment) getSupportFragmentManager().findFragmentById(R.id.detail_fragment);

        Intent extra = getIntent();
        if (extra.hasExtra(DetailFragment.KEY)) {
            String key = extra.getStringExtra(DetailFragment.KEY);

            onPostSelect(key);
        }
    }

    public boolean isDualPane() {
        return detailFragment!=null && detailFragment.isInLayout();
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

        Utils.tintMenu(menu, ContextCompat.getColor(this, R.color.white_translucent));

        return true;
    }

    private void toggleFragments() {
        Fragment newFragment;
        if(showFavorite)
            newFragment = new FavoriteFragment();
        else
            newFragment = new MainFragment();

        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.replace(R.id.wrapper, newFragment);
        fragmentTransaction.commit();
        showFavorite = !showFavorite;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
            case R.id.drafts:
                loadDrafts();
                break;
            case R.id.favorites:
                toggleFragments();
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

    @Override
    public void onFabDisplay(boolean show) {
        if(show) {
            if(!fab.isShown()) fab.show();
        } else {
            if(fab.isShown()) fab.hide();
        }
    }

    @Override
    public void onPostSelect(String key) {
        if(isDualPane()) {
            detailFragment.setKey(key);
            return;
        }

        Intent intent = new Intent(this, DetailActivity.class);
        intent.putExtra(DetailFragment.KEY, key);

        startActivity(intent);
    }

    @Override
    public void onPostStart(String key) {
        if (isDualPane()) detailFragment.setKey(key);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putBoolean(FAVORITE, !showFavorite);
    }
}
