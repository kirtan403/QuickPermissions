package com.livinglifetechway.quickpermissionssample;

import android.Manifest;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.widget.Toast;

import com.livinglifetechway.quickpermissions.annotations.OnPermissionsDenied;
import com.livinglifetechway.quickpermissions.annotations.OnPermissionsPermanentlyDenied;
import com.livinglifetechway.quickpermissions.annotations.RequiresPermissions;
import com.livinglifetechway.quickpermissions.util.QuickPermissionsRequest;

public class JavaActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_java);

        testMethod();
    }

    @RequiresPermissions(
            permissions = {Manifest.permission.CAMERA, Manifest.permission.ACCESS_FINE_LOCATION},
            rationaleMessage = "Custom rational message"
    )
    public void testMethod() {
        Toast.makeText(this, "I do have the camera permission", Toast.LENGTH_SHORT).show();
    }

    //
    //    @OnShowRationale
    //    public void onShowRational(final QuickPermissionsRequest arg) {
    //        new AlertDialog.Builder(this)
    //                .setTitle("Rationale!!")
    //                .setMessage("rational dialog shown")
    //                .setPositiveButton("TRY NOW", new DialogInterface.OnClickListener() {
    //                    @Override
    //                    public void onClick(DialogInterface dialogInterface, int i) {
    //                        arg.proceed();
    //                    }
    //                })
    //                .setNegativeButton("close", new DialogInterface.OnClickListener() {
    //                    @Override
    //                    public void onClick(DialogInterface dialogInterface, int i) {
    //                        arg.cancel();
    //                    }
    //                })
    //                .setNeutralButton(arg.getRationaleMessage(), null)
    //                .show();
    //
    //    }
    @OnPermissionsPermanentlyDenied
    public void onPerDenied(final QuickPermissionsRequest arg) {
        new AlertDialog.Builder(this)
                .setTitle("Per Denied!!")
                .setMessage("per denied dialog shown")
                .setPositiveButton("SET", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        arg.openAppSettings();
                    }
                })
                .setNegativeButton("close", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        arg.cancel();
                    }
                })
                .setNeutralButton(arg.getRationaleMessage(), null)
                .show();

    }

    @OnPermissionsDenied
    public void onDenied(QuickPermissionsRequest arg) {
        Toast.makeText(this, "permissions denied!! " + arg.getDeniedPermissions().length, Toast.LENGTH_SHORT).show();
    }
}
