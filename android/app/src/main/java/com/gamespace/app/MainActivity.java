package com.gamespace.app;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;

import com.gamespace.app.utils.ShizukuExecutor;
import com.gamespace.app.utils.ShellExecutor;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Auto-grant permissions if Shizuku binder is active
        if (ShizukuExecutor.hasShizukuPermission()) {
            ShizukuExecutor.grantAppPermissionsViaShizuku(this);
        }
    }
}
