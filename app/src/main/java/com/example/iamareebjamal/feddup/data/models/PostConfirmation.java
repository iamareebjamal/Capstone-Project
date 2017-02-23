package com.example.iamareebjamal.feddup.data.models;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class PostConfirmation {

    @SerializedName("url")
    @Expose
    private String url;
    @SerializedName("user")
    @Expose
    private String user;
    @SerializedName("key")
    @Expose
    private String key;
    @SerializedName("error")
    @Expose
    private Boolean error;
    @SerializedName("message")
    @Expose
    private String message;

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public Boolean getError() {
        return error;
    }

    public void setError(Boolean error) {
        this.error = error;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    @Override
    public String toString() {
        return "PostConfirmation{" +
                "error=" + error +
                ", message='" + message + '\'' +
                ", user='" + user + '\'' +
                ", key='" + key + '\'' +
                ", url='" + url + '\'' +
                '}';
    }
}