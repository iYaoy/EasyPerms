package com.iyao.sample

import android.Manifest.permission.*
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.iyao.permission.Callback
import com.iyao.permission.Permissions
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        txtClick.setOnClickListener {
            Permissions.with(this)
                .requestCode(100)
                .permissions(CAMERA, WRITE_EXTERNAL_STORAGE, READ_CALENDAR, READ_CALL_LOG, READ_EXTERNAL_STORAGE, READ_SMS, READ_PHONE_STATE)
                .callback(object : Callback() {
                    override fun onPermissionDenied(allDenied: Boolean, vararg deniedPermissions: String) {
                        Log.e("onPermissionDenied", deniedPermissions.contentToString())
                    }

                    override fun onPermissionGranted(allGranted: Boolean, vararg grantedPermissions: String) {
                        Log.e("onPermissionGranted", grantedPermissions.contentToString())
                    }
                }).check()
        }
    }
}
