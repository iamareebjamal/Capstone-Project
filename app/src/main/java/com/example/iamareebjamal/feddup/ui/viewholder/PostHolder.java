package com.example.iamareebjamal.feddup.ui.viewholder;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.support.graphics.drawable.VectorDrawableCompat;
import android.support.v4.graphics.drawable.DrawableCompat;
import android.support.v7.graphics.Palette;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.iamareebjamal.feddup.R;
import com.example.iamareebjamal.feddup.data.db.utils.DownvotesHelper;
import com.example.iamareebjamal.feddup.data.db.utils.FavoritesHelper;
import com.example.iamareebjamal.feddup.data.models.Post;
import com.example.iamareebjamal.feddup.ui.activity.DetailActivity;
import com.example.iamareebjamal.feddup.ui.fragment.DetailFragment;
import com.google.firebase.database.DatabaseReference;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

public class PostHolder extends RecyclerView.ViewHolder {
    @BindView(R.id.rootcard) CardView panel;
    @BindView(R.id.title) TextView title;
    @BindView(R.id.downvotes) TextView downvotes;
    @BindView(R.id.image) ImageView image;
    @BindView(R.id.downvote) ImageView downvote;
    @BindView(R.id.favorite) ImageView favorite;
    @BindView(R.id.title_bar) LinearLayout titleBar;

    private Context context;
    private FavoritesHelper favoritesHelper;
    private DownvotesHelper downvotesHelper;

    private static List<String> downvoted= new ArrayList<>();
    private static List<String> favorites = new ArrayList<>();

    public PostHolder(View itemView) {
        super(itemView);

        ButterKnife.bind(this, itemView);
        context = itemView.getContext();
        favoritesHelper = new FavoritesHelper(context);
        downvotesHelper = new DownvotesHelper(context);
    }

    public static void setDownvoted(List<String> downvoted) {
        PostHolder.downvoted = downvoted;
    }

    public static void addDownVoted(String key) {
        downvoted.add(key);
    }

    public static void clearDownVoted() {
        downvoted.clear();
    }

    public static void setFavorites(List<String> favorites) {
        PostHolder.favorites = favorites;
    }

    public static void addFavorite(String key) {
        favorites.add(key);
    }

    public static void clearFavorites() {
        favorites.clear();
    }

    public void setPost(final Post post, final DatabaseReference reference){
        String key = reference.getKey();

        title.setText(post.title);
        downvotes.setText("-"+post.downvotes);

        if(favorites.contains(key)){
            favorite.setImageDrawable(VectorDrawableCompat.create(context.getResources(), R.drawable.ic_heart, null));
            favorite.setOnClickListener(view -> {
                favoritesHelper.removeFavorite(reference.getKey())
                        .subscribeOn(Schedulers.computation())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(rows -> {
                            Log.d("Removed", String.valueOf(rows));
                        }, throwable -> {
                            Log.d("Error", throwable.getMessage());
                        });
            });
        } else {
            favorite.setImageDrawable(VectorDrawableCompat.create(context.getResources(), R.drawable.ic_heart_outline, null));
            favorite.setOnClickListener(view -> {
                favoritesHelper.addFavorite(reference.getKey())
                        .subscribeOn(Schedulers.computation())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(added -> {
                            Log.d("Added", String.valueOf(added));
                        }, throwable -> {
                            Log.d("Error", throwable.getMessage());
                        });
            });
        }

        if(downvoted.contains(key)){
            downvote.setImageDrawable(VectorDrawableCompat.create(context.getResources(), R.drawable.ic_thumb_down, null));
            downvote.setOnClickListener(view -> {
                downvotesHelper.removeDownvote(reference.getKey())
                        .subscribeOn(Schedulers.computation())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(rows -> {
                            reference.child("downvotes").setValue(post.downvotes-1);
                            Log.d("Removed", String.valueOf(rows));
                        }, throwable -> {
                            Log.d("Error", throwable.getMessage());
                        });
            });
        } else {
            downvote.setImageDrawable(VectorDrawableCompat.create(context.getResources(), R.drawable.ic_thumb_down_outline, null));
            downvote.setOnClickListener(view -> {
                downvotesHelper.addDownvote(reference.getKey())
                        .subscribeOn(Schedulers.computation())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(added -> {
                            reference.child("downvotes").setValue(post.downvotes+1);
                            Log.d("Added", String.valueOf(added));
                        }, throwable -> {
                            Log.d("Error", throwable.getMessage());
                        });
            });
        }

        Picasso.with(context)
                .load(post.url)
                .fit()
                .centerCrop()
                .placeholder(VectorDrawableCompat.create(context.getResources(), R.drawable.ic_photo, null))
                .into(image, new Callback.EmptyCallback() {
                    @Override
                    public void onSuccess() {
                        Bitmap bitmap = ((BitmapDrawable) image.getDrawable()).getBitmap();
                        Palette.from(bitmap).generate(palette -> {
                            Palette.Swatch swatch = palette.getVibrantSwatch();
                            if(swatch != null) {
                                titleBar.setBackgroundColor(swatch.getRgb());
                                DrawableCompat.setTint(DrawableCompat.wrap(favorite.getDrawable()), swatch.getBodyTextColor());
                                DrawableCompat.setTint(DrawableCompat.wrap(downvote.getDrawable()), swatch.getBodyTextColor());

                                title.setTextColor(swatch.getTitleTextColor());
                                downvotes.setTextColor(swatch.getTitleTextColor());
                            }
                        });
                    }
                });

        panel.setOnClickListener(view -> {
            Intent intent = new Intent(context, DetailActivity.class);
            intent.putExtra(DetailFragment.KEY, reference.getKey());

            context.startActivity(intent);
        });
    }
}
