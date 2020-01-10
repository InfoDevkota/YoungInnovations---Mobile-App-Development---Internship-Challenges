package com.devkotasagar.listMe.test;

import androidx.appcompat.app.AppCompatActivity;

import android.content.ContentValues;
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

    private final String USER_URL = "https://jsonplaceholder.typicode.com/users/"; //API endpoint to get users

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
            int idColumnIndex = c.getColumnIndex(ListContract.UserEntry.COLUMN_USER_IDD);//the id from jsonData
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

            updateUI(users);
        } catch (Exception e) {
            Log.e("MAIN", "displayFromDB()\n"+ e);
            Toast aToast = Toast.makeText(context,"Something Went Wrong :(",Toast.LENGTH_LONG);
            aToast.show();
        }
    }

    void updateUI(ArrayList<User> users){
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
                UserAsyncTask task = new UserAsyncTask(USER_URL);
                task.execute();
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

    private class UserAsyncTask extends AsyncTask<URL, Void, ArrayList<User>> {
        String urlToFetch;

        UserAsyncTask(String urlToFetch) {
            this.urlToFetch = urlToFetch;
        }

        @Override
        protected ArrayList<User> doInBackground(URL... urls) {
            URL url = createUrl(urlToFetch);

            String jsonResponse = null;
            try {
                jsonResponse = makeHttpRequest(url);
            } catch (IOException e) {
                e.printStackTrace();
            }

            ArrayList<User> users = makeUsersFromJson(jsonResponse);

            return users;
        }

        private URL createUrl(String stringUrl) {
            URL url;
            try {
                url = new URL(stringUrl);
            } catch (MalformedURLException e) {
                e.printStackTrace();
                return null;
            }
            return url;
        }

        private String makeHttpRequest(URL url) throws IOException {
            String jsonResponce = "";
            HttpURLConnection httpURLConnection = null;
            InputStream inputStream = null;
            try {
                httpURLConnection = (HttpURLConnection) url.openConnection();
                httpURLConnection.setRequestMethod("GET");
                httpURLConnection.setConnectTimeout(1000);
                httpURLConnection.setReadTimeout(1500);
                httpURLConnection.connect();
                inputStream = httpURLConnection.getInputStream();
                jsonResponce = readFromStream(inputStream);

            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                if (httpURLConnection != null) {
                    httpURLConnection.disconnect();
                }
                if (inputStream != null) {
                    inputStream.close();
                }
            }
            return jsonResponce;
        }

        private String readFromStream(InputStream inputStream) throws IOException {
            StringBuilder jsonResponce = new StringBuilder();
            if (inputStream != null) {
                InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
                BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
                String line = bufferedReader.readLine();
                while (line != null) {
                    jsonResponce.append(line);
                    line = bufferedReader.readLine();
                }
            }

            return jsonResponce.toString();
        }

        public ArrayList<User> makeUsersFromJson(String jsonString) {

            System.out.println(jsonString);

            ArrayList<User> users = new ArrayList<>();
            try {
                JSONArray jsonUsers =  new JSONArray(jsonString);
                int i;
                for(i=0; i < (jsonUsers.length());i++) {
                    JSONObject aJSONUser = jsonUsers.getJSONObject(i);
                    int id = aJSONUser.getInt("id");
                    String name = aJSONUser.getString("name");
                    String phone = aJSONUser.getString("phone");
                    String email = aJSONUser.getString("email");

                    JSONObject jsonAddress = aJSONUser.getJSONObject("address");
                    String street = jsonAddress.getString("street");
                    String zip = jsonAddress.getString("zipcode");

                    User aUser = new User(name,id,email,phone,street,zip);
                    users.add(aUser);
                }

            } catch (JSONException e) {
                Log.e("UserAsyncTask", "makeUsersFromJson \n" + e);
                //handel
            }
            return users;
        }

        @Override
        protected  void onPostExecute(ArrayList<User> users){
            updateUI(users);
            insertIntoDB(users);
        }
    }

    public void insertIntoDB(ArrayList<User> users){

        //It would also be better if this function is also executed in async task

        ListMeDBHelper listMeDBHelper = new ListMeDBHelper(context);
        SQLiteDatabase database = listMeDBHelper.getWritableDatabase();

        //Here we first clear all the database and insert new ones [when added referesh button].
        database.execSQL("DELETE from "+ ListContract.UserEntry.TABLE_NAME);

        ContentValues values = new ContentValues();

        for(User user:users){
            String name = user.getName();
            String email = user.getEmail();
            int id = user.getId();
            String phone = user.getPhone();
            String street = user.getStreet();
            String zip = user.getZip();

            values.put(ListContract.UserEntry.COLUMN_NAME, name);
            values.put(ListContract.UserEntry.COLUMN_EMAIL, email);
            values.put(ListContract.UserEntry.COLUMN_USER_IDD, id); //in DB idd is for Id from API
            values.put(ListContract.UserEntry.COLUMN_phone, phone);
            values.put(ListContract.UserEntry.COLUMN_ADDRESS_STREET, street);
            values.put(ListContract.UserEntry.COLUMN_ADDRESS_ZIP, zip);

            database.insert(ListContract.UserEntry.TABLE_NAME,null,values);
        }
        displayFromDb();
    }
}
