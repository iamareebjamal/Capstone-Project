package com.example.iamareebjamal.feddup.ui.widget;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.media.ThumbnailUtils;
import android.os.Binder;
import android.support.v7.graphics.Palette;
import android.util.Log;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

import com.example.iamareebjamal.feddup.R;
import com.example.iamareebjamal.feddup.data.db.DatabaseProvider;
import com.example.iamareebjamal.feddup.data.db.utils.FavoritesHelper;
import com.example.iamareebjamal.feddup.data.models.Post;
import com.example.iamareebjamal.feddup.utils.Utils;
import com.squareup.picasso.Picasso;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

class FavoriteProvider implements RemoteViewsService.RemoteViewsFactory {

    private Context context;
    private Cursor cursor;
    private List<Post> posts = new ArrayList<>();

    FavoriteProvider(Context context) {
        this.context = context;
    }

    private void loadData() {
        final long identityToken = Binder.clearCallingIdentity();

        posts.clear();

        if (cursor != null)
            cursor.close();

        cursor = context.getContentResolver().query(
                DatabaseProvider.Favorites.FAVORITE_DETAILS,
                DatabaseProvider.Favorites.JOIN_PROJECTION,
                null,
                null,
                null);

        FavoritesHelper.getFavoritePostsFromCursor(cursor).subscribe(
                post -> posts.add(post),
                throwable -> Log.d("Widget", throwable.getMessage()));

        Binder.restoreCallingIdentity(identityToken);
    }

    @Override
    public void onCreate() {
        loadData();
    }

    @Override
    public void onDataSetChanged() {
        loadData();
    }

    @Override
    public void onDestroy() {
        if(cursor != null) cursor.close();
    }

    @Override
    public int getCount() {
        return posts.size();
    }

    @Override
    public RemoteViews getViewAt(int i) {
        RemoteViews remoteViews = new RemoteViews(context.getPackageName(), R.layout.content_post);

        Post post = posts.get(i);

        remoteViews.setTextViewText(R.id.title, post.title);
        try {
            Bitmap bitmap = Picasso.with(context).load(post.url).get();
            remoteViews.setImageViewBitmap(R.id.image,
                    ThumbnailUtils.extractThumbnail(
                            bitmap,
                            (int) Utils.getPx(context, 400),
                            (int) Utils.getPx(context, 320)
                    ));

            Palette.from(bitmap).generate(palette -> {
                Palette.Swatch swatch = palette.getVibrantSwatch();

                if(swatch != null) {
                    remoteViews.setInt(R.id.title_bar, "setBackgroundColor", swatch.getRgb());
                }
            });
        } catch (IOException e) {
            e.printStackTrace();
        }

        return remoteViews;
    }

    @Override
    public RemoteViews getLoadingView() {
        return null;
    }

    @Override
    public int getViewTypeCount() {
        return 1;
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public boolean hasStableIds() {
        return true;
    }
}
