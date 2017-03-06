package com.iamareebjamal.feddup.api;

import com.iamareebjamal.feddup.data.models.PostConfirmation;

import retrofit2.Response;

public interface PostUploadListener {
    void onStart();

    void onSuccess(PostConfirmation postConfirmation);

    void onNetworkError(Response throwable);

    void onError(Throwable throwable);
}
