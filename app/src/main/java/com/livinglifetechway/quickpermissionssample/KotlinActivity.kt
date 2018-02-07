package com.livinglifetechway.quickpermissionssample

import android.Manifest
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.widget.Toast
import com.livinglifetechway.quickpermissions.annotations.RequiresPermissions

class KotlinActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_kotlin)

        testMethod()
    }


    @RequiresPermissions(permissions = [Manifest.permission.CAMERA, Manifest.permission.ACCESS_FINE_LOCATION])
    fun testMethod(): Unit {
        Toast.makeText(this, "I do have the camera permission", Toast.LENGTH_SHORT).show()
    }

}
