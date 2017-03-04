package com.iamareebjamal.feddup.api;

import retrofit2.Retrofit;
import retrofit2.adapter.rxjava.RxJavaCallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;

public class FeddupApi {

    private static Retrofit retrofit =
            new Retrofit.Builder()
                    .baseUrl("http://iamfeddup.appspot.com")
                    .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();

    private static FeddupService feddupService =
                    retrofit.create(FeddupService.class);

    public static Retrofit retrofit() { return retrofit;}

    public static FeddupService getFeddupService() { return feddupService; }

}
