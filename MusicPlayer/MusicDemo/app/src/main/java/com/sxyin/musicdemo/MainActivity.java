package com.sxyin.musicdemo;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.os.StrictMode;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import com.karumi.dexter.Dexter;
import com.karumi.dexter.MultiplePermissionsReport;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionDeniedResponse;
import com.karumi.dexter.listener.PermissionGrantedResponse;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.multi.MultiplePermissionsListener;
import com.karumi.dexter.listener.single.PermissionListener;

import org.json.JSONException;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.helper.StringUtil;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity {
    ListView listView;
    String [] items;
    customAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        if (android.os.Build.VERSION.SDK_INT > 9) {//防止避免主线程联网卡死的异常机制抛出异常
            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
            StrictMode.setThreadPolicy(policy);
        }
        listView = findViewById(R.id.listViewSong);
        runtimePermission();

    }

    @Override
    public void onBackPressed() {
        runtimePermission();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu,menu);
        MenuItem menuItem = menu.findItem(R.id.search_view);
        SearchView searchView = (SearchView) menuItem.getActionView();
        searchView.setQueryHint("Search Online Music");

        searchView.setOnCloseListener(new SearchView.OnCloseListener() {
            @Override
            public boolean onClose() {
                displaySongs();
                return false;
            }
        });
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                //todo after trigger the search networking
                OkHttpClient client = new OkHttpClient();
                String baseUrl = "https://freemusicarchive.org/search/?quicksearch="+ searchView.getQuery();
                Request request = new Request.Builder()
                        .url(baseUrl)
                        .build();
                client.newCall(request).enqueue(new Callback() {
                    @Override
                    public void onFailure(@NonNull Call call, @NonNull IOException e) {
                        e.printStackTrace();
                    }
                    @Override
                    public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {

                        if(response.isSuccessful()) {
                            final String myResponse = response.body().string();
                            MainActivity.this.runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    Document doc = Jsoup.parse(myResponse);
                                    Elements elements = doc.select("div[data-track-info]");
                                    displaySongs(elements);
                                }
                            });

                        }
                    }
                });
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        if(id == R.id.search_view){
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public void runtimePermission(){
        Dexter.withContext(this).withPermissions(Manifest.permission.READ_EXTERNAL_STORAGE,Manifest.permission.RECORD_AUDIO)
                .withListener(new MultiplePermissionsListener() {
                    @Override
                    public void onPermissionsChecked(MultiplePermissionsReport multiplePermissionsReport) {
                        displaySongs();
                    }

                    @Override
                    public void onPermissionRationaleShouldBeShown(List<PermissionRequest> list, PermissionToken permissionToken) {
                        permissionToken.continuePermissionRequest();
                    }
                }).check();
    }

    public ArrayList<File> findSong(File file){
        ArrayList<File> arrayList = new ArrayList<>();
        File[] files = file.listFiles();

        assert files != null;
        for(File singlefile: files){
            if(singlefile.isDirectory() && !singlefile.isHidden()){
                arrayList.addAll(findSong(singlefile)); //recursive find songs file
            }
            else {
                if(singlefile.getName().endsWith(".mp3") || singlefile.getName().endsWith((".wav"))){
                    arrayList.add(singlefile);
                }
            }
        }
        return arrayList;
    }

    void displaySongs(){

            final ArrayList<File> mySongs = findSong(new File(Environment.getExternalStorageDirectory().getPath()+"/Download"));
            items = new String[mySongs.size()];
            for(int i = 0; i < mySongs.size(); i++){
                items[i] = mySongs.get(i).getName().toString().replace(".mp3","").replace(".wav","");
            }
        adapter = new customAdapter();
        listView.setAdapter(adapter);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String songName = (String) listView.getItemAtPosition(position);
                startActivity(new Intent(getApplicationContext(),PlayerActivity.class)
                .putExtra("songs",mySongs)
                .putExtra("songname",songName)
                .putExtra("pos",position));
            }
        });
    }
    void displaySongs(Elements elements){
        String songName, artistName,downloadUrl;
        String temp="";
        JSONObject jsonObject;
        ArrayList<songInfo> mySongs = new ArrayList<>();
        for (Element element:elements){
            temp = element.attr("data-track-info");
            if(temp.contains("title")&&temp.contains("artistName")&&temp.contains("downloadUrl")){//保证一一对应
                try {
                    jsonObject= new JSONObject(temp);
                    songName = jsonObject.getString("title");
                    artistName = jsonObject.getString("artistName");
                    downloadUrl = jsonObject.getString("downloadUrl");
                    mySongs.add(new songInfo(songName,artistName,downloadUrl));
//                    Log.e("歌",songName+"--"+artistName+"   "+downloadUrl);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }
        items = new String[mySongs.size()];
        for (int i = 0; i < mySongs.size(); i++){
            items[i] = mySongs.get(i).getItemInfo();
        }
        adapter = new customAdapter();
        listView.setAdapter(adapter);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {//position starts from 0 as from top to bottom
                String info = (String) listView.getItemAtPosition(position);
                String downloadUrl = mySongs.get(position).getDownloadUrl();
                //solve Redirection

                Intent downloadIntent = new Intent(MainActivity.this,DownloadService.class)
                        .putExtra("songName_artistName",mySongs.get(position).getTitle()+mySongs.get(position).getArtistName())
                        .putExtra("downloadUrl", downloadUrl)
                        ;
                startService(downloadIntent);


                //todo: startActivity? intentService download songs
            }
        });

    }

    class customAdapter extends BaseAdapter implements Filterable {
        @Override
        public int getCount() {
            return items.length;
        }

        @Override
        public Object getItem(int position) {
            return null;
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            @SuppressLint({"ViewHolder", "InflateParams"}) View myView = getLayoutInflater().inflate(R.layout.list_item,null);
            TextView textSong = myView.findViewById(R.id.txtsongname);
            textSong.setSelected(true);
            textSong.setText(items[position]);
            return myView;
        }

        @Override
        public Filter getFilter() {
            return null;
        }
    }

    class songInfo {
        String title;
        String artistName;
        String downloadUrl;

        public songInfo(String title, String artistName, String downloadUrl) {
            this.title = title;
            this.artistName = artistName;
            this.downloadUrl = downloadUrl;
        }

        public String getTitle() {
            return title;
        }
        public String getArtistName() {
            return artistName;
        }
        public String getDownloadUrl() {
            return downloadUrl;
        }
        public String getItemInfo(){
            return getTitle() + "--" + getArtistName();
        }
    }
}