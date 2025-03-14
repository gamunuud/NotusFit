package com.notus.fit.utils;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Looper;
import android.provider.Settings;
import android.util.Log;

import com.afollestad.materialdialogs.MaterialDialog;
import com.notus.fit.R;

/**
 * Project: NotusFit
 * Created by Gamunu Balagalla
 * Last Modified: 9/3/2015 8:15 PM
 */
public class GpsTracker extends Service
        implements LocationListener {

    private static final long MIN_DISTANCE_CHANGE_FOR_UPDATES = 2;
    private static final long MIN_TIME_BW_UPDATES = 60000;
    private final Context mContext;
    protected LocationManager locationManager;
    boolean canGetLocation;
    boolean isGPSEnabled;
    boolean isNetworkEnabled;
    double latitude;
    Location location;
    double longitude;

    public GpsTracker(Context context) {
        isGPSEnabled = false;
        isNetworkEnabled = false;
        canGetLocation = false;
        mContext = context;
        getLocation();
    }

    public boolean canGetLocation() {
        return canGetLocation;
    }

    public double getLatitude() {
        if (location != null) {
            latitude = location.getLatitude();
        }
        return latitude;
    }

    public Location getLocation() {
        try {
            locationManager = (LocationManager) mContext.getSystemService(Context.LOCATION_SERVICE);
            isGPSEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
            ;
            isNetworkEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
            if (isGPSEnabled || isNetworkEnabled) {
                canGetLocation = true;
                if (isNetworkEnabled) {
                    locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, MIN_TIME_BW_UPDATES, MIN_DISTANCE_CHANGE_FOR_UPDATES, this, Looper.getMainLooper());
                    Log.d("Network", "Network");
                    if (locationManager != null) {
                        location = locationManager.getLastKnownLocation("network");
                        if (location != null) {
                            latitude = location.getLatitude();
                            longitude = location.getLongitude();
                        }
                    }
                }
                if (isGPSEnabled && location == null) {
                    locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, MIN_TIME_BW_UPDATES, MIN_DISTANCE_CHANGE_FOR_UPDATES, this, Looper.getMainLooper());
                    Log.d("GPS Enabled", "GPS Enabled");
                    if (locationManager != null) {
                        location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                        if (location != null) {
                            latitude = location.getLatitude();
                            longitude = location.getLongitude();
                        }
                    }
                }
            }
        } catch (Exception exception) {
            exception.printStackTrace();
        }
        return location;
    }

    public double getLongitude() {
        if (location != null) {
            longitude = location.getLongitude();
        }
        return longitude;
    }

    public IBinder onBind(Intent intent) {
        return null;
    }

    public void onLocationChanged(Location location1) {
    }

    public void onProviderDisabled(String s) {
    }

    public void onProviderEnabled(String s) {
    }

    public void onStatusChanged(String s, int i, Bundle bundle) {
    }

    public void showSettingsAlert() {
        new MaterialDialog.Builder(this).title("GPS is settings").content("GPS is not enabled. Do you want to go to settings menu?").positiveText("Settings").positiveColorRes(R.color.primary_dark).negativeText("Cancel").negativeColorRes(R.color.accent_color).callback(new MaterialDialog.ButtonCallback() {

            public void onNegative(MaterialDialog materialdialog) {
                super.onNegative(materialdialog);
                materialdialog.dismiss();
            }

            public void onPositive(MaterialDialog materialdialog) {
                super.onPositive(materialdialog);
                Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                GpsTracker.this.mContext.startActivity(intent);
            }
        }).build().show();
    }

    public void stopUsingGPS() {
        if (locationManager != null) {
            locationManager.removeUpdates(this);
        }
    }

}
