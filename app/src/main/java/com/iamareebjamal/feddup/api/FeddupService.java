package com.iamareebjamal.feddup.api;

import com.iamareebjamal.feddup.data.models.PostConfirmation;

import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;
import rx.Observable;

public interface FeddupService {

    @Multipart
    @POST("/new_post")
    Observable<PostConfirmation> post(
            @Part MultipartBody.Part image,
            @Part("title") RequestBody title,
            @Part("user") RequestBody user,
            @Part("content") RequestBody content
    );
}
