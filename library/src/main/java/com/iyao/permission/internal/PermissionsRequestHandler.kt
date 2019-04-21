package com.iyao.permission.internal

import androidx.core.app.ActivityCompat

interface PermissionsRequestHandler {
    fun setResultCallback(resultCallback: ActivityCompat.OnRequestPermissionsResultCallback)
    fun checkPermissions(permissions: Array<String>): Array<String>
    fun shouldShowRequestRationalePermissions(vararg permissions: String): Array<String>
    fun doNotAskAgainPermissions(permissions: Array<String>): Array<String>
    fun requestPermissions(requestCode: Int, vararg permissions: String)
}