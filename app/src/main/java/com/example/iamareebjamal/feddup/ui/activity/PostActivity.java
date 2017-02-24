package com.example.iamareebjamal.feddup.ui.activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.design.widget.BaseTransientBottomBar;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TextInputEditText;
import android.support.design.widget.TextInputLayout;
import android.support.graphics.drawable.VectorDrawableCompat;
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
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.iamareebjamal.feddup.data.db.DatabaseProvider;
import com.example.iamareebjamal.feddup.data.models.PostDraft;
import com.example.iamareebjamal.feddup.R;
import com.example.iamareebjamal.feddup.data.db.DatabaseHelper;
import com.example.iamareebjamal.feddup.data.models.PostConfirmation;
import com.example.iamareebjamal.feddup.utils.ErrorUtils;
import com.example.iamareebjamal.feddup.utils.Utils;
import com.jakewharton.rxbinding.widget.RxTextView;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import retrofit2.adapter.rxjava.HttpException;
import rx.Observable;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.schedulers.Schedulers;
import rx.subjects.PublishSubject;
import rx.subscriptions.CompositeSubscription;

public class PostActivity extends AppCompatActivity {

    private static final String TAG = "PostActivity";
    private static final String IMAGE_TAG = "image";
    private static final int PICK_IMAGE = 34;
    private String filePath;
    private Uri draftUri;

    private DatabaseHelper db = new DatabaseHelper(this);

    @BindView(R.id.activity_post) CoordinatorLayout rootLayout;
    @BindView(R.id.toolbar) Toolbar mToolbar;
    @BindView(R.id.postImage) ImageView postImage;
    @BindView(R.id.post) FloatingActionButton postArticle;

    @BindView(R.id.title) TextInputEditText title_text;
    @BindView(R.id.title_wrapper) TextInputLayout title_wrapper;
    @BindView(R.id.user) TextInputEditText user_text;
    @BindView(R.id.user_wrapper) TextInputLayout user_wrapper;
    @BindView(R.id.content) TextInputEditText content_text;
    @BindView(R.id.content_wrapper) TextInputLayout content_wrapper;
    @BindView(R.id.progress) ProgressBar progressBar;

    PublishSubject<Boolean> created = PublishSubject.create();
    CompositeSubscription compositeSubscription = new CompositeSubscription();

    Action1<Throwable> throwableHandler = (throwable) -> {
        progressBar.setVisibility(View.GONE);
        Log.d(TAG, throwable.getMessage());
    };

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

        handleIntentExtras();

        setupForm();
    }

    private void handleIntentExtras() {
        Intent extras = getIntent();
        if(extras == null) return;

        int id = extras.getIntExtra(PostDraft.ID, -1);
        if(id != -1) draftUri = DatabaseProvider.Drafts.withId(id);

        String title = extras.getStringExtra(PostDraft.TITLE);
        String author = extras.getStringExtra(PostDraft.AUTHOR);
        String content = extras.getStringExtra(PostDraft.CONTENT);
        String filePath = extras.getStringExtra(PostDraft.FILE_PATH);

        if(title != null) title_text.setText(title);
        if(author != null) user_text.setText(author);
        if(content != null) content_text.setText(content);

        if(filePath != null) {
            this.filePath = filePath;
            imageLoaded();
        }
    }

    private void imageLoaded() {
        if(filePath == null)
            return;

        Picasso.with(this)
                .load(new File(filePath))
                .fit()
                .centerCrop()
                .placeholder(VectorDrawableCompat.create(getResources(), R.drawable.ic_photo, null))
                .into(postImage);

        created.onNext(true);
    }

    private Observable<Boolean> formValidObservable(TextView textView, TextInputLayout textInputLayout, int lower_bound, int upper_bound) {
        String message = String.format(Locale.getDefault(), "Should be between %d and %d", lower_bound, upper_bound);

        Observable<Boolean> formObservable =
                RxTextView.textChanges(textView)
                        .map(inputText -> inputText.length() > lower_bound && inputText.length() < upper_bound)
                        .distinctUntilChanged()
                        .debounce(500, TimeUnit.MILLISECONDS)
                        .observeOn(AndroidSchedulers.mainThread());

        Subscription subscription = formObservable.subscribe(isValid -> {
            if(isValid) {
                textInputLayout.setError(null);
                textInputLayout.setErrorEnabled(false);
            } else {
                textInputLayout.setErrorEnabled(true);
                textInputLayout.setError(message);
            }
        });

        compositeSubscription.add(subscription);

        return formObservable;
    }

    private void setupForm(){
        postArticle.hide();

        Observable<Boolean> titleObservable = formValidObservable(title_text, title_wrapper, 2, 50);
        Observable<Boolean> userObservable = formValidObservable(user_text, user_wrapper, 4, 16);
        Observable<Boolean> contentObservable = formValidObservable(content_text, content_wrapper, 10, 50000);

        Subscription subscription = Observable.combineLatest(
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

        compositeSubscription.add(subscription);
    }

    private PostDraft preparePost() {
        PostDraft postDraft = new PostDraft();

        postDraft.setTitle(title_text.getText().toString());
        postDraft.setAuthor(user_text.getText().toString());
        postDraft.setContent(content_text.getText().toString());
        postDraft.setFilePath(filePath);

        return postDraft;
    }

    @OnClick(R.id.post)
    public void sendPost() {
        Subscription postSubscription = preparePost().send()
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

        compositeSubscription.add(postSubscription);
    }

    private void saveInDraft() {
        if(draftUri == null) savePost(); else updatePost();
    }

    private void queryHandler(boolean condition) {
        progressBar.setVisibility(View.GONE);
        if(condition) {

            Snackbar.make(rootLayout, "Post Saved", Snackbar.LENGTH_LONG)
                    .setAction("Undo", view -> deleteDraft())
                    .setActionTextColor(ContextCompat.getColor(this, R.color.colorPrimary))
                    .show();

            Log.d(TAG, "Post Created : " + draftUri.toString());
        } else {
            Snackbar.make(rootLayout, "Saving Post Failed", Snackbar.LENGTH_LONG)
                    .setAction("Retry", view -> saveInDraft())
                    .show();

            draftUri = null;
            Log.d(TAG, "Post Save Failed");
        }
    }

    private void savePost() {
        Subscription postSubscription = db.insertDraft(preparePost())
                .subscribeOn(Schedulers.computation())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnSubscribe(() -> progressBar.setVisibility(View.VISIBLE) )
                .subscribe(uri -> {
                    draftUri = uri;
                    queryHandler(uri != null);
                }, throwableHandler);

        compositeSubscription.add(postSubscription);
    }

    private void updatePost() {
        if(draftUri == null) {
            Snackbar.make(rootLayout, "Error updating Draft. Create new?", Snackbar.LENGTH_LONG)
                    .setAction("Yes", view -> savePost())
                    .show();

            return;
        }

        Subscription postSubscription = db.updateDraft(draftUri, preparePost())
                .subscribeOn(Schedulers.computation())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnSubscribe(() -> progressBar.setVisibility(View.VISIBLE) )
                .subscribe(rows -> {
                    queryHandler(rows != 0);
                }, throwableHandler);

        compositeSubscription.add(postSubscription);
    }

    private void deleteDraft() {
        if(draftUri == null) {
            Snackbar.make(rootLayout, "No Draft to Delete. Create new?", Snackbar.LENGTH_LONG)
                    .setAction("Yes", view -> saveInDraft())
                    .show();

            return;
        }

        Subscription postSubscription = db.deleteUri(draftUri)
                .subscribeOn(Schedulers.computation())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnSubscribe(() -> progressBar.setVisibility(View.VISIBLE) )
                .subscribe(rows -> {
                    progressBar.setVisibility(View.GONE);
                    if (rows != 0) {
                        draftUri = null;

                        Snackbar.make(rootLayout, "Draft Deleted", Snackbar.LENGTH_LONG)
                                .setAction("Undo", view -> saveInDraft())
                                .setActionTextColor(ContextCompat.getColor(this, R.color.colorPrimary))
                                .show();

                        Log.d(TAG, "Post Deleted : " + draftUri.toString() + " Rows : " + rows);
                    } else {
                        Snackbar.make(rootLayout, "Deleting Post Failed", Snackbar.LENGTH_LONG)
                                .setAction("Retry", view -> deleteDraft())
                                .show();

                        Log.d(TAG, "Post Delete Failed");
                    }
                }, throwableHandler);

        compositeSubscription.add(postSubscription);
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

            filePath = Utils.getFilePath(this, fileUri);
            imageLoaded();
        }
    }

    @OnClick(R.id.image)
    public void requestPermissionAndLoadImage() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && ContextCompat.checkSelfPermission(this, android.Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.READ_EXTERNAL_STORAGE}, PICK_IMAGE);
            return;
        }

        loadImage();
    }

    @Override
    public void onRequestPermissionsResult(final int requestCode, @NonNull final String[] permissions, @NonNull final int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PICK_IMAGE && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            loadImage();
        }
    }

    private boolean save = true;
    @Override
    public void onBackPressed() {

        Runnable close = super::onBackPressed;

        final Context context = this;

        Snackbar.make(rootLayout, "Saving in Drafts. Undo?", Snackbar.LENGTH_SHORT)
                .setAction("Yes", view -> {
                    Toast.makeText(this, "Draft Deleted", Toast.LENGTH_LONG).show();
                    save = false;
                    super.onBackPressed();
                })
                .addCallback(new BaseTransientBottomBar.BaseCallback<Snackbar>() {
                    @Override
                    public void onDismissed(Snackbar transientBottomBar, int event) {
                        super.onDismissed(transientBottomBar, event);
                        if(save) {
                            Toast.makeText(context, "Saving in Drafts", Toast.LENGTH_SHORT).show();
                            saveInDraft();
                        }
                        close.run();
                    }
                })
                .show();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                break;
            case R.id.save:
                saveInDraft();
                break;
            case R.id.delete:
                deleteDraft();
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

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putString(IMAGE_TAG, filePath);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);

        filePath = savedInstanceState.getString(IMAGE_TAG);

        if(filePath == null)
            return;

        imageLoaded();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if(compositeSubscription != null) compositeSubscription.unsubscribe();
    }
}
