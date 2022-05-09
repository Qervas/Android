package com.sxyin.musicdemo;

import static java.net.HttpURLConnection.HTTP_MOVED_PERM;
import static java.net.HttpURLConnection.HTTP_MOVED_TEMP;
import static java.net.HttpURLConnection.HTTP_SEE_OTHER;

import android.app.DownloadManager;
import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Environment;
import android.os.StrictMode;
import android.util.Log;
import android.widget.Toast;


import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.net.URLConnection;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class DownloadService extends IntentService {
    String downloadUrl = "", songName_artistName = "";
    public DownloadService() {
        super("DownloadService");
    }

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public int onStartCommand(@Nullable Intent intent, int flags, int startId) {
        if (intent != null) {
            songName_artistName = intent.getStringExtra("songName_artistName");
            downloadUrl = intent.getStringExtra("downloadUrl");
            //solve redirection
            String realUrl = null;
            try {
                realUrl = getRedirectUrl(downloadUrl);
                DownloadTask(songName_artistName,realUrl);

            } catch (IOException e) {
                e.printStackTrace();
            }
            Toast.makeText(getApplicationContext(), realUrl, Toast.LENGTH_SHORT).show();
            Log.e("link",realUrl);

        }

        return super.onStartCommand(intent, flags, startId);
    }
    public String getRedirectUrl(String downloadUrl) throws IOException {
        Log.e("redirecting",downloadUrl);
        HttpURLConnection con = (HttpURLConnection) new URL(downloadUrl).openConnection();
        con.setInstanceFollowRedirects(false);
        con.connect();
        con.getInputStream();

        if (con.getResponseCode() == HttpURLConnection.HTTP_MOVED_PERM || con.getResponseCode() == HttpURLConnection.HTTP_MOVED_TEMP) {
            String redirectUrl = con.getHeaderField("Location");
            return getRedirectUrl(redirectUrl);
        }
        return downloadUrl;
    }
    public void DownloadTask(String filename, String realUrl) throws IOException {
        Log.e("downloading",filename);
        URL url = new URL(downloadUrl);
        //打开连接
        URLConnection conn = url.openConnection();
        //打开输入流
        InputStream is = conn.getInputStream();
        //获得长度
        int contentLength = conn.getContentLength();
        Log.e("哦好", "contentLength = " + contentLength);
        //创建文件夹 MyDownLoad，在存储卡下
        String dirName = Environment.getExternalStorageDirectory().getPath()+"/Download/";
        File file = new File(dirName);
        //不存在创建
        if (!file.exists()) {
            file.mkdir();
        }
        //下载后的文件名
        String fileName = dirName + filename+".mp3";
        File file1 = new File(fileName);
        if (file1.exists()) {
            file1.delete();
        }
        //创建字节流
        byte[] bs = new byte[1024];
        int len;
        OutputStream os = new FileOutputStream(fileName);
        //写数据
        while ((len = is.read(bs)) != -1) {
            os.write(bs, 0, len);
        }
        //完成后关闭流
        Toast.makeText(getApplicationContext(), "download finished", Toast.LENGTH_SHORT).show();
        Log.e("哦好", "download-finish");
        os.close();
        is.close();

    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }
}
