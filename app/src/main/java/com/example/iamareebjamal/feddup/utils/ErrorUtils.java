package com.example.iamareebjamal.feddup.utils;

import com.example.iamareebjamal.feddup.api.FeddupApi;
import com.example.iamareebjamal.feddup.data.models.PostConfirmation;

import java.io.IOException;
import java.lang.annotation.Annotation;

import okhttp3.ResponseBody;
import retrofit2.Converter;
import retrofit2.Response;

public class ErrorUtils {

    public static PostConfirmation parseError(Response<?> response) {
        Converter<ResponseBody, PostConfirmation> converter =
                FeddupApi.retrofit()
                        .responseBodyConverter(PostConfirmation.class, new Annotation[0]);

        PostConfirmation error;

        try {
            error = converter.convert(response.errorBody());
        } catch (IOException e) {
            error = new PostConfirmation();
            error.setError(true);
            error.setMessage("I/O Exception");
        }

        return error;
    }
}