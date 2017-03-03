package com.example.iamareebjamal.feddup.data.models;

public class Post {

    public String title, content, user, url, key;
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Post post = (Post) o;

        if (time != post.time) return false;
        if (downvotes != post.downvotes) return false;
        if (!title.equals(post.title)) return false;
        if (!content.equals(post.content)) return false;
        if (!user.equals(post.user)) return false;
        if (!url.equals(post.url)) return false;
        return key.equals(post.key);

    }

    @Override
    public int hashCode() {
        int result = title.hashCode();
        result = 31 * result + content.hashCode();
        result = 31 * result + user.hashCode();
        result = 31 * result + url.hashCode();
        result = 31 * result + key.hashCode();
        result = 31 * result + (int) (time ^ (time >>> 32));
        result = 31 * result + downvotes;
        return result;
    }
}
