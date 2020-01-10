package com.devkotasagar.listMe.test;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.ArrayList;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class PostListAdapter extends ArrayAdapter<Post> {
    private Context context;

    public PostListAdapter(@NonNull Context context, ArrayList<Post> posts) {
        super(context, 0, posts);
        this.context = context;
    }

    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        View listItemView = convertView;
        if (listItemView == null) {
            listItemView = LayoutInflater.from(getContext()).inflate(R.layout.post_on_list, parent, false);
        }

        final Post currentPost = getItem(position);

        TextView titleTextView = (TextView) listItemView.findViewById(R.id.titleTextView);
        titleTextView.setText(currentPost.getTitle());

        TextView bodyTextView = (TextView) listItemView.findViewById(R.id.bodyTextView);
        bodyTextView.setText(currentPost.getBody());

//        Log.e("TITLE", currentPost.getTitle());

        return listItemView;
    }
}
