package com.hplasplas.task7;

import android.app.Application;
import android.content.Context;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.hplasplas.task7.interfaces.OpenWeatherMapApi;
import com.jakewharton.picasso.OkHttp3Downloader;
import com.squareup.picasso.Picasso;

import retrofit2.Retrofit;
import retrofit2.converter.scalars.ScalarsConverterFactory;

import static com.hplasplas.task7.setting.Constants.BASE_URL;

/**
 * Created by StarkinDG on 06.04.2017.
 */

public class App extends Application {
    
    private static App instance;
    private static OpenWeatherMapApi sOpenWeatherMapApi;
    private static Gson sGson;
    private static Picasso sPicasso;
    
    @Override
    public void onCreate() {
        
        super.onCreate();
        instance = this;
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(ScalarsConverterFactory.create())
                .build();
        sOpenWeatherMapApi = retrofit.create(OpenWeatherMapApi.class);
        GsonBuilder builder = new GsonBuilder();
        sGson = builder.create();
        sPicasso = new Picasso.Builder(this)
                .downloader(new OkHttp3Downloader(this, Integer.MAX_VALUE))
                .build();
    }
    
    private static synchronized App getInstance() {
        
        return instance;
    }
    
    public static synchronized Context getAppContext() {
        
        return getInstance().getApplicationContext();
    }
    
    public static OpenWeatherMapApi getOpenWeatherMapApi() {
        
        return sOpenWeatherMapApi;
    }
    
    public static Gson getGson() {
        
        return sGson;
    }
    
    public static Picasso getPicasso() {
        
        return sPicasso;
    }
}