package com.iyao.sample

import android.Manifest.permission.*
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.iyao.permission.Callback
import com.iyao.permission.Permissions
import com.iyao.permission.RequestManager
import kotlinx.android.synthetic.main.activity_main.*


class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        txtClick.setOnClickListener {
            Permissions.with(this)
                .turnToSettingsWhenDoNotAskAgain(true)
                .permissions(CAMERA, WRITE_EXTERNAL_STORAGE, READ_CALENDAR, READ_CALL_LOG, READ_EXTERNAL_STORAGE, READ_SMS, READ_PHONE_STATE)
                .check(object : Callback() {
                    override fun onGrantPermissionStart(
                        requestManager: RequestManager,
                        shouldGrantPerms: Array<String>
                    ): Boolean {
                        AlertDialog.Builder(this@MainActivity)
                            .setMessage("onGrantPermissionStart，想你所想")
                            .setPositiveButton("确定") { dialog, which ->
                                requestManager.requestOnStart()
                            }
                            .setNegativeButton("取消") { dialog, which ->
                                dialog.dismiss()
                            }
                            .create()
                            .show()
                        return true
                    }

                    override fun onShowRequestPermissionsRationale(
                        requestManager: RequestManager,
                        rationalePerms: Array<String>
                    ): Boolean {
                        AlertDialog.Builder(this@MainActivity)
                            .setMessage("onShowRequestPermissionsRationale，想你所想")
                            .setPositiveButton("确定") { dialog, which ->
                                requestManager.requestOnShowRationale()
                            }
                            .setNegativeButton("取消") { dialog, which ->
                                dialog.dismiss()
                            }
                            .create()
                            .show()
                        return true
                    }
                })
        }
    }
}
