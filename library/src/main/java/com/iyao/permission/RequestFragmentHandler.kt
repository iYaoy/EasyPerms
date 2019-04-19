package com.iyao.permission

import androidx.core.app.ActivityCompat
import androidx.core.content.PermissionChecker
import androidx.core.content.PermissionChecker.PERMISSION_GRANTED
import androidx.fragment.app.Fragment

class RequestFragmentHandler : Fragment(), PermissionsRequestHandler {

    private var resultCallback: ActivityCompat.OnRequestPermissionsResultCallback? = null
    var manager: RequestManager? = null

    override fun setResultCallback(resultCallback: ActivityCompat.OnRequestPermissionsResultCallback) {
        this.resultCallback = resultCallback
    }

    override fun checkPermissions(permissions: Array<String>): Array<String> {
        return permissions.filter { !checkPermission(it) }.toTypedArray()
    }

    private fun checkPermission(permission: String): Boolean {
        return PermissionChecker.checkCallingOrSelfPermission(activity ?: return false, permission) == PERMISSION_GRANTED
    }

    override fun shouldShowRequestRationalePermissions(vararg permissions: String): Array<String> {
        return permissions.filter { shouldShowRequestPermissionRationale(it) }.toTypedArray()
    }

    override fun doNotAskAgainPermissions(permissions: Array<String>): Array<String> {
        return checkPermissions(permissions).filter{ !shouldShowRequestPermissionRationale(it) }.toTypedArray()
    }

    override fun requestPermissions(requestCode: Int, vararg permissions: String) {
        requestPermissions(permissions, requestCode)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        resultCallback?.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }
}