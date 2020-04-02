/*
 * Copyright 2017 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.example.android.walkmyandroid;

import android.Manifest;
import android.animation.AnimatorInflater;
import android.animation.AnimatorSet;
import android.content.pm.PackageManager;
import android.location.Location;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;

public class MainActivity extends AppCompatActivity implements FetchAddressTask.OnTaskCompleted{

    private Button mLocationButton;
    private TextView mLocationTextView;
    private static  final int REQUEST_LOCATION_PERMISSION=1;
    private static final String TRACKING_LOCATION_KEY = "tracking_location";
    private FusedLocationProviderClient mFusedLocationClient;
    private ImageView mAndroidImageView;
    private AnimatorSet mRotateAnim;
    private  boolean mTrackingLocation;
    private LocationCallback mLocationCallback;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mLocationButton=findViewById(R.id.button_location);
        mLocationTextView=findViewById(R.id.textview_location);
        mAndroidImageView= (ImageView) findViewById(R.id.imageview_android);
        mRotateAnim = (AnimatorSet) AnimatorInflater.loadAnimator(this,R.animator.rotate);
        mRotateAnim.setTarget(mAndroidImageView);
        mFusedLocationClient= LocationServices.getFusedLocationProviderClient(this);

        if (savedInstanceState != null) {
            mTrackingLocation = savedInstanceState.getBoolean(
                    TRACKING_LOCATION_KEY);
        }

        mLocationButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if	(!mTrackingLocation){
                    startTrackingLocation();
                }
                else{
                    stopTrackingLocation();
                }
            }
        });
        mLocationCallback = new LocationCallback(){
            //jika aktif geocode aktif akan mentracking address
            @Override
            public  void onLocationResult(LocationResult locationResult){
                new FetchAddressTask(MainActivity.this,MainActivity.this)
                        .execute(locationResult.getLastLocation());
            }
        };




        }

    private void startTrackingLocation() {//sebelumnya getLocation()
        if(ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION)!= PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this, new String[]
                    {Manifest.permission.ACCESS_FINE_LOCATION},REQUEST_LOCATION_PERMISSION);
    }else{
//            mFusedLocationClient.getLastLocation().addOnSuccessListener(new OnSuccessListener<Location>() {
//                @Override
//                public void onSuccess(Location location) {
//                    if(location!=null){
//                        new FetchAddressTask(MainActivity.this,MainActivity.this).execute(location);
//                    }
//                        /*mLocationTextView.setText(
//                                "Latitude : "+location.getLatitude()+
//                                        "\nLongitude : "+location.getLongitude()+
//                                        "\nTimeStamp : "+location.getTime()
//                        );
//
//                    }*/
//
//                }
//            });
            mTrackingLocation=true;
            mFusedLocationClient.requestLocationUpdates
                    (getLocationRequest(),
                            mLocationCallback,
                            null /* Looper */);
            mRotateAnim.start();
            mLocationTextView.setText(getString(R.string.address_text,
                    getString(R.string.loading),
                    System.currentTimeMillis()));
            mLocationButton.setText(R.string.stop_tracking_location);
        }
        }

        private void  stopTrackingLocation(){
            if(mTrackingLocation){
                mTrackingLocation=false;
                mLocationButton.setText(R.string.start_tracking_location);
                mLocationTextView.setText(R.string.textview_hint);
                //mFusedLocationClient.removeLocationUpdates(mLocationCallback);
                mRotateAnim.end();
            }
        }

    private LocationRequest getLocationRequest(){
        LocationRequest locationRequest =new LocationRequest();
        locationRequest.setInterval(10000);
        locationRequest.setFastestInterval(5000);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        return locationRequest;
    }

    public  void onRequestPermissionResult(int requestCode,
                                           @NonNull String[] permission,
                                           @NonNull int[] grantResult){
        switch (requestCode){
            case REQUEST_LOCATION_PERMISSION:
                if(grantResult.length>0&&grantResult[0]==PackageManager.PERMISSION_GRANTED){
                    startTrackingLocation();
                }else{
                    Toast.makeText(this, "Permission Denied!",
                            Toast.LENGTH_SHORT).show();
                }
                break;
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState){
        outState.putBoolean(TRACKING_LOCATION_KEY,mTrackingLocation);
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onTaskCompleted(String result) {
        if(mTrackingLocation){
        mLocationTextView.setText(getString
                (R.string.address_text,result,System.currentTimeMillis()));
        }
    }

    @Override
    protected void onPause() {
        if (mTrackingLocation) {
            stopTrackingLocation();
            mTrackingLocation = true;
        }
        super.onPause();
    }

    @Override
    protected void onResume() {
        if (mTrackingLocation) {
            startTrackingLocation();
        }
        super.onResume();
    }

}
