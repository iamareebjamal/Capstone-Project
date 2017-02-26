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
import android.widget.Toast;

import com.example.iamareebjamal.feddup.R;
import com.example.iamareebjamal.feddup.data.db.DatabaseProvider;
import com.example.iamareebjamal.feddup.data.db.utils.DraftsHelper;
import com.example.iamareebjamal.feddup.data.models.PostDraft;
import com.example.iamareebjamal.feddup.ui.activity.PostActivity;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import java.io.File;

import butterknife.BindView;
import butterknife.ButterKnife;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

public class DraftHolder extends RecyclerView.ViewHolder {

    @BindView(R.id.rootcard) CardView panel;
    @BindView(R.id.title) TextView title;
    @BindView(R.id.image) ImageView image;
    @BindView(R.id.delete) ImageView delete;
    @BindView(R.id.title_bar) LinearLayout titleBar;

    private Context context;

    public DraftHolder(View itemView) {
        super(itemView);

        ButterKnife.bind(this, itemView);
        context = itemView.getContext();
    }

    public void setDraft(PostDraft post) {
        if(post.getTitle() != null) {
            title.setText(post.getTitle());
        } else {
            title.clearComposingText();
        }

        if(post.getFilePath() != null) {
            Picasso.with(context)
                    .load(new File(post.getFilePath()))
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
                                    DrawableCompat.setTint(DrawableCompat.wrap(delete.getDrawable()), swatch.getBodyTextColor());

                                    title.setTextColor(swatch.getTitleTextColor());
                                }
                            });
                        }
                    });
        } else {
            image.setImageDrawable(VectorDrawableCompat.create(context.getResources(), R.drawable.ic_photo, null));
        }

        delete.setOnClickListener(view ->
            DraftsHelper.deleteUri(DatabaseProvider.Drafts.withId(post.getId()))
                    .subscribeOn(Schedulers.computation())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(rows -> {
                        Toast.makeText(context, context.getString(R.string.draft_deleted), Toast.LENGTH_SHORT).show();
                    }, throwable -> Log.d("Error", "Can't delete"))
        );

        panel.setOnClickListener(view -> {
            Intent intent = new Intent(context, PostActivity.class);
            intent.putExtra(PostDraft.ID, post.getId());
            intent.putExtra(PostDraft.TITLE, post.getTitle());
            intent.putExtra(PostDraft.AUTHOR, post.getAuthor());
            intent.putExtra(PostDraft.CONTENT, post.getContent());
            intent.putExtra(PostDraft.FILE_PATH, post.getFilePath());

            context.startActivity(intent);
        });
    }

}
