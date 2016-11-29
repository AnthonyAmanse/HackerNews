package com.example.ghiea.hackernews;

import android.content.Intent;
import android.graphics.Color;
import android.os.AsyncTask;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    ListView headlinesList;
    ArrayList<Integer> topStoriesInt = new ArrayList<>();
    ArrayList<String> titleList = new ArrayList<>();
    static ArrayList<String> urlList = new ArrayList<>();
    ArrayList<String> visibleTitleList = new ArrayList<>();
    ArrayAdapter<String> arrayAdapter;
    int test = 0;
    boolean flag_loading = false;
    boolean snack_flag = false;
    Snackbar snackbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        snackbar = Snackbar.make(findViewById(android.R.id.content), "Load More", Snackbar.LENGTH_LONG)
                .setAction("LOAD", new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        flag_loading = true;
                        addItems();
                    }
                })
                .setActionTextColor(Color.WHITE);

        headlinesList = (ListView) findViewById(R.id.headlinesList);
        arrayAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, visibleTitleList);
        DownloadTask downloadTask = new DownloadTask();
        downloadTask.execute("https://hacker-news.firebaseio.com/v0/topstories.json");
        headlinesList.setAdapter(arrayAdapter);
        headlinesList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Intent intent = new Intent(getApplicationContext(), WebActivity.class);
                intent.putExtra("indexURL", i);
                startActivity(intent);
            }
        });
        headlinesList.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView absListView, int i) {

                if(snackbar.isShown() && i == SCROLL_STATE_FLING) {
                    snackbar.dismiss();
                    snack_flag = false;
                }
                if(!snackbar.isShown() && i == SCROLL_STATE_IDLE) {
                    snack_flag = false;
                }
            }

            @Override
            public void onScroll(AbsListView absListView, int i, int i1, int i2) {
                if(i+i1 == i2 && i2 != 0 && visibleTitleList.size() >= 50) {
                    if(!flag_loading && !snack_flag) {
//                        Toast.makeText(MainActivity.this, "end", Toast.LENGTH_SHORT).show();
//                        flag_loading = true;
//                        addItems();
                        snackbar.show();
                        snack_flag = true;
                    }
                }
            }
        });


    }

    public void addItems() {
        try {
            int currentMax = visibleTitleList.size();
            for (int i = 0; i < 51; i++) {
                visibleTitleList.add(titleList.get(currentMax + i));
            }
            arrayAdapter.notifyDataSetChanged();
            flag_loading = false;
            snack_flag = false;
        } catch (Exception e) {
            flag_loading = false;
            snack_flag = false;
            e.printStackTrace();
        }
    }

    public class DownloadTaskTopStories extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... strings) {
            String result = "";
            URL url;
            HttpURLConnection urlConnection;
            try {
                url = new URL(strings[0]);
                urlConnection = (HttpURLConnection) url.openConnection();
                InputStream inputStream = urlConnection.getInputStream();
                InputStreamReader inputStreamReader = new InputStreamReader(inputStream);

                int data = inputStreamReader.read();
                while(data != -1) {
                    char current = (char) data;
                    result += current;
                    data = inputStreamReader.read();
                }
                return result;
            } catch (Exception e) {
                e.printStackTrace();
            }
            return "Failed";
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            try {
                JSONObject jsonObject = new JSONObject(s);
                String author = jsonObject.getString("by");
                String title = jsonObject.getString("title");
                String url = jsonObject.getString("url");

//                Log.i("AUTHOR", author);
//                Log.i("TITLE", title);
//                String titleTest = test + "-" + title;
                titleList.add(title);
                if(titleList.size() < 51) {
                    visibleTitleList.add(title);
                } else {

                }
//                Log.i("URL", url);
                urlList.add(url);
                test++;
            } catch (JSONException e) {
                e.printStackTrace();
            }
            arrayAdapter.notifyDataSetChanged();
        }
    }

    public class DownloadTask extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... strings) {
            String result = "";
            URL url;
            HttpURLConnection urlConnection;
            try {
                url = new URL(strings[0]);
                urlConnection = (HttpURLConnection) url.openConnection();
                InputStream inputStream = urlConnection.getInputStream();
                InputStreamReader inputStreamReader = new InputStreamReader(inputStream);

                int data = inputStreamReader.read();
                while(data != -1) {
                    char current = (char) data;
                    result += current;
                    data = inputStreamReader.read();
                }
                return result;
            } catch (Exception e) {
                e.printStackTrace();
            }
            return "Failed";
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            try {
                JSONArray jsonArray = new JSONArray(s);
                for(int i = 0; i < jsonArray.length(); i++) {
                    topStoriesInt.add((Integer) jsonArray.get(i));
                }
                for(int storiesInt: topStoriesInt) {
                    DownloadTaskTopStories downloadTaskTopStories = new DownloadTaskTopStories();
                    downloadTaskTopStories.execute("https://hacker-news.firebaseio.com/v0/item/" + storiesInt + ".json");
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }

        }
    }
}