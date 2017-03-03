package com.example.iamareebjamal.feddup.ui.adapter;

import android.support.v7.util.DiffUtil;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.iamareebjamal.feddup.R;
import com.example.iamareebjamal.feddup.data.models.Post;
import com.example.iamareebjamal.feddup.ui.viewholder.PostHolder;
import com.example.iamareebjamal.feddup.utils.PostDiffUtils;

import java.util.ArrayList;
import java.util.List;

public class PostAdapter extends RecyclerView.Adapter<PostHolder> {

    private List<Post> posts = new ArrayList<>();

    public void updateList(List<Post> newPosts) {
        DiffUtil.DiffResult diffResult = DiffUtil.calculateDiff(new PostDiffUtils(this.posts, newPosts));

        this.posts.clear();
        this.posts.addAll(newPosts);
        diffResult.dispatchUpdatesTo(this);
    }

    @Override
    public PostHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View thisItemsView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_card, parent, false);

        return new PostHolder(thisItemsView);
    }

    @Override
    public void onBindViewHolder(PostHolder holder, int position) {
        holder.setPost(posts.get(position));
    }

    @Override
    public int getItemCount() {
        return posts.size();
    }
}