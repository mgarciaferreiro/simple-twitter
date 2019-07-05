package com.codepath.apps.restclienttemplate;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CenterCrop;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.bumptech.glide.request.RequestOptions;
import com.codepath.apps.restclienttemplate.models.User;

import org.parceler.Parcels;

public class UserActivity extends AppCompatActivity {

    User user;
    ImageView profile;
    ImageView background;
    TextView name;
    TextView screenName;
    TextView description;
    TextView friends;
    TextView followers;
    RecyclerView rvFollowers;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user);

        profile = findViewById(R.id.profile);
        background = findViewById(R.id.background);
        name = findViewById(R.id.name);
        screenName = findViewById(R.id.screenName);
        description = findViewById(R.id.description);
        friends = findViewById(R.id.friends);
        followers = findViewById(R.id.followers);
        rvFollowers = findViewById(R.id.rvFollowers);
        user = Parcels.unwrap(getIntent().getParcelableExtra("user"));

        name.setText(user.name);
        screenName.setText(user.screenName);
        description.setText(user.description);
        followers.setText(String.valueOf(user.followersCount) + " Followers");
        friends.setText(String.valueOf(user.friendsCount) + " Friends");

        RequestOptions requestOptionsMedia = new RequestOptions();
        requestOptionsMedia = requestOptionsMedia.transforms(new CenterCrop(), new RoundedCorners(100));
        Glide.with(this)
                .load(user.profileImageUrl)
                .apply(requestOptionsMedia)
                .into(profile);

        Glide.with(this)
                .load(user.backgroundImageUrl)
                .into(background);
    }
}
