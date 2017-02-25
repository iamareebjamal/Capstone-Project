package com.example.iamareebjamal.feddup.ui.activity;

import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.content.ContextCompat;
import android.support.v4.graphics.drawable.DrawableCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import com.example.iamareebjamal.feddup.FeddupApp;
import com.example.iamareebjamal.feddup.R;
import com.example.iamareebjamal.feddup.ui.fragment.DetailFragment;
import com.example.iamareebjamal.feddup.ui.fragment.MainFragment;
import com.squareup.leakcanary.RefWatcher;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class MainActivity extends AppCompatActivity implements MainFragment.FragmentInteractionListener {

    @BindView(com.example.iamareebjamal.feddup.R.id.toolbar) Toolbar toolbar;
    @BindView(R.id.fab) FloatingActionButton fab;

    DetailFragment detailFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ButterKnife.bind(this);

        setSupportActionBar(toolbar);

        detailFragment = (DetailFragment) getSupportFragmentManager().findFragmentById(R.id.detail_fragment);
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

        for(int i = 0; i < menu.size(); i++) {
            Drawable drawable = menu.getItem(i).getIcon();
            drawable = DrawableCompat.wrap(drawable);
            DrawableCompat.setTint(drawable, ContextCompat.getColor(this, R.color.white_translucent));
            menu.getItem(i).setIcon(drawable);
        }

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
}
