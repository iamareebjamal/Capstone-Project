package com.example.iamareebjamal.feddup.ui.activity;

import android.content.Intent;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;

import com.example.iamareebjamal.feddup.R;
import com.example.iamareebjamal.feddup.data.models.Post;
import com.example.iamareebjamal.feddup.ui.viewholder.PostHolder;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class MainActivity extends AppCompatActivity {

    @BindView(com.example.iamareebjamal.feddup.R.id.toolbar)
    Toolbar mToolbar;

    @BindView(R.id.post_list)
    RecyclerView mRecyclerView;

    @BindView(R.id.fab)
    FloatingActionButton fab;

    FirebaseRecyclerAdapter<Post, PostHolder> postAdapter;

    static {
        FirebaseDatabase.getInstance().setPersistenceEnabled(true);
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ButterKnife.bind(this);

        setSupportActionBar(mToolbar);
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        setupList();
    }

    public void setupList(){
        Query postReference = FirebaseDatabase.getInstance().getReference("posts").orderByChild("downvotes").limitToFirst(10);

        postAdapter = new FirebaseRecyclerAdapter<Post, PostHolder>(Post.class, R.layout.item_card, PostHolder.class, postReference) {
            @Override
            protected void populateViewHolder(PostHolder viewHolder, Post post, int position) {
                viewHolder.setPost(post, getRef(position));
            }
        };

        mRecyclerView.setAdapter(postAdapter);
        mRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener(){
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy){
                if (dy > 0 ||dy<0 && fab.isShown())
                    fab.hide();
            }

            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {

                if (newState == RecyclerView.SCROLL_STATE_IDLE){
                    fab.show();
                }
                super.onScrollStateChanged(recyclerView, newState);
            }
        });
    }

    @OnClick(R.id.fab)
    public void newActivity(){
        startActivity(new Intent(this, PostActivity.class));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_main, menu);
        return true;
    }


    /*@OnClick(R.id.postButton)
    public void setPostButton(){
        File file = new File(Environment.getExternalStorageDirectory().getPath()+"/screencap.png");

        RequestBody reqFile = RequestBody.create(MediaType.parse("image/*"), file);
        final MultipartBody.Part body = MultipartBody.Part.createFormData("image", file.getName(), reqFile);
        RequestBody title = RequestBody.create(MediaType.parse("text/plain"), "New Post");
        RequestBody user = RequestBody.create(MediaType.parse("text/plain"), "iamareebjamal");
        RequestBody content = RequestBody.create(MediaType.parse("text/plain"), "Ooga Booga, where all the white women at?");

        FeddupApi.getFeddupService().post(body, title, user, content).enqueue(new Callback<PostConfirmation>() {
            @Override
            public void onResponse(Call<PostConfirmation> call, Response<PostConfirmation> response) {
                PostConfirmation postConfirmation = response.body();
                Toast.makeText(getApplicationContext(), postConfirmation.getMessage(), Toast.LENGTH_SHORT).show();
                Log.d("Feddup", postConfirmation.toString());
            }

            @Override
            public void onFailure(Call<PostConfirmation> call, Throwable t) {
                Toast.makeText(getApplicationContext(), t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }*/
}
