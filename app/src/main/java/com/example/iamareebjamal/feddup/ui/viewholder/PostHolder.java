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
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.iamareebjamal.feddup.R;
import com.example.iamareebjamal.feddup.data.models.Post;
import com.example.iamareebjamal.feddup.ui.activity.DetailActivity;
import com.google.firebase.database.DatabaseReference;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import butterknife.BindView;
import butterknife.ButterKnife;

public class PostHolder extends RecyclerView.ViewHolder {
    @BindView(R.id.rootcard) CardView panel;
    @BindView(R.id.title) TextView title;
    @BindView(R.id.downvotes) TextView downvotes;
    @BindView(R.id.image) ImageView image;
    @BindView(R.id.downvote) ImageView downvote;
    @BindView(R.id.favorite) ImageView favorite;
    @BindView(R.id.title_bar) LinearLayout titleBar;

    private Context context;

    public PostHolder(View itemView) {
        super(itemView);

        ButterKnife.bind(this, itemView);
        context = itemView.getContext();
    }

    public void setPost(final Post post, final DatabaseReference reference){
        title.setText(post.title);
        downvotes.setText("-"+post.downvotes);

        favorite.setImageDrawable(VectorDrawableCompat.create(context.getResources(), R.drawable.ic_heart_outline, null));
        downvote.setImageDrawable(VectorDrawableCompat.create(context.getResources(), R.drawable.ic_thumb_down_outline, null));

        downvote.setOnClickListener(view -> {
            reference.child("downvotes").setValue(post.downvotes+1);
            downvote.setImageDrawable(VectorDrawableCompat.create(context.getResources(), R.drawable.ic_thumb_down, null));
        });

        favorite.setOnClickListener(view -> favorite.setImageDrawable(VectorDrawableCompat.create(context.getResources(), R.drawable.ic_heart, null)));

        Picasso.with(context)
                .load(post.url)
                .fit()
                .centerCrop()
                .into(image, new Callback.EmptyCallback() {
                    @Override
                    public void onSuccess() {
                        Bitmap bitmap = ((BitmapDrawable) image.getDrawable()).getBitmap();
                        Palette.from(bitmap).maximumColorCount(16).generate(palette -> {
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

        panel.setOnClickListener(view -> context.startActivity(new Intent(context, DetailActivity.class)));
    }
}
