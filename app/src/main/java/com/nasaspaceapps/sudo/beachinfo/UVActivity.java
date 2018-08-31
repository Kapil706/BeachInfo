package com.nasaspaceapps.sudo.beachinfo;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.google.gson.Gson;
import com.jakewharton.picasso.OkHttp3Downloader;
import com.squareup.picasso.Picasso;

import java.io.IOException;
import java.util.List;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.GET;
import retrofit2.http.Headers;
import retrofit2.http.Query;

public class UVActivity extends AppCompatActivity {
    private String lat, lon;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_uv);


    Retrofit retrofit = new Retrofit.Builder()
            .baseUrl(OpenUVApi.BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build();

    OpenUVApi openUVApi = retrofit.create(OpenUVApi.class);

    Call<Ultraviolet> call = openUVApi.getUltraviolet(getLat(), getLon());

    call.enqueue(new Callback<Ultraviolet>() {
        @Override
        public void onResponse(Call<Ultraviolet> call, Response<Ultraviolet> response) {
            Result result = new Result();
            SafeExposureTime safeExposureTime = new SafeExposureTime();
            Ultraviolet uv = response.body();
            //GETTER METHODS FOR UV
            double currentuvData = uv.getResult().getUv();
            String currentUVTimeData = uv.getResult().getUvTime();
            double maxUVData = uv.getResult().getUvMax();
            String maxUVTimeData = uv.getResult().getUvMaxTime();
            double ozoneData = uv.getResult().getOzone();

            //Getter Method For SafeExposure
            SafeExposureTime safeExposureData = uv.getResult().getSafeExposureTime();
            Integer st1data =safeExposureData.getSt1();
            Integer st2data =safeExposureData.getSt2();
            Integer st3data =safeExposureData.getSt3();
            Integer st4data =safeExposureData.getSt4();
            Integer st5data =safeExposureData.getSt5();
            Integer st6data =safeExposureData.getSt6();

            //Getter Methods For SunInfo
            SunInfo sunInfo = uv.getResult().getSunInfo();
            SunPosition sunPosition = sunInfo.getSunPosition();
            double sunPositionAltitude =sunPosition.getAltitude();
            double sunPositionAzimuth = sunPosition.getAzimuth();

            //Setter Method For SafeExposureTime
            safeExposureData.setSt1(st1data);
            safeExposureData.setSt2(st2data);
            safeExposureData.setSt3(st3data);
            safeExposureData.setSt4(st4data);
            safeExposureData.setSt5(st5data);
            safeExposureData.setSt6(st6data);

            //Setter Methods for SunInfo
            sunPosition.setAltitude(sunPositionAltitude);
            sunPosition.setAzimuth(sunPositionAzimuth);

            //Setter MEthods For UV
            uv.setResult(result);
            result.setUv(currentuvData);
            result.setUvTime(currentUVTimeData);
            result.setUvMax(maxUVData);
            result.setUvMaxTime(maxUVTimeData);
            result.setOzone(ozoneData);
            //result.setSafeExposureTime(safeExposureTimeData);

            //Setter Methods For SafeExposure
            //safeExposureTime.setSt1(result.setSafeExposureTime());

            Log.e(" mainAction", "  UV - "+ result.getUv());
            //txt1.setText(response.body().getRom().toString());
            Log.e(" mainAction", "  UV Time - "+ result.getUvTime());
            //txt2.setText(response.body().getScreenSize().toString());
            Log.e(" mainAction", "  UV Max - "+ result.getUvMax());
            //txt3.setText(response.body().getBackCamera().toString());
            Log.e("mainAction", "UV Max Time - "+ result.getUvMaxTime());

            Log.e("mainAction", "Ozone Exposure"+ result.getOzone());

            Log.e("mainAction", "Safe Exposure Time 1:- "+ safeExposureData.getSt1());

            Log.e("mainAction", "Safe Exposure Time 2:- "+ safeExposureData.getSt2());

            Log.e("mainAction", "Safe Exposure Time 3:- "+ safeExposureData.getSt3());

            Log.e("mainAction", "Safe Exposure Time 4:- "+ safeExposureData.getSt4());

            Log.e("mainAction", "Safe Exposure Time 5:- "+ safeExposureData.getSt5());

            Log.e("mainAction", "Safe Exposure Time 6:- "+ safeExposureData.getSt6());

            Log.e("mainAction", "Sun Altitude:- " + sunPosition.getAltitude());

            Log.e("mainAction", "Sun Azimuth:- " + sunPosition.getAzimuth());


        }

        @Override
        public void onFailure(Call<Ultraviolet> call, Throwable t) {
            Toast.makeText(getApplicationContext(), t.getMessage(), Toast.LENGTH_LONG).show();
        }
    });


    }

    public String getLat(){
        SharedPreferences sharePref = getSharedPreferences("locationInfo", Context.MODE_PRIVATE);
        lat = sharePref.getString("latitude", "ERROR!!");
        return lat;
        //lon = sharePref.getString("longitude", "ERROR!!");
    }
    public String getLon(){
        SharedPreferences sharePref = getSharedPreferences("locationInfo", Context.MODE_PRIVATE);
        lon = sharePref.getString("longitude", "ERROR!!");
        return lon;
        //lon = sharePref.getString("longitude", "ERROR!!");
    }



    //Open UV API

    public interface OpenUVApi {

        String BASE_URL = "https://api.openuv.io/api/v1/";


        @Headers("x-access-token: 7d0f4babcad636158de5c9d830d3b632")
        @GET("uv?")
        Call<Ultraviolet> getUltraviolet(@Query("lat") String lat, @Query("lon") String lon);
    }




    }




