package com.example.homework1.activities;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.location.Location;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Looper;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.example.homework1.R;
import com.example.homework1.interfaces.Constants;
import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.SettingsClient;
import com.google.android.gms.tasks.Task;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;

import java.util.HashMap;
import java.util.Map;

public class GameMenuActivity extends AppCompatActivity implements Constants {

    private String trainerName;

    private Location theLocation;
    private MediaPlayer mediaPlayer;

    FusedLocationProviderClient fusedLocationProviderClient;
    LocationRequest locationRequest;

    LocationCallback locationCallback = new LocationCallback() {
        @Override
        public void onLocationResult(@NonNull LocationResult locationResult) {
        }
    };

    @SuppressWarnings("deprecation")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu);

        mediaPlayer = MediaPlayer.create(getApplicationContext(), R.raw.menu);
        mediaPlayer.setLooping(true);
        mediaPlayer.setVolume(100, 100);
        mediaPlayer.start();

        if (getIntent().getExtras().getString("trainer") != null)
            trainerName = getIntent().getExtras().getString("trainer");

        if (getIntent().getExtras().getBoolean("caught"))
        {
            Map<String, Object> updates = new HashMap<>();
            updates.put("trainers/"+trainerName+"/afekaMons", ServerValue.increment(1));
            FirebaseDatabase.getInstance().getReference().updateChildren(updates);
        }

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);

        locationRequest = LocationRequest.create();
        locationRequest.setInterval(4000);
        locationRequest.setFastestInterval(2000);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        MaterialButton menu_BTN_light = findViewById(R.id.menu_BTN_light);
        MaterialButton menu_BTN_topTen = findViewById(R.id.menu_BTN_topTen);

        menu_BTN_light.setOnClickListener(v -> startAR());
        menu_BTN_topTen.setOnClickListener(v -> openTopTenActivity(GameMenuActivity.this));
    }

    private void startAR()
    {
        mediaPlayer.stop();
        mediaPlayer.release();
        Log.d("MENU", "startAR: " + theLocation);
        Intent myIntent = new Intent(this, ArActivity.class);
        startActivity(myIntent);
        finish();
    }

    private void openTopTenActivity(Activity activity) {
        mediaPlayer.stop();
        mediaPlayer.release();
        Intent myIntent = new Intent(activity, TrainerActivity.class);
        myIntent.putExtra("trainer", trainerName);
        startActivity(myIntent);
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            getLastLocation();
            checkSettingsAndStartLocationUpdates();
        } else {
            askLocationPermission();
        }

    }

    @Override
    protected void onResume() {
        super.onResume();
        mediaPlayer.start();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mediaPlayer.pause();
    }

    @Override
    protected void onStop() {
        super.onStop();
        stopLocationUpdates();
        mediaPlayer.stop();
        mediaPlayer.release();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mediaPlayer.stop();
        mediaPlayer.release();
    }

    private void checkSettingsAndStartLocationUpdates() {
        LocationSettingsRequest request = new LocationSettingsRequest.Builder()
                .addLocationRequest(locationRequest).build();
        SettingsClient client = LocationServices.getSettingsClient(this);

        Task<LocationSettingsResponse> locationSettingsResponseTask = client.checkLocationSettings(request);
        locationSettingsResponseTask.addOnSuccessListener(locationSettingsResponse -> {
            //Settings of device are satisfied and we can start location updates
            startLocationUpdates();
        });
        locationSettingsResponseTask.addOnFailureListener(e -> {
            if (e instanceof ResolvableApiException) {
                ResolvableApiException apiException = (ResolvableApiException) e;
                try {
                    apiException.startResolutionForResult(GameMenuActivity.this, 1001);
                } catch (IntentSender.SendIntentException ex) {
                    ex.printStackTrace();
                }
            }
        });

    }


    private void startLocationUpdates() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        fusedLocationProviderClient.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper());
    }

    private void stopLocationUpdates() {
        fusedLocationProviderClient.removeLocationUpdates(locationCallback);
    }

    private void getLastLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        Task<Location> locationTask = fusedLocationProviderClient.getLastLocation();
        locationTask.addOnSuccessListener(location -> {
            if (location != null) {
                //We have a location
                theLocation = location;
            }
        });
    }

    private void askLocationPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_FINE_LOCATION)) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_REQUEST_CODE);
            } else {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_REQUEST_CODE);
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                getLastLocation();
                checkSettingsAndStartLocationUpdates();
            } else {
                showLocationAlert();
            }
        }
    }

    private void showLocationAlert() {
        new AlertDialog.Builder(this)
                .setTitle("Device location")
                .setMessage("Application must use device location.\nPlease allow it.\n")
                .setPositiveButton("Exit", (dialog, which) -> finish())
                .setIcon(android.R.drawable.ic_dialog_alert)
                .show();
    }

}
