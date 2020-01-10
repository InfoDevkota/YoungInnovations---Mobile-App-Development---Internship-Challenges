package com.devkotasagar.listMe.test;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class UserListAdapter extends ArrayAdapter<User> {
    private Context context;

    public UserListAdapter(@NonNull Context context, ArrayList<User> users) {
        super(context, 0, users);
        this.context = context;
    }

    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        View listItemView = convertView;
        if (listItemView == null) {
            listItemView = LayoutInflater.from(getContext()).inflate(R.layout.user_on_list, parent, false);
        }

        final User currentUser = getItem(position);

        TextView nameTextView = (TextView) listItemView.findViewById(R.id.nameView);
        nameTextView.setText(currentUser.getName());

        TextView emailTextView = (TextView) listItemView.findViewById(R.id.emailView);
        emailTextView.setText(currentUser.getEmail());

        listItemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //TODO start user profile with his/her post
                Toast aToast = Toast.makeText(context,"Clicked",Toast.LENGTH_LONG);
                aToast.show();
            }
        });

        return listItemView;
    }
}
