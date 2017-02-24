package com.example.iamareebjamal.feddup.ui.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.example.iamareebjamal.feddup.R;
import com.example.iamareebjamal.feddup.ui.fragment.DetailFragment;

public class DetailActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);


        Intent extra = getIntent();
        if(extra.hasExtra(DetailFragment.KEY)) {
            String key = extra.getStringExtra(DetailFragment.KEY);

            DetailFragment detailFragment = (DetailFragment) getSupportFragmentManager().findFragmentById(R.id.detail_fragment);
            detailFragment.setKey(key);
        }
    }

}
