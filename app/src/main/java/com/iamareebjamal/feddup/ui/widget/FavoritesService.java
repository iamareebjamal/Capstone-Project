package com.iamareebjamal.feddup.ui.widget;

import android.content.Intent;
import android.widget.RemoteViewsService;

public class FavoritesService extends RemoteViewsService {
    @Override
    public RemoteViewsFactory onGetViewFactory(Intent intent) {
        return new FavoriteProvider(this);
    }
}