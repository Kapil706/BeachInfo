package com.nasaspaceapps.sudo.beachinfo;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Handler;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.nasaspaceapps.sudo.beachinfo.rest.api.ApiClient;
import com.nasaspaceapps.sudo.beachinfo.rest.api.ApiInterface;
import com.nasaspaceapps.sudo.beachinfo.rest.model.Result;
import com.nasaspaceapps.sudo.beachinfo.rest.model.SafeExposureTime;
import com.nasaspaceapps.sudo.beachinfo.rest.model.SunInfo;
import com.nasaspaceapps.sudo.beachinfo.rest.model.SunPosition;
import com.nasaspaceapps.sudo.beachinfo.rest.model.Ultraviolet;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class UVActivity extends AppCompatActivity {

    private final int MY_PERMISSIONS = 125;
    TextView UV;



    //@BindView(R.id.uvMax)
    TextView uvMax;

    //@BindView(R.id.maxUvTime)
    TextView maxUvTime;

    //@BindView(R.id.ozoneExpose)
    TextView ozoneExpose;

    //@BindView(R.id.sunAzimuth)
    TextView sunAzimuth;

    //@BindView(R.id.sunAltitude)
    TextView sunAlt;


    TextView safeExpose1;


    TextView safeExpose2;


    TextView safeExpose3;


    TextView safeExpose4;


    TextView safeExpose5;


    TextView safeExpose6;

    View UvView;

    ProgressBar progressBarUV;
    TextView UVProgressText;

    String cityName;

    double longitude;
    double latitude;





    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_uv);

        final LocationManager manager = (LocationManager) getSystemService( Context.LOCATION_SERVICE );

        if ( !manager.isProviderEnabled( LocationManager.GPS_PROVIDER ) ) {
            buildAlertMessageNoGps();
        }




        MyLocation.LocationResult locationResult = new MyLocation.LocationResult(){
            @Override
            public void gotLocation(Location location){
               longitude =  location.getLongitude();
               latitude = location.getLatitude();


            }
        };
        MyLocation myLocation = new MyLocation();
        myLocation.getLocation(this, locationResult);










        UV = findViewById(R.id.UV);
        uvMax = findViewById(R.id.uvMax);
        maxUvTime = findViewById(R.id.maxUvTime);
        ozoneExpose = findViewById(R.id.ozoneExpose);
        sunAzimuth = findViewById(R.id.sunAzimuth);
        sunAlt = findViewById(R.id.sunAltitude);
        //SafeExposures
        safeExpose1 = findViewById(R.id.safeExpose1);
        safeExpose2 = findViewById(R.id.safeExpose2);
        safeExpose3 = findViewById(R.id.safeExpose3);
        safeExpose4 = findViewById(R.id.safeExpose4);
        safeExpose5 = findViewById(R.id.safeExpose5);
        safeExpose6 = findViewById(R.id.safeExpose6);
        //VIEWS MANAGING

        UvView = findViewById(R.id.UVView);
        UvView.setVisibility(View.INVISIBLE);
        UVProgressText = findViewById(R.id.UVProgressText);
        progressBarUV = findViewById(R.id.UVProgress);




        //ApiInterface openUVApi = ApiClient.getClient().create(ApiInterface.class);

        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {





                progressBarUV.setVisibility(View.GONE);
                UVProgressText.setVisibility(View.GONE);

                UvView.setVisibility(View.VISIBLE);

                cityName = null;
                Geocoder gcd = new Geocoder(UVActivity.this, Locale.getDefault());
                List<Address> addresses;
                try {
                    addresses = gcd.getFromLocation(latitude,
                            longitude, 1);
                    if (addresses.size() > 0) {
                        System.out.println(addresses.get(0).getLocality());
                        cityName = addresses.get(0).getLocality();
                    }
                }
                catch (IOException e) {
                    e.printStackTrace();
                }

                if(cityName == null){
                    AlertDialog.Builder builder = new AlertDialog.Builder(UVActivity.this);
                    builder.setMessage("We are unable to detect your city, if your device is still searching for GPS consider restarting the app")
                            .setPositiveButton("Restart", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                  UVActivity.this.finish();
                                }
                            })
                            .setNegativeButton("I am not in City", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.cancel();
                                }
                            });

                }

                ApiInterface openUVApi = ApiClient.getClient().create(ApiInterface.class);

                Log.e("MESSAGE", "WE are Getting "+ latitude+ " Longitude as" + longitude);
                Call<Ultraviolet> call = openUVApi.getUvData("7d0f4babcad636158de5c9d830d3b632", "" + latitude, "" + longitude);

                call.enqueue(new Callback<Ultraviolet>() {
                    @Override
                    public void onResponse(Call<Ultraviolet> call, Response<Ultraviolet> response) {
                        Log.d("response", response.toString());
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
                        Integer st1data = safeExposureData.getSt1();
                        Integer st2data = safeExposureData.getSt2();
                        Integer st3data = safeExposureData.getSt3();
                        Integer st4data = safeExposureData.getSt4();
                        Integer st5data = safeExposureData.getSt5();
                        Integer st6data = safeExposureData.getSt6();

                        //Getter Methods For SunInfo
                        SunInfo sunInfo = uv.getResult().getSunInfo();
                        SunPosition sunPosition = sunInfo.getSunPosition();
                        double sunPositionAltitude = sunPosition.getAltitude();
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

                        Log.e(" mainAction", "  UV - " + result.getUv());
                        UV.setText(""+result.getUv());
                        Log.e(" mainAction", "  UV Time - " + result.getUvTime());

                        Log.e(" mainAction", "  UV Max - " + result.getUvMax());
                        uvMax.setText(""+ result.getUvMax());
                        Log.e("mainAction", "UV Max Time - " + result.getUvMaxTime());
                        maxUvTime.setText(""+ result.getUvMaxTime());
                        Log.e("mainAction", "Ozone Exposure" + result.getOzone());
                        ozoneExpose.setText(""+ result.getOzone());
                        Log.e("mainAction", "Safe Exposure Time 1:- " + safeExposureData.getSt1());
                        if(safeExposureData.getSt1() == null){
                            safeExpose1.setText("No Data For "+ cityName);
                        }
                        else{
                            safeExpose1.setText(""+safeExposureData.getSt1());
                        }

                        Log.e("mainAction", "Safe Exposure Time 2:- " + safeExposureData.getSt2());

                        if(safeExposureData.getSt2() == null){
                            safeExpose2.setText("No Data For "+ cityName);
                        }
                        else{
                            safeExpose2.setText(""+safeExposureData.getSt2());
                        }

                        Log.e("mainAction", "Safe Exposure Time 3:- " + safeExposureData.getSt3());

                        if(safeExposureData.getSt3() == null){
                            safeExpose3.setText("No Data For "+ cityName);
                        }
                        else{
                            safeExpose3.setText(""+safeExposureData.getSt3());
                        }

                        Log.e("mainAction", "Safe Exposure Time 4:- " + safeExposureData.getSt4());

                        if(safeExposureData.getSt4() == null){
                            safeExpose4.setText("No Data For "+ cityName);
                        }
                        else{
                            safeExpose4.setText(""+safeExposureData.getSt4());
                        }

                        Log.e("mainAction", "Safe Exposure Time 5:- " + safeExposureData.getSt5());

                        if(safeExposureData.getSt5() == null){
                            safeExpose5.setText("No Data For "+cityName);
                        }
                        else{
                            safeExpose5.setText(""+safeExposureData.getSt5());
                        }

                        Log.e("mainAction", "Safe Exposure Time 6:- " + safeExposureData.getSt6());

                        if(safeExposureData.getSt6() == null){
                            safeExpose6.setText("No Data For "+ cityName);
                        }
                        else{
                            safeExpose6.setText(""+safeExposureData.getSt6());
                        }

                        Log.e("mainAction", "Sun Altitude:- " + sunPosition.getAltitude());
                        sunAlt.setText(""+sunPosition.getAltitude());
                        Log.e("mainAction", "Sun Azimuth:- " + sunPosition.getAzimuth());
                        sunAzimuth.setText(""+ sunPosition.getAzimuth());


                    }

                    @Override
                    public void onFailure(Call<Ultraviolet> call, Throwable t) {
                        UV.setText("Connection Error");
                        uvMax.setText("Connection Error");
                       // Log.e("mainAction", "UV Max Time - " + result.getUvMaxTime());
                        maxUvTime.setText("Connection Error");
                        //Log.e("mainAction", "Ozone Exposure" + result.getOzone());
                        ozoneExpose.setText("Connection Error");
                       // Log.e("mainAction", "Safe Exposure Time 1:- " + safeExposureData.getSt1());

                            safeExpose1.setText("Connection Error");


                        //Log.e("mainAction", "Safe Exposure Time 2:- " + safeExposureData.getSt2());

                       // if(safeExposureData.getSt2() == null){
                         //   safeExpose2.setText("No Data For "+ cityName);
                        //}
                        //else{
                            safeExpose2.setText("Connection Error");
                       // }

                       // Log.e("mainAction", "Safe Exposure Time 3:- " + safeExposureData.getSt3());

                       // if(safeExposureData.getSt3() == null){
                         //   safeExpose3.setText("No Data For "+ cityName);
                        //}
                       // else{
                            safeExpose3.setText("Connection Error");
                        //}

                       // Log.e("mainAction", "Safe Exposure Time 4:- " + safeExposureData.getSt4());

                       // if(safeExposureData.getSt4() == null){
                       //     safeExpose4.setText("No Data For "+ cityName);
                       // }
                       // else{
                            safeExpose4.setText("Connection Error");
                       // }

                       // Log.e("mainAction", "Safe Exposure Time 5:- " + safeExposureData.getSt5());

                       // if(safeExposureData.getSt5() == null){
                            safeExpose5.setText("Connection Error");
                        //}
                        //else{
                        //    safeExpose5.setText(""+safeExposureData.getSt5());
                        //}

//                        Log.e("mainAction", "Safe Exposure Time 6:- " + safeExposureData.getSt6());

  //                      if(safeExposureData.getSt6() == null){
    //                        safeExpose6.setText("No Data For "+ cityName);
      //                  }
        //                else{
                            safeExpose6.setText("Connection Error");
          //              }

//                        Log.e("mainAction", "Sun Altitude:- " + sunPosition.getAltitude());
                        sunAlt.setText("Connection Error");
  //                      Log.e("mainAction", "Sun Azimuth:- " + sunPosition.getAzimuth());
                        sunAzimuth.setText("Connection Error");



                    }
                });

            }
        }, 6000);



    }

    private void buildAlertMessageNoGps() {
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Your GPS seems to be disabled, the result can be inaccurate, do you want to enable it?")
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    public void onClick(@SuppressWarnings("unused") final DialogInterface dialog, @SuppressWarnings("unused") final int id) {
                        startActivity(new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS));
                    }
                })
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    public void onClick(final DialogInterface dialog, @SuppressWarnings("unused") final int id) {
                        dialog.cancel();
                    }
                });
        final AlertDialog alert = builder.create();
        alert.show();
    }
}




