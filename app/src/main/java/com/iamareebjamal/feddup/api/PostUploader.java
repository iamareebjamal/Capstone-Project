package com.iamareebjamal.feddup.api;

import android.os.AsyncTask;

import com.iamareebjamal.feddup.data.models.PostConfirmation;
import com.iamareebjamal.feddup.data.models.PostDraft;

import java.io.IOException;

import retrofit2.Response;

public class PostUploader {
    private static PostUploadListener postUploadListener;

    public static void post(PostDraft postDraft, PostUploadListener postUploadListener) {
        PostUploader.postUploadListener = postUploadListener;
        new Uploader().execute(postDraft);
    }

    public static void cleanup() {
        postUploadListener = null;
    }

    private static class Uploader extends AsyncTask<PostDraft, Object, Object> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            if (postUploadListener != null) postUploadListener.onStart();
        }

        @Override
        protected Object doInBackground(PostDraft... postDrafts) {
            PostDraft post = postDrafts[0];

            try {
                return post.send().execute();
            } catch (IOException ioe) {
                return ioe;
            }
        }

        @Override
        protected void onPostExecute(Object o) {
            super.onPostExecute(o);

            if (postUploadListener == null) return;

            if (o instanceof Response && ((Response) o).isSuccessful()) {
                postUploadListener.onSuccess((PostConfirmation) ((Response) o).body());
            } else if (o instanceof Response && !((Response) o).isSuccessful()) {
                postUploadListener.onNetworkError((Response) o);
            } else if (o instanceof Throwable) {
                postUploadListener.onError((Throwable) o);
            }
        }
    }

}
