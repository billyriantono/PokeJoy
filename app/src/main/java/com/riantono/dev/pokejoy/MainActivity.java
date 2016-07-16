package com.riantono.dev.pokejoy;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.PixelFormat;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;

import com.riantono.dev.pokejoy.utils.LocationUtils;

public class MainActivity extends AppCompatActivity {

    private Intent intentService;

    private static final int MY_PERMISSION_ACCESS_COARSE_LOCATION = 11;
    private static final int MY_PERMISSION_ACCESS_FINE_LOCATION = 12;
    private static final int MY_PERMISSION_ACCESS_INSTALL_PROVIDER = 13;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initUI();
    }

    private void initUI() {
        final Button button = (Button) findViewById(R.id.button_start);

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Start Joystick
                startJoyStick();
            }
        });

    }

    private void startJoyStick() {
        if (LocationUtils.isSystemApps(MainActivity.this)) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.INSTALL_LOCATION_PROVIDER) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{
                        Manifest.permission.INSTALL_LOCATION_PROVIDER
                }, MY_PERMISSION_ACCESS_INSTALL_PROVIDER);
            } else {

            }

            if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_SECURE_SETTINGS) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{
                        Manifest.permission.WRITE_SECURE_SETTINGS
                }, MY_PERMISSION_ACCESS_INSTALL_PROVIDER);
            } else {

            }

            if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_SETTINGS) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{
                        Manifest.permission.WRITE_SETTINGS
                }, MY_PERMISSION_ACCESS_INSTALL_PROVIDER);
            } else {

            }
        }


        if (ContextCompat.checkSelfPermission(MainActivity.this,
                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(MainActivity.this,
                    new String[]{
                            Manifest.permission.ACCESS_FINE_LOCATION
                    }, MY_PERMISSION_ACCESS_INSTALL_PROVIDER);
            return;
        }


        if (ContextCompat.checkSelfPermission(this, Manifest.permission.SYSTEM_ALERT_WINDOW) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{
                    Manifest.permission.SYSTEM_ALERT_WINDOW
            }, MY_PERMISSION_ACCESS_INSTALL_PROVIDER);
        } else {
            intentService = new Intent(this, PokeJoyServices.class);
            startService(intentService);
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (Settings.canDrawOverlays(MainActivity.this)) {
                intentService = new Intent(this, PokeJoyServices.class);
                startService(intentService);
            } else {
                Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                        Uri.parse("package:" + getPackageName()));
                startActivityForResult(intent, 1234);
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSION_ACCESS_INSTALL_PROVIDER:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    startJoyStick();
                }
                break;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

    }
}
