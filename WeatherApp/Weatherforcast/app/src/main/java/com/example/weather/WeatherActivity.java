package com.example.weather;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.util.Objects;

import com.bumptech.glide.Glide;
import com.example.weather.gson.Weather;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

import com.example.weather.util.HttpUtil;
import com.example.weather.util.Utility;

import static com.example.weather.MyDBhelper.DB_NAME;
import static com.example.weather.MyDBhelper.TABLE_NAME;

public class WeatherActivity extends AppCompatActivity {

    public DrawerLayout drawerLayout;
    // title 标签
    private Button navButton;
    // 关注按钮
    private Button concern;
    // 取消关注按钮
    private Button concealConcern;
    // 返回按钮
    private Button goBack;
    // 刷新按钮
    private Button refresh;
    // 下拉刷新
    public SwipeRefreshLayout swipeRefresh;
    // ListView滚动
    private ScrollView weatherLayout;
    // 省级
    private TextView provinceText;
    // 市级
    private TextView cityText;
    // 天气情况
    private TextView weatherText;
    // 温度
    private TextView temperatureText;
    // 湿度
    private TextView humidityText;
    // 实时时间
    private TextView reportTimeText;

    @SuppressLint("ResourceAsColor")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if(Build.VERSION.SDK_INT >= 21){
            View decorView = getWindow().getDecorView();
            // 当前布局将显示在Title之上
            decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
            // 将状态栏设置成透明色
            getWindow().setStatusBarColor(Color.TRANSPARENT);
        }

        // 获得活动组件
        setContentView(R.layout.activity_weather);
        weatherLayout = findViewById(R.id.weather_layout);
        swipeRefresh = findViewById(R.id.swipe_refresh);
        provinceText = findViewById(R.id.province_text);
        cityText = findViewById(R.id.city_text);
        weatherText = findViewById(R.id.weather_text);
        temperatureText = findViewById(R.id.temperature_text);
        humidityText = findViewById(R.id.humidity_text);
        reportTimeText = findViewById(R.id.reporttime_text);
        drawerLayout = findViewById(R.id.drawer_layout);
        navButton = findViewById(R.id.nav_button);
        concern = findViewById(R.id.concern);
        concealConcern = findViewById(R.id.concealConcern);
        goBack = findViewById(R.id.goBack);
        refresh = findViewById(R.id.refresh);

        // 获取数据库中的缓存
        SharedPreferences prefs = getSharedPreferences(String.valueOf(this), Context.MODE_PRIVATE);
        String adcodeString = prefs.getString("weather",null);
        final String countyCode;
        final String countyName;

        // 若存在缓存 (即数据库中有数据)
        // 则可以直接进行天气解析
        if (adcodeString != null) {
            Weather weather = Utility.handleWeatherResponse(adcodeString);
            assert weather != null;
            countyCode = weather.adcodeName;
            countyName = weather.cityName;
            // 最后显示天气的具体信息
            showWeatherInfo(weather);
        }
        // 若不存在缓存 (即数据库中没有数据)
        else {
            // 则将需要保存的值设置 Key 值
            // 并调用API进行查找和保存
            countyCode = getIntent().getStringExtra("adcode");
            countyName = getIntent().getStringExtra("city");
            weatherLayout.setVisibility(View.INVISIBLE);
            requestWeather(countyCode);
        }

        // 下拉刷新
        swipeRefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener(){//下拉进度条监听器
            @Override
            public void onRefresh() {
                // 每次刷新时都进行天气的实时更新
                requestWeather(countyCode);
            }
        });

        // title 标签按钮
        navButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                // 开启侧滑菜单 (显示内容为最开始的主页内容)
                drawerLayout.openDrawer(GravityCompat.START);
            }
        });

        // 关注按钮
        concern.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 将当前 县级名称 和 代码 加入数据库中
                MyDBhelper dbHelper = new MyDBhelper(WeatherActivity.this, DB_NAME, null, 1);
                SQLiteDatabase db = dbHelper.getWritableDatabase();
                ContentValues values = new ContentValues();
                values.put("city_code", countyCode);
                values.put("city_name", countyName);
                db.insert(TABLE_NAME, null, values);
                Toast.makeText(WeatherActivity.this, "关注成功！", Toast.LENGTH_LONG).show();
            }
        });

        // 取消关注按钮
        concealConcern.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 将当前 县及名称 和 代码 从数据库中删除
                MyDBhelper dbHelper = new MyDBhelper(WeatherActivity.this, DB_NAME, null, 1);
                SQLiteDatabase db = dbHelper.getWritableDatabase();
                db.delete(TABLE_NAME,"city_code=?",new String[]{String.valueOf(countyCode)});
                Toast.makeText(WeatherActivity.this, "取消关注成功！", Toast.LENGTH_LONG).show();
            }
        });

        // 返回按钮
        goBack.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                // 启动主活动 (即返回主页面)
                Intent intent = new Intent(WeatherActivity.this, MainActivity.class);
                startActivity(intent);
                finish();
            }
        });

        // 刷新按钮 (重新获取天气信息)
        refresh.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                requestWeather(countyCode);
            }
        });
    }

    // 获取天气主要信息
    public void requestWeather(final String adCode) {
        // 调用 天气API 获取 Http请求
        String weatherUrl = "https://restapi.amap.com/v3/weather/weatherInfo?city=" + adCode + "&key=562a75a2243ea6a24389af6f5f954388";
        HttpUtil.sendOkHttpRequest(weatherUrl, new Callback() {
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                final String responseText = Objects.requireNonNull(response.body()).string();
                final Weather weather = Utility.handleWeatherResponse(responseText);

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        // 获取API解析成果
                        if (weather != null) {
                            // 使用 sharePreferences 将获取的天气信息存储
                            SharedPreferences.Editor editor = getSharedPreferences(String.valueOf(this),MODE_PRIVATE).edit();
                            editor.putString("weather", responseText);
                            editor.apply();
                            showWeatherInfo(weather);
                        }
                        else {
                            Toast.makeText(WeatherActivity.this, "获取天气信息失败,城市ID不存在，请重新输入！", Toast.LENGTH_SHORT).show();
                        }
                        // 停止下拉刷新动画
                        swipeRefresh.setRefreshing(false);
                    }
                });
            }

            // 响应失败：
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(WeatherActivity.this, "获取天气信息失败", Toast.LENGTH_SHORT).show();
                        // 停止下拉刷新动画
                        swipeRefresh.setRefreshing(false);
                    }
                });
            }
        });
    }

    // 具体天气显示
    private void showWeatherInfo(Weather weather) {
        provinceText.setText(weather.provinceName);
        cityText.setText(weather.cityName);
        weatherText.setText("天气: " + weather.weatherName);
        temperatureText.setText("温度: " + weather.temperatureName + "℃");
        humidityText.setText("湿度: " + weather.humidityName + "%");
        reportTimeText.setText(weather.reportTimeName);
        weatherLayout.setVisibility(View.VISIBLE);
    }
}