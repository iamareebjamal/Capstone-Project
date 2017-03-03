package com.example.iamareebjamal.feddup.ui.adapter;

import com.example.iamareebjamal.feddup.data.models.Post;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.database.DatabaseReference;

public abstract class PostAdapter extends FirebaseRecyclerAdapter {


        public PostAdapter(Class<Post> modelClass, int modelLayout, Class<com.example.iamareebjamal.feddup.ui.viewholder.PostHolder> viewHolderClass, DatabaseReference ref) {
            super(modelClass, modelLayout, viewHolderClass, ref);

        }
}
