package com.riantono.dev.pokejoy;

import android.Manifest;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.PixelFormat;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.os.Bundle;
import android.os.IBinder;
import android.os.SystemClock;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.riantono.dev.pokejoy.utils.LocationUtils;

/**
 * Created by billy on 7/16/16.
 */
public class PokeJoyServices extends Service implements View.OnTouchListener {
    private final String TAG = PokeJoyServices.this.getClass().getSimpleName();
    private WindowManager windowManager;
    private RelativeLayout mViewContainer;
    private ImageView mButtonUp;
    private ImageView mButtonDown;
    private ImageView mButtonLeft;
    private ImageView mButtonRight;
    WindowManager.LayoutParams params;
    private View myView;

    private LocationManager mLocationManager;
    private String mockLocationProvider;

    private Location currentLoc;

    private boolean haveRequestInitialLocation;

    @Override
    public void onCreate() {
        super.onCreate();
        final float scale = getResources().getDisplayMetrics().density;
        int pixels = (int) (200 * scale + 0.5f);
        windowManager = (WindowManager) getSystemService(WINDOW_SERVICE);


        final WindowManager.LayoutParams myParams = new WindowManager.LayoutParams(
                pixels,
                pixels,
                WindowManager.LayoutParams.TYPE_SYSTEM_ALERT,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                        | WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL
                        | WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH,
                PixelFormat.TRANSLUCENT);

        myParams.gravity = Gravity.TOP | Gravity.LEFT;
        myParams.x = 0;
        myParams.y = 100;

        LayoutInflater inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
        myView = inflater.inflate(R.layout.layout_joystick, null);

        mViewContainer = (RelativeLayout) myView.findViewById(R.id.joystick_container);
        mButtonUp = (ImageView) myView.findViewById(R.id.button_up);
        mButtonDown = (ImageView) myView.findViewById(R.id.button_down);
        mButtonLeft = (ImageView) myView.findViewById(R.id.button_left);
        mButtonRight = (ImageView) myView.findViewById(R.id.button_right);
        mButtonUp.setOnTouchListener(this);
        mButtonDown.setOnTouchListener(this);
        mButtonLeft.setOnTouchListener(this);
        mButtonRight.setOnTouchListener(this);
        //this code is for dragging the chat head
        myView.setOnTouchListener(new View.OnTouchListener() {
            private int initialX;
            private int initialY;
            private float initialTouchX;
            private float initialTouchY;

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        initialX = myParams.x;
                        initialY = myParams.y;
                        initialTouchX = event.getRawX();
                        initialTouchY = event.getRawY();
                        return true;
                    case MotionEvent.ACTION_UP:
                        return true;
                    case MotionEvent.ACTION_MOVE:
                        myParams.x = initialX
                                + (int) (event.getRawX() - initialTouchX);
                        myParams.y = initialY
                                + (int) (event.getRawY() - initialTouchY);
                        windowManager.updateViewLayout(myView, myParams);
                        return true;
                }
                return false;
            }
        });
        windowManager.addView(myView, myParams);

        //init locationManager
        initLocationThings();
    }

    private void initLocationThings() {
        try {
            mLocationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

            mockLocationProvider = LocationManager.GPS_PROVIDER;
            currentLoc = new Location(mockLocationProvider);

            mLocationManager.addTestProvider(mockLocationProvider, true, true, true, false, true,
                    true, true, 0, 5);
            mLocationManager.setTestProviderEnabled(mockLocationProvider, true);
            mLocationManager.setTestProviderStatus(mockLocationProvider, LocationProvider.AVAILABLE, null, System.currentTimeMillis());
            if (ContextCompat.checkSelfPermission(this,
                    Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_DENIED) {
                mLocationManager.requestLocationUpdates(mockLocationProvider, 5, 15, locationListener);
            }


        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mLocationManager != null) {
            this.mLocationManager.removeTestProvider(mockLocationProvider);
        }
        if (myView != null)
            windowManager.removeView(myView);
    }

    private final LocationListener locationListener = new LocationListener() {
        @Override
        public void onLocationChanged(Location location) {
            Log.v(TAG, "Location updated: " + location.toString());
            if (!haveRequestInitialLocation && currentLoc != null) {
                //initial Location
                currentLoc.setTime(System.currentTimeMillis());
                currentLoc.setElapsedRealtimeNanos(SystemClock.elapsedRealtimeNanos());
                currentLoc.setAccuracy(50);
                currentLoc.setAltitude(location.getAltitude());
                currentLoc.setLatitude(location.getLatitude());
                currentLoc.setLongitude(location.getLongitude());
                haveRequestInitialLocation = true;
            }
        }

        @Override
        public void onStatusChanged(String s, int i, Bundle bundle) {
        }

        @Override
        public void onProviderEnabled(String s) {
        }

        @Override
        public void onProviderDisabled(String s) {
        }
    };

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }


    private void updateLocation(String lat, String lon, String altitude) {
        currentLoc.setLatitude(Double.parseDouble(lat));
        currentLoc.setLongitude(Double.parseDouble(lon));
        currentLoc.setAltitude(Double.parseDouble(altitude));
        currentLoc.setTime(System.currentTimeMillis());
        currentLoc.setElapsedRealtimeNanos(SystemClock.elapsedRealtimeNanos());
        currentLoc.setAccuracy(50);
        Log.v(TAG, "Update Location : " + lat + " : " + lon + " : " + altitude);
        int value = LocationUtils.setMockLocationSettings(PokeJoyServices.this);//toggle ALLOW_MOCK_LOCATION on
        try {
            mLocationManager.setTestProviderLocation(mockLocationProvider, currentLoc);
        } catch (SecurityException e) {
            e.printStackTrace();
        } finally {
            LocationUtils.restoreMockLocationSettings(PokeJoyServices.this, value);//toggle ALLOW_MOCK_LOCATION off
        }

    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            if (currentLoc.getLatitude() == 0.0) {
                //initial Location
                currentLoc.setTime(System.currentTimeMillis());
                currentLoc.setElapsedRealtimeNanos(SystemClock.elapsedRealtimeNanos());
                currentLoc.setAccuracy(50);
                currentLoc.setAltitude(65.0);
                currentLoc.setLatitude(-6.175);
                currentLoc.setLongitude(106.8272);
            }
            Double randomDistance = (20 + (Math.floor(Math.random() * (20 - 0 + 1) + 0) % 20));
            String movementValue = "0.0000" + String.valueOf(randomDistance.intValue());
            Log.v(TAG, "Movement Value :  " + movementValue);
            switch (v.getId()) {
                case R.id.button_up:
                    Double movementUp = currentLoc.getLatitude() + Double.parseDouble(movementValue);
                    updateLocation(String.valueOf(movementUp), String.valueOf(currentLoc.getLongitude()), String.valueOf(currentLoc.getAltitude()));
                    break;
                case R.id.button_down:
                    Double movementDown = currentLoc.getLatitude() - Double.parseDouble(movementValue);
                    updateLocation(String.valueOf(movementDown), String.valueOf(currentLoc.getLongitude()), String.valueOf(currentLoc.getAltitude()));
                    break;
                case R.id.button_left:
                    Double movementLeft = currentLoc.getLongitude() - Double.parseDouble(movementValue);
                    updateLocation(String.valueOf(currentLoc.getLatitude()), String.valueOf(movementLeft), String.valueOf(currentLoc.getAltitude()));
                    break;
                case R.id.button_right:
                    Double movementRight = currentLoc.getLongitude() + Double.parseDouble(movementValue);
                    updateLocation(String.valueOf(currentLoc.getLatitude()), String.valueOf(movementRight), String.valueOf(currentLoc.getAltitude()));
                    break;
            }
            return true;
        }
        return false;
    }
}
