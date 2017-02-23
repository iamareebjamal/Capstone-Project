package com.example.iamareebjamal.feddup.ui.fragment;

import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.support.v4.graphics.drawable.DrawableCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.graphics.Palette;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.iamareebjamal.feddup.R;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * A placeholder fragment containing a simple view.
 */
public class DetailActivityFragment extends Fragment {
    @BindView(R.id.toolbar)
    Toolbar mToolbar;

    @BindView(R.id.backdrop)
    ImageView backdrop;

    @BindView(R.id.article_title)
    TextView title;

    @BindView(R.id.author)
    TextView author;

    @BindView(R.id.article_body)
    TextView body;

    @BindView(R.id.date)
    TextView date;

    @BindView(R.id.downvotes)
    TextView downvotes;

    @BindView(R.id.title_photo)
    ImageView title_icon;

    @BindView(R.id.author_photo)
    ImageView author_icon;

    @BindView(R.id.date_photo)
    ImageView date_icon;

    @BindView(R.id.downvote_iphoto)
    ImageView downvote_icon;

    @BindView(R.id.meta_bar)
    LinearLayout panel;


    public DetailActivityFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View root =  inflater.inflate(R.layout.fragment_detail, container, false);

        ButterKnife.bind(this, root);

        ((AppCompatActivity)getActivity()).setSupportActionBar(mToolbar);
        mToolbar.setTitle("");

        Picasso.with(getContext())
                .load("http://netdna.webdesignerdepot.com/uploads/2015/07/featured_mdl.jpg")
                .into(backdrop, new Callback.EmptyCallback(){
                    @Override
                    public void onSuccess() {
                        Bitmap bitmap = ((BitmapDrawable) backdrop.getDrawable()).getBitmap();
                        Palette.from(bitmap).maximumColorCount(16).generate(new Palette.PaletteAsyncListener() {
                            @Override
                            public void onGenerated(Palette palette) {
                                Palette.Swatch swatch = palette.getVibrantSwatch();
                                if(swatch != null) {
                                    panel.setBackgroundColor(swatch.getRgb());
                                    DrawableCompat.setTint(DrawableCompat.wrap(title_icon.getDrawable()), swatch.getBodyTextColor());
                                    DrawableCompat.setTint(DrawableCompat.wrap(date_icon.getDrawable()), swatch.getBodyTextColor());
                                    DrawableCompat.setTint(DrawableCompat.wrap(author_icon.getDrawable()), swatch.getBodyTextColor());
                                    DrawableCompat.setTint(DrawableCompat.wrap(downvote_icon.getDrawable()), swatch.getBodyTextColor());

                                    title.setTextColor(swatch.getTitleTextColor());
                                    date.setTextColor(swatch.getTitleTextColor());
                                    author.setTextColor(swatch.getTitleTextColor());
                                    downvotes.setTextColor(swatch.getTitleTextColor());
                                }
                            }
                        });
                    }
                });

        return root;
    }
}
