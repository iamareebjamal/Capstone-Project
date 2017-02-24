package com.example.iamareebjamal.feddup.ui.fragment;

import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.graphics.drawable.VectorDrawableCompat;
import android.support.v4.app.Fragment;
import android.support.v4.graphics.drawable.DrawableCompat;
import android.support.v4.widget.NestedScrollView;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.graphics.Palette;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.example.iamareebjamal.feddup.R;
import com.example.iamareebjamal.feddup.data.models.Post;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

import butterknife.BindView;
import butterknife.ButterKnife;

public class DetailFragment extends Fragment {

    private static final String TAG = "DetailFragment";
    public static final String KEY = "key";
    private String key;

    @BindView(R.id.main_content) CoordinatorLayout rootLayout;
    @BindView(R.id.detail_view) NestedScrollView detailView;
    @BindView(R.id.toolbar) Toolbar mToolbar;
    @BindView(R.id.backdrop) ImageView backdrop;
    @BindView(R.id.article_title) TextView title;
    @BindView(R.id.author) TextView author;
    @BindView(R.id.article_body) TextView body;
    @BindView(R.id.date) TextView date;
    @BindView(R.id.downvotes) TextView downvotes;
    @BindView(R.id.meta_bar) LinearLayout panel;
    @BindView(R.id.progress) ProgressBar progressBar;

    private Query query;
    private ValueEventListener valueEventListener;

    public DetailFragment() { }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View root =  inflater.inflate(R.layout.fragment_detail, container, false);
        ButterKnife.bind(this, root);

        ((AppCompatActivity) getActivity()).setSupportActionBar(mToolbar);

        return root;
    }

    private String getDateTime(long timestamp) {
        SimpleDateFormat formatter = new SimpleDateFormat("hh:mm a, E d MMM yyyy", Locale.getDefault());
        formatter.setTimeZone(TimeZone.getDefault());
        Date date = new Date(timestamp);

        return formatter.format(date);
    }

    public void setKey(String key) {
        this.key = key;
        loadArticle();
    }

    private void loadArticle() {
        if (key == null) {
            Snackbar.make(rootLayout, "No Article key provided", Snackbar.LENGTH_LONG).show();
            return;
        }

        progressBar.setVisibility(View.VISIBLE);

        query = FirebaseDatabase.getInstance().getReference("posts/"+key);

        valueEventListener = query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Post post = dataSnapshot.getValue(Post.class);

                if (post == null) {
                    progressBar.setVisibility(View.GONE);
                    Snackbar.make(rootLayout, "Post Deleted", Snackbar.LENGTH_INDEFINITE).show();
                    detailView.setVisibility(View.GONE);

                    backdrop.setImageDrawable(VectorDrawableCompat.create(getContext().getResources(), R.drawable.ic_photo, null));
                    return;
                }

                detailView.setVisibility(View.VISIBLE);
                setBackdrop(post.url);

                title.setText(post.title);
                author.setText(post.user);
                body.setText(post.content);
                downvotes.setText(String.valueOf(post.downvotes));
                date.setText(getDateTime(post.time));
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                progressBar.setVisibility(View.GONE);
                Log.d(TAG, databaseError.getMessage());
            }
        });

    }

    private void setBackdrop(String photoUrl) {
        Picasso.with(getContext())
                .load(photoUrl)
                .placeholder(VectorDrawableCompat.create(getContext().getResources(), R.drawable.ic_photo, null))
                .into(backdrop, new Callback.EmptyCallback(){
                    @Override
                    public void onSuccess() {
                        progressBar.setVisibility(View.GONE);

                        Bitmap bitmap = ((BitmapDrawable) backdrop.getDrawable()).getBitmap();
                        Palette.from(bitmap).generate(palette -> {
                            Palette.Swatch swatch = palette.getVibrantSwatch();
                            if(swatch != null) {
                                panel.setBackgroundColor(swatch.getRgb());
                                DrawableCompat.setTint(DrawableCompat.wrap(title.getCompoundDrawables()[0]), swatch.getBodyTextColor());
                                DrawableCompat.setTint(DrawableCompat.wrap(date.getCompoundDrawables()[0]), swatch.getBodyTextColor());
                                DrawableCompat.setTint(DrawableCompat.wrap(author.getCompoundDrawables()[0]), swatch.getBodyTextColor());
                                DrawableCompat.setTint(DrawableCompat.wrap(downvotes.getCompoundDrawables()[0]), swatch.getBodyTextColor());

                                title.setTextColor(swatch.getTitleTextColor());
                                date.setTextColor(swatch.getTitleTextColor());
                                author.setTextColor(swatch.getTitleTextColor());
                                downvotes.setTextColor(swatch.getTitleTextColor());
                            }
                        });
                    }
                });
    }

    @Override
    public void onDetach() {
        if(query != null) query.removeEventListener(valueEventListener);

        super.onDetach();
    }
}
