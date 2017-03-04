package com.example.iamareebjamal.feddup.utils;

import android.support.v7.util.DiffUtil;

import com.example.iamareebjamal.feddup.data.models.Post;

import java.util.List;

public class PostDiffUtils extends DiffUtil.Callback {

    private List<Post> oldPosts, newPosts;

    public PostDiffUtils(List<Post> oldPosts, List<Post> newPosts) {
        this.oldPosts = oldPosts;
        this.newPosts = newPosts;
    }

    @Override
    public int getOldListSize() {
        return oldPosts.size();
    }

    @Override
    public int getNewListSize() {
        return newPosts.size();
    }

    @Override
    public boolean areItemsTheSame(int oldItemPosition, int newItemPosition) {
        return oldPosts.get(oldItemPosition).key.equals(newPosts.get(newItemPosition).key);
    }

    @Override
    public boolean areContentsTheSame(int oldItemPosition, int newItemPosition) {
        return oldPosts.get(oldItemPosition).equals(newPosts.get(newItemPosition));
    }
}
