package com.nasaspaceapps.sudo.beachinfo;

import android.content.Context;
import android.content.SharedPreferences;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

public class MyLocationListener implements LocationListener {
    private Context context;
    private String longitude;
    private String latitude;

    public MyLocationListener(Context context) {
        this.context = context;
    }

    @Override
    public void onProviderDisabled(String provider) {
    }

    @Override
    public void onProviderEnabled(String provider) {
    }

    @Override
    public void onLocationChanged(Location loc) {

        //editLocation.setText("");
        //pb.setVisibility(View.INVISIBLE);
        Toast.makeText(context,
                "Location changed: Lat: " + loc.getLatitude() + " Lng: "
                        + loc.getLongitude(), Toast.LENGTH_SHORT).show();
        longitude = "Longitude: " + loc.getLongitude();
        Log.v("Longitude", longitude);
        latitude = "Latitude: " + loc.getLatitude();
        Log.v("Latitude", latitude);

        /*------- To get city name from coordinates -------- */
        String cityName = null;
        Geocoder gcd = new Geocoder(context, Locale.getDefault());
        List<Address> addresses;
        try {
            addresses = gcd.getFromLocation(loc.getLatitude(),
                    loc.getLongitude(), 1);
            if (addresses.size() > 0) {
                System.out.println(addresses.get(0).getLocality());
                cityName = addresses.get(0).getLocality();
            }
        }
        catch (IOException e) {
            e.printStackTrace();
        }
        String s = longitude + "\n" + latitude + "\n\nMy Current City is: "
                + cityName;
      //  editLocation.setText(s)
                Log.e("Location", " "+ s);

                geoLocation location = new geoLocation();
                location.setMlatitude(latitude);
                location.setMlongitude(longitude);

        Log.e("DATA", "MyLocation Listner The Data I fetched" + location.getMlongitude());
        Log.e("DATA", "MyLocation Listner The Data I fetched" + location.getMlatitude());

        MainActivity mainActivity = new MainActivity();
        mainActivity.setMainlatitude(latitude);
        mainActivity.setMainlogitude(longitude);
        mainActivity.setmCity(cityName);


    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
    }

    public String getLongitude() {
        return longitude;
    }

    public void setLongitude(String longitude) {
        this.longitude = longitude;
    }

    public String getLatitude() {
        return latitude;
    }

    public void setLatitude(String latitude) {
        this.latitude = latitude;
    }
}
