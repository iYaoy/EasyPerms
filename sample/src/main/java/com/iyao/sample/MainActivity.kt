package com.iyao.sample

import android.Manifest.permission.*
import android.app.Activity
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.iyao.permission.callback.Callback
import com.iyao.permission.EasyPerms
import com.iyao.permission.callback.ContextCallback
import com.iyao.permission.internal.Request
import kotlinx.android.synthetic.main.activity_main.*


class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        EasyPerms.addInterceptor(GlobalRationalePermsInterceptor)
        EasyPerms.addInterceptor(GlobalCheckPermsInterceptor)
        txtClick.setOnClickListener {
            EasyPerms.with(this)
                .builder()
                .requestCode(100)
                .useInterceptor(false)
                .permissions(CAMERA, WRITE_EXTERNAL_STORAGE, READ_CALENDAR, READ_CALL_LOG, READ_EXTERNAL_STORAGE, READ_SMS, READ_PHONE_STATE)
                .request(object: ContextCallback() {

                    override fun onRequestStart(needGrantPerms: Array<String>) {
                        Log.e("onRequestStart", needGrantPerms.contentToString())
                    }
                    override fun onAllPermsGrated() {
                        Log.e("onAllPermsGrated","onAllPermsGrated")
                    }
                    override fun onPermsDeniedFinally(deniedPerms: Array<String>, doNotAskAgainPerms: Array<String>) {
                        Log.e("onPermsDeniedFinally",deniedPerms.contentToString())
                        Log.e("onPermsDeniedFinally",doNotAskAgainPerms.contentToString())
                    }

                    override fun onRequestCanceled(requestCode: Int) {
                        Log.e("onRequestCanceled","$requestCode")
                    }
                })
        }
    }
}
