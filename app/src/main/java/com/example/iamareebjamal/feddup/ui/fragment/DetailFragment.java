package com.example.iamareebjamal.feddup.ui.fragment;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.graphics.drawable.VectorDrawableCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v4.graphics.drawable.DrawableCompat;
import android.support.v4.widget.NestedScrollView;
import android.support.v7.graphics.Palette;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.example.iamareebjamal.feddup.R;
import com.example.iamareebjamal.feddup.data.db.utils.DownvotesHelper;
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
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.schedulers.Schedulers;
import rx.subscriptions.CompositeSubscription;

public class DetailFragment extends Fragment {

    private static final String TAG = "DetailFragment";
    public static final String KEY = "key";
    private String key;

    @BindView(R.id.main_content) CoordinatorLayout rootLayout;
    @BindView(R.id.detail_view) NestedScrollView detailView;
    @BindView(R.id.collapsing_toolbar) CollapsingToolbarLayout collapsingToolbarLayout;
    @BindView(R.id.toolbar) Toolbar toolbar;
    @BindView(R.id.backdrop) ImageView backdrop;
    @BindView(R.id.article_title) TextView title;
    @BindView(R.id.author) TextView author;
    @BindView(R.id.article_body) TextView body;
    @BindView(R.id.date) TextView date;
    @BindView(R.id.downvotes) TextView downvotes;
    @BindView(R.id.meta_bar) LinearLayout panel;
    @BindView(R.id.progress) ProgressBar progressBar;
    @BindView(R.id.downvote) FloatingActionButton downvote;
    @BindView(R.id.empty_layout) FrameLayout emptyLayout;

    private Query query;
    private Post post;
    private ValueEventListener valueEventListener;
    private DownvotesHelper downvotesHelper;

    private CompositeSubscription compositeSubscription;

    public DetailFragment() { }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View root =  inflater.inflate(R.layout.fragment_detail, container, false);
        ButterKnife.bind(this, root);

        collapsingToolbarLayout.setExpandedTitleColor(ContextCompat.getColor(getContext(), R.color.white_transparent));

        downvote.hide();
        return root;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        downvotesHelper = new DownvotesHelper(getContext());
    }

    public Toolbar getToolbar() {
        return toolbar;
    }

    private String getDateTime(long timestamp) {
        SimpleDateFormat formatter = new SimpleDateFormat("hh:mm a, E d MMM yyyy", Locale.getDefault());
        formatter.setTimeZone(TimeZone.getDefault());
        Date date = new Date(timestamp);

        return formatter.format(date);
    }

    public void setKey(String key) {
        this.key = key;
        emptyLayout.setVisibility(View.GONE);

        if(compositeSubscription != null) compositeSubscription.unsubscribe();

        compositeSubscription = new CompositeSubscription();

        loadArticle();
    }

    private void loadArticle() {
        if (key == null) {
            Snackbar.make(rootLayout, "No Article key provided", Snackbar.LENGTH_LONG).show();
            return;
        }

        if (query != null) query.removeEventListener(valueEventListener);

        progressBar.setVisibility(View.VISIBLE);

        query = FirebaseDatabase.getInstance().getReference("posts/"+key);

        valueEventListener = query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                post = dataSnapshot.getValue(Post.class);

                if (post == null) {
                    progressBar.setVisibility(View.GONE);
                    Snackbar.make(rootLayout, "Post Deleted", Snackbar.LENGTH_INDEFINITE).show();
                    detailView.setVisibility(View.GONE);

                    backdrop.setImageDrawable(VectorDrawableCompat.create(getContext().getResources(), R.drawable.ic_photo, null));
                    return;
                }

                downvote.show();
                detailView.setVisibility(View.VISIBLE);
                setBackdrop(post.url);

                collapsingToolbarLayout.setTitle(post.title);
                title.setText(post.title);
                author.setText(post.user);
                body.setText(post.content);
                downvotes.setText(String.valueOf(post.downvotes));
                date.setText(getDateTime(post.time));

                loadArticleStatus(key);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                progressBar.setVisibility(View.GONE);
                Log.d(TAG, databaseError.getMessage());
            }
        });

    }

    private Action1<Throwable> throwableHandler = throwable ->
            Log.d(TAG, "Error " + throwable.getMessage());

    private View.OnClickListener addDownvote = view -> {
        if(post == null) return;

        Subscription subscription = downvotesHelper.addDownvote(key, post.downvotes)
                .subscribeOn(Schedulers.computation())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(uri -> Log.d(TAG, "Added " + uri), throwableHandler);

        compositeSubscription.add(subscription);
    };

    private View.OnClickListener removeDownvote = view -> {
        if(post == null) return;

        Subscription subscription = downvotesHelper.removeDownvote(key, post.downvotes)
                .subscribeOn(Schedulers.computation())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(rows -> Log.d(TAG, "Removed " + rows), throwableHandler);

        compositeSubscription.add(subscription);
    };

    private void loadArticleStatus(String key) {
        Subscription dbSubscription = downvotesHelper.isDownvoted(key)
                .subscribeOn(Schedulers.computation())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(downvoted -> {
                    if(downvoted) {
                        downvote.setImageDrawable(VectorDrawableCompat
                                .create(getResources(), R.drawable.ic_thumb_down_outline, null));

                        downvote.setOnClickListener(removeDownvote);
                    } else {
                        downvote.setImageDrawable(VectorDrawableCompat
                                .create(getResources(), R.drawable.ic_thumb_down, null));

                        downvote.setOnClickListener(addDownvote);
                    }
                }, throwableHandler);

        compositeSubscription.add(dbSubscription);
    }

    public static int getDarkColor(int color) {
        float[] hsv = new float[3];
        Color.colorToHSV(color, hsv);
        hsv[2] *= 0.8f;
        return Color.HSVToColor(hsv);
    }

    private void setColors(Palette.Swatch swatch) {
        if(swatch == null)
            return;

        int mainColor = swatch.getRgb();
        int bodyText = swatch.getBodyTextColor();

        collapsingToolbarLayout.setContentScrimColor(mainColor);
        collapsingToolbarLayout.setStatusBarScrimColor(getDarkColor(mainColor));

        panel.setBackgroundColor(swatch.getRgb());
        DrawableCompat.setTint(DrawableCompat.wrap(title.getCompoundDrawables()[0]), bodyText);
        DrawableCompat.setTint(DrawableCompat.wrap(date.getCompoundDrawables()[0]), bodyText);
        DrawableCompat.setTint(DrawableCompat.wrap(author.getCompoundDrawables()[0]), bodyText);
        DrawableCompat.setTint(DrawableCompat.wrap(downvotes.getCompoundDrawables()[0]), bodyText);

        title.setTextColor(swatch.getTitleTextColor());
        date.setTextColor(swatch.getTitleTextColor());
        author.setTextColor(swatch.getTitleTextColor());
        downvotes.setTextColor(swatch.getTitleTextColor());
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
                            setColors(swatch);
                        });
                    }
                });
    }

    @Override
    public void onDetach() {
        if(query != null) query.removeEventListener(valueEventListener);
        if(compositeSubscription != null) compositeSubscription.unsubscribe();

        super.onDetach();
    }
}
