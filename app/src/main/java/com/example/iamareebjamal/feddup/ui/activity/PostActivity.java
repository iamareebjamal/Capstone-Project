package com.example.iamareebjamal.feddup.ui.activity;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.iamareebjamal.feddup.api.PostService;
import com.example.iamareebjamal.feddup.R;
import com.example.iamareebjamal.feddup.data.models.PostConfirmation;
import com.example.iamareebjamal.feddup.utils.ErrorUtils;
import com.example.iamareebjamal.feddup.utils.Utils;
import com.jakewharton.rxbinding.widget.RxTextView;
import com.squareup.picasso.Picasso;

import java.util.Locale;
import java.util.concurrent.TimeUnit;

import butterknife.BindView;
import butterknife.ButterKnife;
import retrofit2.adapter.rxjava.HttpException;
import rx.Observable;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import rx.subjects.PublishSubject;

public class PostActivity extends AppCompatActivity {

    private static final int PICK_IMAGE = 34;
    private String filePath;

    @BindView(R.id.activity_post) CoordinatorLayout rootLayout;
    @BindView(R.id.toolbar) Toolbar mToolbar;
    @BindView(R.id.postImage) ImageView postImage;
    @BindView(R.id.image) FloatingActionButton loadImage;
    @BindView(R.id.post) FloatingActionButton postArticle;

    @BindView(R.id.title) EditText title_text;
    @BindView(R.id.title_wrapper) TextInputLayout title_wrapper;
    @BindView(R.id.user) EditText user_text;
    @BindView(R.id.user_wrapper) TextInputLayout user_wrapper;
    @BindView(R.id.content) EditText content_text;
    @BindView(R.id.content_wrapper) TextInputLayout content_wrapper;
    @BindView(R.id.progress) ProgressBar progressBar;

    PublishSubject<Boolean> created = PublishSubject.create();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post);

        ButterKnife.bind(this);

        setSupportActionBar(mToolbar);
        ActionBar actionBar = getSupportActionBar();
        if(actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setDisplayShowHomeEnabled(true);
        }

        Picasso.with(this)
                .load("http://netdna.webdesignerdepot.com/uploads/2015/07/featured_mdl.jpg")
                .fit()
                .centerCrop()
                .into(postImage);

        loadImage.setOnClickListener(view -> requestPermissionAndLoadImage());
        postArticle.setOnClickListener(view -> sendPost());

        setupForm();
    }

    private Observable<Boolean> formValidObservable(TextView textView, TextInputLayout textInputLayout, int lower_bound, int upper_bound) {
        String message = String.format(Locale.getDefault(), "Should be between %d and %d", lower_bound, upper_bound);

        Observable<Boolean> formObservable =
                RxTextView.textChanges(textView)
                        .map(inputText -> inputText.length() > lower_bound && inputText.length() < upper_bound)
                        .distinctUntilChanged()
                        .debounce(500, TimeUnit.MILLISECONDS)
                        .observeOn(AndroidSchedulers.mainThread());

        formObservable.subscribe(isValid -> {
            if(isValid) {
                textInputLayout.setError(null);
                textInputLayout.setErrorEnabled(false);
            } else {
                textInputLayout.setErrorEnabled(true);
                textInputLayout.setError(message);
            }
        });

        return formObservable;
    }

    private void setupForm(){
        postArticle.hide();

        Observable<Boolean> titleObservable = formValidObservable(title_text, title_wrapper, 2, 50);
        Observable<Boolean> userObservable = formValidObservable(user_text, user_wrapper, 4, 16);
        Observable<Boolean> contentObservable = formValidObservable(content_text, content_wrapper, 10, 50000);

        Observable.combineLatest(
                titleObservable,
                userObservable,
                contentObservable,
                created,
                (titleValid, userValid, contentValid, photoCreated) -> titleValid && userValid && contentValid && photoCreated)
                .subscribe(valid -> {
                    if(valid) {
                        postArticle.show();
                    } else {
                        postArticle.hide();
                    }
                });
    }

    private PostService preparePost() {
        PostService postService = new PostService();

        postService.setTitle(title_text.getText().toString());
        postService.setAuthor(user_text.getText().toString());
        postService.setContent(content_text.getText().toString());
        postService.setFilePath(filePath);

        return postService;
    }

    private void sendPost() {
        preparePost()
                .send()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnSubscribe(() -> progressBar.setVisibility(View.VISIBLE))
                .subscribe(postConfirmation -> {
                    progressBar.setVisibility(View.GONE);

                    if(postConfirmation.getError()) {
                        Snackbar.make(rootLayout, postConfirmation.getMessage(), Snackbar.LENGTH_LONG).show();
                    } else {
                        Toast.makeText(this, "Post Created", Toast.LENGTH_LONG).show();
                        finish();
                    }
                }, throwable -> {
                    progressBar.setVisibility(View.GONE);
                    if(throwable instanceof HttpException) {
                        PostConfirmation postConfirmation = ErrorUtils.parseError(((HttpException) throwable).response());

                        Snackbar.make(rootLayout, postConfirmation.getMessage(), Snackbar.LENGTH_LONG).show();
                    }
                });
    }

    private void savePost() {
        Log.d("LOL", "Start");
        preparePost()
                .save(this)
                .subscribeOn(Schedulers.computation())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnSubscribe(() -> progressBar.setVisibility(View.VISIBLE) )
                .subscribe(uri -> {
                    progressBar.setVisibility(View.GONE);
                    if(uri != null) {
                        Snackbar.make(rootLayout, "Post Saved", Snackbar.LENGTH_LONG)
                                .setAction("Undo", view -> deleteDraft(uri))
                                .setActionTextColor(ContextCompat.getColor(this, R.color.colorPrimary))
                                .show();

                        Log.d("LOL", uri.toString());
                    } else {
                        Snackbar.make(rootLayout, "Saving Post Failed", Snackbar.LENGTH_LONG)
                                .setAction("Retry", view -> savePost())
                                .show();

                        Log.d("LOL", "null");
                    }
                }, throwable -> {
                    progressBar.setVisibility(View.GONE);
                    Log.d("LOL", throwable.getMessage());
                });
    }

    private void deleteDraft(Uri uri) {
        Snackbar.make(rootLayout, "Draft deleted", Snackbar.LENGTH_LONG)
                .setAction("Undo", view -> savePost())
                .show();
    }

    private void loadImage() {
        Intent getIntent = new Intent(Intent.ACTION_GET_CONTENT);
        getIntent.setType("image/*");

        Intent pickIntent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        pickIntent.setType("image/*");

        Intent chooserIntent = Intent.createChooser(getIntent, "Select Image");
        chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, new Intent[] {pickIntent});

        startActivityForResult(chooserIntent, PICK_IMAGE);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE && resultCode == Activity.RESULT_OK) {
            if (data == null) {
                Snackbar.make(rootLayout, "Failed to load image", Snackbar.LENGTH_LONG).show();
                return;
            }

            Uri fileUri = data.getData();

            Picasso.with(this)
                    .load(fileUri)
                    .fit()
                    .centerCrop()
                    .into(postImage);

            filePath = Utils.getFilePath(this, fileUri);
            created.onNext(true);
        }
    }

    private boolean requestPermissionAndLoadImage() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && ContextCompat.checkSelfPermission(this, android.Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.READ_EXTERNAL_STORAGE}, PICK_IMAGE);

            return false;
        }

        loadImage();
        return true;
    }

    @Override
    public void onRequestPermissionsResult(final int requestCode, @NonNull final String[] permissions, @NonNull final int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PICK_IMAGE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                loadImage();
            }
        }
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
            case R.id.save:
                savePost();
                break;
            default:
                // Do nothing
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_post, menu);
        return true;
    }
}
