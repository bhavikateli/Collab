package com.bhavikateli.collab.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

import com.bhavikateli.collab.CommentActivity;
import com.bhavikateli.collab.IntroActivity;
import com.bhavikateli.collab.Post;
import com.bhavikateli.collab.ProfileFragmentAdapter;
import com.bhavikateli.collab.R;
import com.bumptech.glide.Glide;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import org.parceler.Parcels;

import java.util.ArrayList;
import java.util.List;

import jp.wasabeef.glide.transformations.RoundedCornersTransformation;


public class ProfileFragment extends Fragment {

    public static final String TAG = "ProfileFragment";
    ParseUser user;
    private ImageView ivUserProfileImage;
    private TextView tvUserUsername;
    private RecyclerView rvUserPosts;
    private ProfileFragmentAdapter adapter;
    private List<Post> allPosts;
    private StaggeredGridLayoutManager manager;
    private Button btnLogOut;
    private TextView tvProfileDescription;
    private Button btnDiscoveryComment;


    public ProfileFragment() {
        // Required empty public constructor
    }

    public ProfileFragment(ParseUser user) {
        // Required empty public constructor
        this.user = user;
    }


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
      //  ((AppCompatActivity) getActivity()).getSupportActionBar().show();

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_profile, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
       // user = ParseUser.getCurrentUser();

        //find id within resources
        tvUserUsername = view.findViewById(R.id.tvUserUsername);
        ivUserProfileImage = view.findViewById(R.id.ivUserProfileImage);
        btnLogOut = view.findViewById(R.id.btnLogOut);
        tvProfileDescription = view.findViewById(R.id.tvProfileDescription);
        rvUserPosts = view.findViewById(R.id.rvUserPosts);
        btnDiscoveryComment = view.findViewById(R.id.btnDiscoveryComment);

        btnDiscoveryComment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(getContext(), CommentActivity.class);
                i.putExtra("user", Parcels.wrap(ParseUser.getCurrentUser()));
                startActivity(i);
            }
        });

        //set its text
        Log.i(TAG, "this is the username: " + user.getUsername());
        tvUserUsername.setText(user.getUsername());
        tvProfileDescription.setText(user.get("profileDescription").toString());
        ParseFile profileImage = user.getParseFile("profilePicture");
        Log.i(TAG, "pic url: " + profileImage.getUrl());
        Glide.with(this)
                .load(profileImage.getUrl())
                .transform(new RoundedCornersTransformation(100, 20))
                .into(ivUserProfileImage);

        if(user != ParseUser.getCurrentUser()){
            btnLogOut.setVisibility(View.INVISIBLE);
            btnDiscoveryComment.setVisibility(View.INVISIBLE);
        }

        allPosts = new ArrayList<>();
        adapter = new ProfileFragmentAdapter(getContext(), allPosts);

        rvUserPosts.setAdapter(adapter);
        manager = new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL);
        manager.setGapStrategy(StaggeredGridLayoutManager.GAP_HANDLING_NONE);

        rvUserPosts.setLayoutManager(manager);


        btnLogOut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ParseUser.logOut();
                goLoginActivity();
            }
        });

        queryPosts();
    }

    private void goLoginActivity() {
        Intent i = new Intent(getContext(), IntroActivity.class);
        startActivity(i);
    }

    private void queryPosts() {
        ParseQuery<Post> query = ParseQuery.getQuery(Post.class);
        query.include(Post.KEY_USER);
        query.whereEqualTo(Post.KEY_USER, user);
        query.setLimit(100);
        query.addDescendingOrder(Post.KEY_CREATED_AT);
        query.findInBackground(new FindCallback<Post>() {
            @Override
            public void done(List<Post> posts, ParseException e) {
                if (e != null) {
                    //toast the output of e
                    Log.e(TAG, "cant get objects", e);
                    return;
                }
                for (Post post : posts) {
                    Log.i(TAG, "post: " + post.getDescription() + ", user: " + post.getUser().getUsername());
                }
                allPosts.addAll(posts);
                adapter.notifyDataSetChanged();
            }
        });
    }
}