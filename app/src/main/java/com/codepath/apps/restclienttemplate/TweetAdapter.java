package com.codepath.apps.restclienttemplate;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v4.graphics.drawable.DrawableCompat;
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

import org.json.JSONArray;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

import cz.msebera.android.httpclient.Header;

public class TweetAdapter extends RecyclerView.Adapter<TweetAdapter.ViewHolder> {

    private List<Tweet> mTweets;
    Context context;
    TwitterClient client;

    // pass in Tweets array
    public TweetAdapter(List<Tweet> tweets) {
        mTweets = tweets;
    }

    // for each row, inflate layout and cache references into viewholder

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        context = viewGroup.getContext();
        client = TwitterApp.getRestClient(context);
        LayoutInflater inflater = LayoutInflater.from(context);

        View tweetView = inflater.inflate(R.layout.item_tweet, viewGroup, false);
        ViewHolder viewHolder = new ViewHolder(tweetView);
        return viewHolder;
    }

    // bind values based on position of the element
    @Override
    public void onBindViewHolder(@NonNull final ViewHolder viewHolder, final int position) {
        final Tweet tweet = mTweets.get(position);

        viewHolder.tvUsername.setText(tweet.user.name);
        viewHolder.tvBody.setText(tweet.body);
        viewHolder.tvScreenName.setText("@" + tweet.user.screenName);
        viewHolder.likeCount.setText(String.valueOf(tweet.favoriteCount));
        viewHolder.retweetCount.setText(String.valueOf(tweet.retweetCount));

        RequestOptions requestOptions = new RequestOptions();
        requestOptions = requestOptions.transforms(new CenterCrop(), new RoundedCorners(100));

        Glide.with(context)
                .load(tweet.user.profileImageUrl)
                .apply(requestOptions)
                .into(viewHolder.ivProfileImage);
        viewHolder.tvDate.setText(getRelativeTimeAgo(tweet.createdAt));

        if (tweet.isLikedByUser) {
            DrawableCompat.setTint(
                    DrawableCompat.wrap(viewHolder.heartButton.getBackground()),
                    ContextCompat.getColor(context, R.color.medium_red)
            );
        } else {
            DrawableCompat.setTint(
                    DrawableCompat.wrap(viewHolder.heartButton.getBackground()),
                    ContextCompat.getColor(context, R.color.medium_gray)
            );
        }
        DrawableCompat.setTint(
                DrawableCompat.wrap(viewHolder.replyButton.getBackground()),
                ContextCompat.getColor(context, R.color.medium_gray)
        );
        DrawableCompat.setTint(
                DrawableCompat.wrap(viewHolder.retweetButton.getBackground()),
                ContextCompat.getColor(context, R.color.medium_gray)
        );

        viewHolder.replyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO: implement reply
            }
        });

        viewHolder.retweetButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                retweet(v, position);
                DrawableCompat.setTint(
                        DrawableCompat.wrap(viewHolder.retweetButton.getBackground()),
                        ContextCompat.getColor(context, R.color.medium_green)
                );
            }
        });

        viewHolder.heartButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (tweet.isLikedByUser) {
                    unlikeTweet(v, position);
                    tweet.setLikedByUser(false);
                    DrawableCompat.setTint(
                            DrawableCompat.wrap(viewHolder.heartButton.getBackground()),
                            ContextCompat.getColor(context, R.color.medium_gray)
                    );
                } else {
                    likeTweet(v, position);
                    tweet.setLikedByUser(true);
                    DrawableCompat.setTint(
                            DrawableCompat.wrap(viewHolder.heartButton.getBackground()),
                            ContextCompat.getColor(context, R.color.medium_red)
                    );
                }
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

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                Log.d("TweetAdapter", errorResponse.toString());
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONArray errorResponse) {
                Log.d("TweetAdapter", errorResponse.toString());
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

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                Log.d("TweetAdapter", errorResponse.toString());
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONArray errorResponse) {
                Log.d("TweetAdapter", errorResponse.toString());
            }

        });
    }

    public void retweet(View view, int position) {
        Tweet tweet = mTweets.get(position);
        client.retweet(tweet.uid, new JsonHttpResponseHandler() {

            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                Log.i("TweetAdapter", "Retweeted tweet");
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                Log.d("TweetAdapter", responseString);
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                Log.d("TweetAdapter", errorResponse.toString());
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONArray errorResponse) {
                Log.d("TweetAdapter", errorResponse.toString());
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

    public void addMoreTweets(List<Tweet> newTweets) {
        mTweets.addAll(newTweets);
    }

}
