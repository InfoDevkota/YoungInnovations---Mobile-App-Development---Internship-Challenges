package com.devkotasagar.listMe.test;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ListView;
import android.widget.Toast;

import com.devkotasagar.listMe.test.data.ListContract;
import com.devkotasagar.listMe.test.data.ListMeDBHelper;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {
    Context context;
    ListView rootView;
    View noDataView;
    View loadingIndicator;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        context = this;

        rootView = findViewById(R.id.rootView);
        noDataView = findViewById(R.id.no_data);
        loadingIndicator = findViewById(R.id.loading_indicator);

        displayFromDb();//Will show users if available in DB.
    }

    void displayFromDb(){
        ArrayList<User> users = new ArrayList<>();

        ListMeDBHelper listMeDBHelper = new ListMeDBHelper(context);
        SQLiteDatabase database = listMeDBHelper.getReadableDatabase();

        Cursor c = null;
        try {
            c = database.query(ListContract.UserEntry.TABLE_NAME, null, null, null, null, null, null);
            int count = c.getCount();
            if(count == 0){
                //no data in the database show sync to server
                //For now we have a test data so should have some data
                noDataView.setVisibility(View.VISIBLE);
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

                User aUser = new User(name,id, email,phone,street,zip);
                users.add(aUser);
            }

            UpdateUI(users);
        } catch (Exception e) {
            Log.e("MAIN", "displayFromDB()\n"+ e);
            Toast aToast = Toast.makeText(context,"Something Went Wrong :(",Toast.LENGTH_LONG);
            aToast.show();
        }
    }

    void UpdateUI(ArrayList<User> users){
        UserListAdapter adapter = new UserListAdapter(this, users);

        loadingIndicator.setVisibility(View.GONE);
        rootView.setAdapter(adapter);
    }

    private void saveToDB(){
        ConnectivityManager connMgr = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        try {
            NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
            if (networkInfo != null && networkInfo.isConnected()) {
                loadingIndicator.setVisibility(View.VISIBLE);
                noDataView.setVisibility(View.GONE);
                //TODO Create and execute a async task to save from json data
            } else {
                Toast aToast = Toast.makeText(this,"Internet Connection Required",Toast.LENGTH_LONG);
                aToast.show();
            }
        } catch (Exception e) {
            //
            Log.e("MAIN", "saveToDB() \n" + e);
            Toast aToast = Toast.makeText(this,"Something Went Wrong. :(",Toast.LENGTH_LONG);
            aToast.show();
        }
    }

    public void sync(View v){
        saveToDB();
    }


}
