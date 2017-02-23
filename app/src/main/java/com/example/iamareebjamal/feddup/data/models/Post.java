package com.example.iamareebjamal.feddup.data.models;

public class Post {

    public String title, content, user, url;
    public long time;
    public int downvotes;

    @Override
    public String toString() {
        return "Post{" +
                "title='" + title + '\'' +
                ", user='" + user + '\'' +
                ", downvotes=" + downvotes +
                ", time=" + time +
                ", url='" + url + '\'' +
                '}';
    }
}
