package com.livinglifetechway.quickpermissionssample

import android.content.Intent
import android.databinding.DataBindingUtil
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import com.livinglifetechway.k4kotlin.onClick
import com.livinglifetechway.quickpermissionssample.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    lateinit var mBinding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mBinding = DataBindingUtil.setContentView(this, R.layout.activity_main)

        mBinding.buttonJava.onClick { startActivity(Intent(this@MainActivity, JavaActivity::class.java)) }
        mBinding.buttonKotlin.onClick { startActivity(Intent(this@MainActivity, KotlinActivity::class.java)) }
    }

}
