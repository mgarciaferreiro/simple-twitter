package com.codepath.apps.restclienttemplate;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CenterCrop;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.bumptech.glide.request.RequestOptions;
import com.codepath.apps.restclienttemplate.models.Tweet;
import com.loopj.android.http.JsonHttpResponseHandler;

import org.json.JSONObject;
import org.parceler.Parcels;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

import cz.msebera.android.httpclient.Header;

public class TweetAdapter extends RecyclerView.Adapter<TweetAdapter.ViewHolder> {

    private List<Tweet> mTweets;
    //Context context;
    TwitterClient client;
    Activity timeLineActivity;

    // pass in Tweets array
    public TweetAdapter(List<Tweet> tweets, Activity timeLineActivity) {
        mTweets = tweets;
        this.timeLineActivity = timeLineActivity;
    }

    // for each row, inflate layout and cache references into viewholder

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        //context = viewGroup.getContext();
        client = TwitterApp.getRestClient(timeLineActivity);
        LayoutInflater inflater = LayoutInflater.from(timeLineActivity);

        View tweetView = inflater.inflate(R.layout.item_tweet, viewGroup, false);
        ViewHolder viewHolder = new ViewHolder(tweetView);
        return viewHolder;
    }

    // bind values based on position of the element
    @SuppressLint("NewApi")
    @Override
    public void onBindViewHolder(@NonNull final ViewHolder viewHolder, final int position) {
        final Tweet tweet = mTweets.get(position);

        viewHolder.tvUsername.setText(tweet.user.name);
        viewHolder.tvBody.setText(tweet.body);
        viewHolder.tvScreenName.setText("@" + tweet.user.screenName);
        viewHolder.likeCount.setText(String.valueOf(tweet.favoriteCount));
        viewHolder.retweetCount.setText(String.valueOf(tweet.retweetCount));
        viewHolder.tvDate.setText(getRelativeTimeAgo(tweet.createdAt));

        RequestOptions requestOptions = new RequestOptions();
        requestOptions = requestOptions.transforms(new CenterCrop(), new RoundedCorners(100));
        System.out.println(tweet.mediaUrl);
        Glide.with(timeLineActivity)
                .load(tweet.user.profileImageUrl)
                .apply(requestOptions)
                .into(viewHolder.ivProfileImage);

        if (tweet.mediaUrl != null) {
            viewHolder.mediaView.setVisibility(View.VISIBLE);
            RequestOptions requestOptionsMedia = new RequestOptions();
            requestOptionsMedia = requestOptionsMedia.transforms(new CenterCrop(), new RoundedCorners(20));
            Glide.with(timeLineActivity)
                    .load(tweet.mediaUrl)
                    .apply(requestOptionsMedia)
                    .into(viewHolder.mediaView);
        } else {
            viewHolder.mediaView.setVisibility(View.GONE);
            Log.i("TweetAdapter", "no image");
        }

        if (tweet.isLikedByUser) {
            viewHolder.heartButton.setBackgroundTintList(timeLineActivity.getResources().getColorStateList(R.color.medium_red));
        } else {
            viewHolder.heartButton.setBackgroundTintList(timeLineActivity.getResources().getColorStateList(R.color.medium_gray));
        }

        if (tweet.isRetweeted) {
            viewHolder.retweetButton.setBackgroundTintList(timeLineActivity.getResources().getColorStateList(R.color.medium_green));
        } else {
            viewHolder.retweetButton.setBackgroundTintList(timeLineActivity.getResources().getColorStateList(R.color.medium_gray));
        }

        viewHolder.replyButton.setBackgroundTintList(timeLineActivity.getResources().getColorStateList(R.color.medium_gray));

        viewHolder.replyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(timeLineActivity, ComposeActivity.class);
                intent.putExtra("isReply", true);
                intent.putExtra("prevTweet", Parcels.wrap(tweet));
                timeLineActivity.startActivityForResult(intent, 2);
            }
        });

        viewHolder.retweetButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (tweet.isRetweeted) {
                    unretweet(v, position);
                    tweet.setRetweetedByUser(false);
                    notifyItemChanged(position);
                } else {
                    retweet(v, position);
                    tweet.setRetweetedByUser(true);
                    notifyItemChanged(position);
                }
            }
        });

        viewHolder.heartButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (tweet.isLikedByUser) {
                    unlikeTweet(v, position);
                    tweet.setLikedByUser(false);
                    notifyItemChanged(position);
                } else {
                    likeTweet(v, position);
                    tweet.setLikedByUser(true);
                    notifyItemChanged(position);
                }
            }
        });

        viewHolder.tvScreenName.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(timeLineActivity, UserActivity.class);
                intent.putExtra("user", Parcels.wrap(tweet.user));
                timeLineActivity.startActivity(intent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return mTweets.size();
    }

    // create ViewHolder class

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public ImageView ivProfileImage;
        public TextView tvUsername;
        public TextView tvBody;
        public TextView tvDate;
        public TextView tvScreenName;
        public Button replyButton;
        public Button retweetButton;
        public Button heartButton;
        public TextView retweetCount;
        public TextView likeCount;
        public ImageView mediaView;
        public TextView tvType;

        public ViewHolder(final View itemView) {
            super(itemView);
            ivProfileImage = itemView.findViewById(R.id.ivProfileImage);
            tvUsername = itemView.findViewById(R.id.tvUser);
            tvBody = itemView.findViewById(R.id.tvBody);
            tvDate = itemView.findViewById(R.id.tvDate);
            tvScreenName = itemView.findViewById(R.id.tvAccountName);
            replyButton = itemView.findViewById(R.id.replyButton);
            retweetButton = itemView.findViewById(R.id.retweetButton);
            heartButton = itemView.findViewById(R.id.heartButton);
            retweetCount = itemView.findViewById(R.id.tvRetweetCount);
            likeCount = itemView.findViewById(R.id.tvLikeCount);
            mediaView = itemView.findViewById(R.id.mediaView);
            tvType = itemView.findViewById(R.id.tvType);
        }
    }

    public void likeTweet(View view, int position) {
        Tweet tweet = mTweets.get(position);
        client.likeTweet(tweet.uid, new JsonHttpResponseHandler() {

            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                Log.i("TweetAdapter", "Liked tweet");
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                Log.d("TweetAdapter", responseString);
            }
        });
    }

    public void unlikeTweet(View view, int position) {
        Tweet tweet = mTweets.get(position);
        client.unlikeTweet(tweet.uid, new JsonHttpResponseHandler() {

            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                Log.i("TweetAdapter", "Unliked tweet");
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                Log.d("TweetAdapter", responseString);
            }
        });
    }

    public void retweet(final View view, int position) {
        final Tweet tweet = mTweets.get(position);
        client.retweet(tweet.uid, new JsonHttpResponseHandler() {

            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                mTweets.add(0, tweet);
                notifyItemInserted(0);
                RecyclerView rvTweets = timeLineActivity.findViewById(R.id.rvTweet);
                rvTweets.scrollToPosition(0);
                Log.i("TweetAdapter", "Retweeted tweet");
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                Log.d("TweetAdapter", responseString);
            }
        });
    }

    public void unretweet(View view, final int position) {
        Tweet tweet = mTweets.get(position);
        client.unretweet(tweet.uid, new JsonHttpResponseHandler() {

            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                notifyDataSetChanged();
                Log.i("TweetAdapter", "Unretweeted tweet");
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                Log.d("TweetAdapter", responseString);
            }
        });
    }

    // getRelativeTimeAgo("Mon Apr 01 21:16:23 +0000 2014");
    public String getRelativeTimeAgo(String rawJsonDate) {
        String twitterFormat = "EEE MMM dd HH:mm:ss ZZZZZ yyyy";
        SimpleDateFormat sf = new SimpleDateFormat(twitterFormat, Locale.ENGLISH);
        sf.setLenient(true);

        String relativeDate = "";
        try {
            long dateMillis = sf.parse(rawJsonDate).getTime();
            relativeDate = DateUtils.getRelativeTimeSpanString(dateMillis,
                    System.currentTimeMillis(), DateUtils.SECOND_IN_MILLIS, DateUtils.FORMAT_ABBREV_RELATIVE).toString();
        } catch (ParseException e) {
            e.printStackTrace();
        }

        return relativeDate;
    }

    // Clean all elements of the recycler
    public void clear() {
        mTweets.clear();
        notifyDataSetChanged();
    }

}
