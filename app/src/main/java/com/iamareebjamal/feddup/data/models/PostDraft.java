package com.iamareebjamal.feddup.data.models;

import com.iamareebjamal.feddup.api.FeddupApi;

import java.io.File;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;

public class PostDraft {
    public static final String ID = "id";
    public static final String TITLE = "title";
    public static final String AUTHOR = "author";
    public static final String CONTENT = "content";
    public static final String FILE_PATH = "file_path";

    private int id;
    private String title;
    private String author;
    private String content;
    private String filePath;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public Call<PostConfirmation> send() {
        File file = new File(filePath);

        RequestBody reqFile = RequestBody.create(MediaType.parse("image/*"), file);
        final MultipartBody.Part body = MultipartBody.Part.createFormData("image", file.getName(), reqFile);
        RequestBody title = RequestBody.create(MediaType.parse("text/plain"), this.title);
        RequestBody user = RequestBody.create(MediaType.parse("text/plain"), this.author);
        RequestBody content = RequestBody.create(MediaType.parse("text/plain"), this.content);

        return FeddupApi.getFeddupService().post(body, title, user, content);
    }

    @Override
    public String toString() {
        return "PostDraft{" +
                "title='" + title + '\'' +
                ", author='" + author + '\'' +
                ", content='" + content + '\'' +
                ", filePath='" + filePath + '\'' +
                '}';
    }
}
