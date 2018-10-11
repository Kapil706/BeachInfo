package com.nasaspaceapps.sudo.beachinfo;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import com.bumptech.glide.Glide;

public class MainActivity extends AppCompatActivity {
    private final int MY_PERMISSIONS = 124;

    Intent phoneIntent = new Intent(Intent.ACTION_CALL);
    LocationManager locationManager;

    private String mainlatitude;
    private String mainlogitude;
    private String mCity;
    GPSTracker gps;
    private ImageView imageView;




    //----------------------DRAWER----------------------------------------
    private DrawerLayout mDrawerLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        imageView = findViewById(R.id.gifImage);

        //GPS Location Location Manager
        locationManager = (LocationManager)
                getSystemService(Context.LOCATION_SERVICE);

        beginPermission();

        gps = new GPSTracker(MainActivity.this);

        // Check if GPS enabled
        if(gps.canGetLocation()) {

            double latitude = gps.getLatitude();
            double longitude = gps.getLongitude();

            // \n is for new line
            //Toast.makeText(getApplicationContext(), "Your Location is - \nLat: " + latitude + "\nLong: " + longitude, Toast.LENGTH_LONG).show();

            Log.e("Data", "MainActivity: YESSSSSSS"+ String.valueOf(latitude) +" "+ String.valueOf(longitude));
            saveData();


        } else {
            // Can't get location.
            // GPS or network is not enabled.
            // Ask user to enable GPS/network in settings.
            gps.showSettingsAlert();
        }

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        ActionBar actionbar = getSupportActionBar();
        actionbar.setDisplayHomeAsUpEnabled(true);
        actionbar.setHomeAsUpIndicator(R.drawable.ic_menu);

        Log.e("DATA", "Main Activity" + getMainlatitude());
        Log.e("DATA", "Main Activity" + getMainlogitude());
        Log.e("DATA", "Main Activity" + getmCity());




        mDrawerLayout = findViewById(R.id.drawer_layout);

        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(
                new NavigationView.OnNavigationItemSelectedListener() {
                    @Override
                    public boolean onNavigationItemSelected(MenuItem menuItem) {
                        // set item as selected to persist highlight
                        menuItem.setChecked(true);
                        // close drawer when item is tapped
                        mDrawerLayout.closeDrawers();

                        // Add code here to update the UI based on the item selected
                        // For example, swap UI fragments here
                        switch (menuItem.getItemId()) {
                            case R.id.nav_UV:
                                Bundle bundle = new Bundle();
                                bundle.putString("latitude", String.valueOf(gps.getLatitude()));
                                bundle.putString("longitude", String.valueOf(gps.getLongitude()));
                                Intent i = new Intent(MainActivity.this, UVActivity.class);
                                startActivity(i);
                                break;

                            case R.id.nav_HAB:
                                Intent intent =new Intent(MainActivity.this, HABactivity.class);
                                startActivity(intent);
                                break;

                            case R.id.nav_skin:
                                Intent skin = new Intent(MainActivity.this, SkinTypeActivity.class);
                                startActivity(skin);



                        }

                        return true;
                    }
                });
        Glide.with(this).load(R.drawable.agro).into(imageView);
    }

    @SuppressLint("MissingPermission")
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                mDrawerLayout.openDrawer(GravityCompat.START);
                return true;
            case R.id.call_an_ambulance:
                phoneIntent.setData(Uri.parse("tel:102"));
                startActivity(phoneIntent);
                return true;
            case R.id.call_the_firefighters:
                phoneIntent.setData(Uri.parse("tel:101"));
                startActivity(phoneIntent);
                return true;
            case R.id.call_the_police:
                phoneIntent.setData(Uri.parse("tel:100"));
                startActivity(phoneIntent);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    //-------------------------------Side Menu----------------------------------


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }


    //-----------------------------------Permissions---------------------------

    public void beginPermission() {
        if ((ActivityCompat.checkSelfPermission(this,
                Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) && (ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) && (ActivityCompat.checkSelfPermission(this,
                Manifest.permission.CALL_PHONE)
                != PackageManager.PERMISSION_GRANTED)) {

            Log.i("1", "Permission is not granted");

            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.CAMERA) && (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.ACCESS_FINE_LOCATION)) && (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.CALL_PHONE))) {
                Log.i("REQUEST", "Requesting permission....");
                ActivityCompat.requestPermissions(MainActivity.this,
                        new String[]{Manifest.permission.CAMERA, Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.CALL_PHONE },
                        MY_PERMISSIONS);


            } else {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.CAMERA, Manifest.permission.ACCESS_FINE_LOCATION,Manifest.permission.CALL_PHONE},
                        MY_PERMISSIONS);

            }
        } else {
            Toast.makeText(MainActivity.this, "THANKS", Toast.LENGTH_LONG);
            //Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            LocationListener locationListener = new MyLocationListener(this);
            locationManager.requestLocationUpdates(
                    LocationManager.GPS_PROVIDER, 5000, 10, locationListener);




        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS: {

                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    Log.i("1", "Permission is granted");
                    //Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                    LocationListener locationListener = new MyLocationListener(this);
                    if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                        // TODO: Consider calling
                        //    ActivityCompat#requestPermissions
                        // here to request the missing permissions, and then overriding
                        //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                        //                                          int[] grantResults)
                        // to handle the case where the user grants the permission. See the documentation
                        // for ActivityCompat#requestPermissions for more details.


                    }
                    locationManager.requestLocationUpdates(
                            LocationManager.GPS_PROVIDER, 5000, 10, locationListener);

                } else {
                    Log.i("1", "Permission is again not granted");
                    Snackbar mySnackbar = Snackbar.make(findViewById(android.R.id.content),
                            "Please ennable the permissions", Snackbar.LENGTH_SHORT);
                    mySnackbar.setAction("ENABLE", new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            startActivity(new Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.parse("package:" + BuildConfig.APPLICATION_ID)));

                        }
                    });
                    mySnackbar.show();

                }
                return;
            }
        }
    }

    //------------------------------------END PERMISSIONS---------------------------------------------------------------------------

    //----------------------_Saving Data to Shared Prefrences------------------------------------

    public void saveData(){
        SharedPreferences sharePref = getSharedPreferences("locationInfo", Context.MODE_PRIVATE);
        SharedPreferences.Editor shareEdit = sharePref.edit();
        shareEdit.putString("latitude", String.valueOf(gps.getLatitude()));
        shareEdit.putString("longitude", String.valueOf(gps.getLongitude()));
        shareEdit.commit();
    }


    public String getMainlatitude() {
        return mainlatitude;
    }

    public void setMainlatitude(String mainlatitude) {
        this.mainlatitude = mainlatitude;
    }

    public String getMainlogitude() {
        return mainlogitude;
    }

    public void setMainlogitude(String mainlogitude) {
        this.mainlogitude = mainlogitude;
    }

    public String getmCity() {
        return mCity;
    }

    public void setmCity(String mCity) {
        this.mCity = mCity;
    }



}

