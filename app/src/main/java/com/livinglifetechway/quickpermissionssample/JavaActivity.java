package com.livinglifetechway.quickpermissionssample;

import android.Manifest;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.Toast;

import com.livinglifetechway.quickpermissions.annotations.RequiresPermissions;

public class JavaActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_java);

        testMethod();
    }

    @RequiresPermissions(permissions = {Manifest.permission.CAMERA, Manifest.permission.ACCESS_FINE_LOCATION})
    public void testMethod() {
        Toast.makeText(this, "I do have the camera permission", Toast.LENGTH_SHORT).show();
    }
}
