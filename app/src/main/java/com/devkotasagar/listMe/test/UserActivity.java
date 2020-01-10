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
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;
import android.widget.TextView;
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

public class UserActivity extends AppCompatActivity {
    int userId;
    String usersName;
    Context context;

    View noDataView;
    ListView postsView;
    View loadingIndicator;

    TextView nameTextView;
    TextView emailTextView;
    TextView phoneTextView;
    TextView streetTextView;
    TextView zipTextView;

    final String BASE_URL = "https://jsonplaceholder.typicode.com/users/";
    String URL_TO_FETCH;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user);
        context = this;

        userId = getIntent().getIntExtra("id", 0);
        setTitle(getIntent().getStringExtra("name"));
        URL_TO_FETCH = BASE_URL + userId + "/posts";

        noDataView = findViewById(R.id.no_data);
        loadingIndicator = findViewById(R.id.loading_indicator);
        postsView = findViewById(R.id.postsView);

        nameTextView = findViewById(R.id.nameTextView);
        emailTextView = findViewById(R.id.emailTextView);
        phoneTextView = findViewById(R.id.phoneTextView);
        streetTextView = findViewById(R.id.streetTextView);
        zipTextView = findViewById(R.id.zipTextView);

        displayFromDb();
    }

    void displayFromDb(){
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


        ArrayList<Post> posts = new ArrayList<>();

        projection = null; //we need all so null will be okay.
        selection = ListContract.PostEntry.COLUMN_USER_ID + " = ? "; //where thisvalues = args[]
        selectionArgs = new String[] { String.valueOf(userId)}; //where selection = this values

        c = null;
        try {
            c = database.query(ListContract.PostEntry.TABLE_NAME, projection, selection, selectionArgs, null, null, null);
            int count = c.getCount();
//            Log.e("COUNT", count+"");
            if(count == 0){
                //There is no data in the system.
                saveToDB(); //will try to fetch from server of no internet will display message and set no data to visible.
                //noDataView.setVisibility(View.VISIBLE);
                return;
            }
            int titleColumnIndex = c.getColumnIndex(ListContract.PostEntry.COLUMN_TITLE);
            int bodyColumnIndex = c.getColumnIndex(ListContract.PostEntry.COLUMN_BODY);

            while(c.moveToNext()) {
                String body = c.getString(bodyColumnIndex);
                String title = c.getString(titleColumnIndex);

//                Log.e("TITLE", title+"");

                Post aPost = new Post(title, body, userId);
                posts.add(aPost);
            }
            updateUI(posts);

        } catch (Exception e) {
            Log.e("MAIN", "displayFromDB()\n"+ e);
            Toast aToast = Toast.makeText(context,"Something Went Wrong :(",Toast.LENGTH_LONG);
            aToast.show();
        }

    }

    public void sync(View v){
        saveToDB();
    }

    void updateUI(ArrayList<Post> posts){
        PostListAdapter adapter = new PostListAdapter(this, posts);

        loadingIndicator.setVisibility(View.GONE);
        postsView.setAdapter(adapter);
    }

    private void saveToDB(){
        ConnectivityManager connMgr = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        try {
            NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
            if (networkInfo != null && networkInfo.isConnected()) {
                loadingIndicator.setVisibility(View.VISIBLE);
                noDataView.setVisibility(View.GONE);
                PostAsyncTask task = new PostAsyncTask(URL_TO_FETCH);
                task.execute();
            } else {
                noDataView.setVisibility(View.VISIBLE);
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

    private class PostAsyncTask extends AsyncTask<URL, Void, ArrayList<Post>> {
        String urlToFetch;

        PostAsyncTask(String urlToFetch) {
            this.urlToFetch = urlToFetch;
        }

        @Override
        protected ArrayList<Post> doInBackground(URL... urls) {
            URL url = createUrl(urlToFetch);

            String jsonResponse = null;
            try {
                jsonResponse = makeHttpRequest(url);
            } catch (IOException e) {
                e.printStackTrace();
            }

            ArrayList<Post> posts = makePostsFromJson(jsonResponse);

            return posts;
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

        public ArrayList<Post> makePostsFromJson(String jsonString) {

            System.out.println(jsonString);

            ArrayList<Post> posts = new ArrayList<>();
            try {
                JSONArray jsonPosts =  new JSONArray(jsonString);
                int i;
                for(i=0; i < (jsonPosts.length());i++) {
                    JSONObject aJSONPost = jsonPosts.getJSONObject(i);
//                    int id = aJSONPost.getInt("id");
                    String body = aJSONPost.getString("body");
                    String title = aJSONPost.getString("title");


                    Post aPost = new Post(title,body,userId);
                    posts.add(aPost);
                }

            } catch (JSONException e) {
                Log.e("PostAsyncTask", "makePostFromJSON \n" + e);
                //handel
            }
            return posts;
        }

        @Override
        protected  void onPostExecute(ArrayList<Post> posts){
//            Log.e("POSTS", posts.toString());
            updateUI(posts);
            insertIntoDB(posts);
        }
    }

    public void insertIntoDB(ArrayList<Post> posts){

        //It would also be better if this function is also executed in async task
        //Here lets try with async task
        SaveinDBAsyncTask task = new SaveinDBAsyncTask(posts);
        task.execute();
        displayFromDb();
    }

    private class SaveinDBAsyncTask extends AsyncTask<Void, Void, Void> {
        ArrayList<Post> posts;

        SaveinDBAsyncTask(ArrayList<Post> posts){
            this.posts = posts;
        }

        @Override
        protected Void doInBackground(Void... voids) {
            ListMeDBHelper listMeDBHelper = new ListMeDBHelper(context);
            SQLiteDatabase database = listMeDBHelper.getWritableDatabase();

            //Here we first clear all the database and insert new ones [when added referesh button].
            database.execSQL("DELETE from "+ ListContract.PostEntry.TABLE_NAME + " Where " + ListContract.PostEntry.COLUMN_USER_ID +" = " + userId);

            ContentValues values = new ContentValues();

            for(Post post:posts){
                String body = post.getBody();
                String title = post.getTitle();

                values.put(ListContract.PostEntry.COLUMN_BODY, body);
                values.put(ListContract.PostEntry.COLUMN_TITLE, title);
                values.put(ListContract.PostEntry.COLUMN_USER_ID, userId);

                database.insert(ListContract.PostEntry.TABLE_NAME,null,values);
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            displayFromDb();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.refresh,menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.refresh:
                saveToDB();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
