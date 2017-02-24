package com.example.iamareebjamal.feddup.api;

import android.content.Context;
import android.net.Uri;

import com.example.iamareebjamal.feddup.data.db.DatabaseHelper;
import com.example.iamareebjamal.feddup.data.models.PostConfirmation;

import java.io.File;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import rx.Observable;

public class PostService {
    private String title;
    private String author;
    private String content;
    private String filePath;

    public void setTitle(String title) {
        this.title = title;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public String getTitle() {
        return title;
    }

    public String getAuthor() {
        return author;
    }

    public String getContent() {
        return content;
    }

    public String getFilePath() {
        return filePath;
    }

    public Observable<PostConfirmation> send() {
        File file = new File(filePath);

        RequestBody reqFile = RequestBody.create(MediaType.parse("image/*"), file);
        final MultipartBody.Part body = MultipartBody.Part.createFormData("image", file.getName(), reqFile);
        RequestBody title = RequestBody.create(MediaType.parse("text/plain"), this.title);
        RequestBody user = RequestBody.create(MediaType.parse("text/plain"), this.author);
        RequestBody content = RequestBody.create(MediaType.parse("text/plain"), this.content);

        return FeddupApi.getFeddupService().post(body, title, user, content);
    }
}
