package com.devkotasagar.listMe.test;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.devkotasagar.listMe.test.data.ListContract;
import com.devkotasagar.listMe.test.data.ListMeDBHelper;

public class UserActivity extends AppCompatActivity {
    int userId;
    String usersName;
    Context context;

    View noDataView;
    View postsView;
    View loadingIndicator;

    TextView nameTextView;
    TextView emailTextView;
    TextView phoneTextView;
    TextView streetTextView;
    TextView zipTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user);
        context = this;

        userId = getIntent().getIntExtra("id", 0);
        setTitle(getIntent().getStringExtra("name"));

        noDataView = findViewById(R.id.no_data);
        loadingIndicator = findViewById(R.id.loading_indicator);
        postsView = findViewById(R.id.postsView);

        nameTextView = findViewById(R.id.nameTextView);
        emailTextView = findViewById(R.id.emailTextView);
        phoneTextView = findViewById(R.id.phoneTextView);
        streetTextView = findViewById(R.id.streetTextView);
        zipTextView = findViewById(R.id.zipTextView);

        showFromDb();
    }

    void showFromDb(){
        ListMeDBHelper listMeDBHelper = new ListMeDBHelper(context);
        SQLiteDatabase database = listMeDBHelper.getReadableDatabase();

        //the value from intent is th id from json so use idd wala for selection
        String[] projection = null; //we need all so null will be okay.
        String selection = ListContract.UserEntry.COLUMN_USER_IDD + " = ? "; //where thisvalues = args[]
        String[] selectionArgs = new String[] { String.valueOf(userId)}; //where selection = this values

        Cursor c = null;
        try {
            c = database.query(ListContract.UserEntry.TABLE_NAME, projection, selection, selectionArgs, null, null, null);
            int count = c.getCount();
            if(count == 0){
                //This should never reach :)
                return;
            }
            int idColumnIndex = c.getColumnIndex(ListContract.UserEntry.COLUMN_USER_IDD);
            int nameColumnIndex = c.getColumnIndex(ListContract.UserEntry.COLUMN_NAME);
            int emailColumnIndex = c.getColumnIndex(ListContract.UserEntry.COLUMN_EMAIL);
            int phoneColumnIndex = c.getColumnIndex(ListContract.UserEntry.COLUMN_phone);
            int streetColumnIndex = c.getColumnIndex(ListContract.UserEntry.COLUMN_ADDRESS_STREET);
            int zipColumnIndex = c.getColumnIndex(ListContract.UserEntry.COLUMN_ADDRESS_ZIP);

            while(c.moveToNext()) {
                String name = c.getString(nameColumnIndex);
                int id = c.getInt(idColumnIndex);
                String email = c.getString(emailColumnIndex);
                String phone = c.getString(phoneColumnIndex);
                String street = c.getString(streetColumnIndex);
                String zip = c.getString(zipColumnIndex);

                nameTextView.setText(name);
                emailTextView.setText(email);
                phoneTextView.setText(phone);
                streetTextView.setText(street);
                zipTextView.setText(zip);
            }

        } catch (Exception e) {
            Log.e("MAIN", "displayFromDB()\n"+ e);
            Toast aToast = Toast.makeText(context,"Something Went Wrong :(",Toast.LENGTH_LONG);
            aToast.show();
        }

        noDataView.setVisibility(View.VISIBLE); //for now as there is no post in the system.

    }

    public void sync(View v){
        //saveToDB();
    }
}
