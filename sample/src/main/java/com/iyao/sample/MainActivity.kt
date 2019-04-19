package com.iyao.sample

import android.Manifest.permission.*
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.iyao.permission.Callback
import com.iyao.permission.EasyPerms
import kotlinx.android.synthetic.main.activity_main.*


class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        EasyPerms.addInterceptor(GlobalNotAskPermsPermsInterceptor)
        EasyPerms.addInterceptor(GlobalRationalePermsInterceptor)
        EasyPerms.addInterceptor(GlobalCheckPermsInterceptor)
        txtClick.setOnClickListener {
            EasyPerms.with(this)
                .builder()
                .requestCode(100)
                .permissions(CAMERA, WRITE_EXTERNAL_STORAGE, READ_CALENDAR, READ_CALL_LOG, READ_EXTERNAL_STORAGE, READ_SMS, READ_PHONE_STATE)
                .check(object: Callback() {
                    override fun onPermissionsGranted(allGranted: Boolean, grantedPerms: Array<String>, requestedPerms: Array<String>) {
                        Log.e("onPermissionsGranted", grantedPerms.contentToString())
                        if (allGranted) {
                            Toast.makeText(this@MainActivity, "handle you logic!", Toast.LENGTH_SHORT).show()
                        }
                    }

                    override fun onPermissionsDenied(allDenied: Boolean, deniedPerms: Array<String>, requestedPerms: Array<String>) {
                        Log.e("onPermissionsDenied", deniedPerms.contentToString())
                    }

                    override fun onRequestCanceled(requestCode: Int) {
                        Log.e("onRequestCanceled", "$requestCode")
                    }
                })
        }
    }
}
