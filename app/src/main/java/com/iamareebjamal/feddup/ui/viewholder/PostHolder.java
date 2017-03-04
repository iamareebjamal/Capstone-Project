package com.iamareebjamal.feddup.ui.viewholder;

import android.content.Context;
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

import com.iamareebjamal.feddup.R;
import com.iamareebjamal.feddup.data.db.utils.DatabaseHelper;
import com.iamareebjamal.feddup.data.db.utils.DownvotesHelper;
import com.iamareebjamal.feddup.data.db.utils.FavoritesHelper;
import com.iamareebjamal.feddup.data.models.Post;
import com.iamareebjamal.feddup.ui.FragmentInteractionListener;
import com.iamareebjamal.feddup.ui.widget.FavoritesWidget;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

public class PostHolder extends RecyclerView.ViewHolder {
    public static final String TAG = "PostHolder";

    @BindView(R.id.rootcard) CardView panel;
    @BindView(R.id.title) TextView title;
    @BindView(R.id.downvotes) TextView downvotes;
    @BindView(R.id.image) ImageView image;
    @BindView(R.id.downvote) ImageView downvote;
    @BindView(R.id.favorite) ImageView favorite;
    @BindView(R.id.title_bar) LinearLayout titleBar;

    private Context context;
    private static FragmentInteractionListener fragmentInteractionListener;

    public PostHolder(View itemView) {
        super(itemView);

        ButterKnife.bind(this, itemView);
        context = itemView.getContext();
    }

    public static void setFragmentInteractionListener(FragmentInteractionListener
                                                              fragmentInteractionListener) {
        PostHolder.fragmentInteractionListener = fragmentInteractionListener;
    }

    public void setPost(final Post post){
        String key = post.key;

        title.setText(post.title);
        downvotes.setText(String.format(Locale.getDefault(), context.getString(R.string.downvotes_format), post.downvotes));

        if(DatabaseHelper.isFavorite(key)){
            favorite.setImageDrawable(VectorDrawableCompat.create(context.getResources(), R.drawable.ic_heart, null));
            favorite.setOnClickListener(view ->
                    FavoritesHelper.removeFavorite(key)
                        .subscribeOn(Schedulers.computation())
                        .observeOn(AndroidSchedulers.mainThread())
                        .doOnNext(rows -> { if(rows != 0) FavoritesWidget.sendRefreshBroadcast(context); })
                        .subscribe(rows ->
                            Log.d("Removed", String.valueOf(rows))
                        , throwable ->
                            Log.d("Error", throwable.getMessage())
                        ));
        } else {
            favorite.setImageDrawable(VectorDrawableCompat.create(context.getResources(), R.drawable.ic_heart_outline, null));
            favorite.setOnClickListener(view ->
                    FavoritesHelper.addFavorite(key)
                        .subscribeOn(Schedulers.computation())
                        .observeOn(AndroidSchedulers.mainThread())
                        .doOnNext(added -> { if(added) FavoritesWidget.sendRefreshBroadcast(context); })
                        .subscribe(added ->
                            Log.d("Added", String.valueOf(added))
                        , throwable ->
                            Log.d("Error", throwable.getMessage())
                        ));
        }

        if(DatabaseHelper.isDownvoted(key)){
            downvote.setImageDrawable(VectorDrawableCompat.create(context.getResources(), R.drawable.ic_thumb_down, null));
            downvote.setOnClickListener(view ->
                DownvotesHelper.removeDownvote(key, post.downvotes)
                        .subscribeOn(Schedulers.computation())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(rows ->
                            Log.d("Removed", String.valueOf(rows))
                        , throwable ->
                            Log.d("Error", throwable.getMessage())
                        )
            );

        } else {
            downvote.setImageDrawable(VectorDrawableCompat.create(context.getResources(), R.drawable.ic_thumb_down_outline, null));
            downvote.setOnClickListener(view ->
                DownvotesHelper.addDownvote(key, post.downvotes)
                        .subscribeOn(Schedulers.computation())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(added ->
                            Log.d("Added", String.valueOf(added))
                        , throwable ->
                            Log.d("Error", throwable.getMessage())
                        )
            );

        }

        Palette.PaletteAsyncListener asyncListener = palette -> {
            Palette.Swatch swatch = palette.getVibrantSwatch();
            if(swatch != null) {
                titleBar.setBackgroundColor(swatch.getRgb());
                DrawableCompat.setTint(DrawableCompat.wrap(favorite.getDrawable()), swatch.getBodyTextColor());
                DrawableCompat.setTint(DrawableCompat.wrap(downvote.getDrawable()), swatch.getBodyTextColor());

                title.setTextColor(swatch.getTitleTextColor());
                downvotes.setTextColor(swatch.getTitleTextColor());
            }
        };

        Picasso.with(context)
                .load(post.url)
                .fit()
                .centerCrop()
                .placeholder(VectorDrawableCompat.create(context.getResources(), R.drawable.ic_photo, null))
                .tag(TAG)
                .into(image, new Callback.EmptyCallback() {
                    @Override
                    public void onSuccess() {
                        Bitmap bitmap = ((BitmapDrawable) image.getDrawable()).getBitmap();
                        Palette.from(bitmap).generate(asyncListener);
                    }
                });

        panel.setOnClickListener(view -> {
            if(fragmentInteractionListener == null) return;

            fragmentInteractionListener.onPostSelect(key);
        });
    }

}
