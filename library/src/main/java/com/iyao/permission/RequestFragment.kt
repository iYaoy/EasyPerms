package com.iyao.permission

import android.app.Activity
import android.app.AppOpsManager
import android.app.AppOpsManager.MODE_ALLOWED
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager.PERMISSION_DENIED
import android.content.pm.PackageManager.PERMISSION_GRANTED
import android.net.Uri
import android.os.Bundle
import android.os.Process
import android.provider.Settings
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import java.util.*

class RequestFragment : Fragment() {

    var requestManager: RequestManager? = null
    private var request: Request? = null
    private var hasCurrentPermissionsRequest = false
    private val delayedRequests = Collections.newSetFromMap(HashMap<Request, Boolean>())

    fun request(request: Request) {
        val requestManager = requestManager
        val callback = request.callback
        if (hasCurrentPermissionsRequest || requestManager == null) {
            callback.onRequestCanceled(request.code)
            return
        }
        val permissions = request.permissions
        if (permissions.isNotEmpty()) {
            this.request = request
            hasCurrentPermissionsRequest = true
            val shouldGrantPerms = shouldGrantPermissions(permissions, request.checkWithOps)
            if (shouldGrantPerms.isNotEmpty() && !callback.onGrantPermissionStart(requestManager, shouldGrantPerms)) {
                requestOnStart(request)
            } else {
                callback.onPermissionGranted(true, permissions)
                hasCurrentPermissionsRequest = false
            }
        } else if (!isAdded) {
            delayedRequests.add(request)
        } else {
            callback.onRequestCanceled(request.code)
            hasCurrentPermissionsRequest = false
        }
    }

    fun requestOnStart(request: Request? = this.request) {
        val finalRequest = request ?: return
        val permissions = request.permissions
        val rationalePerms = permissions.filter { shouldShowRequestPermissionRationale(it) }.toTypedArray()
        if (!finalRequest.callback.onShowRequestPermissionsRationale(requestManager ?: return, rationalePerms)) {
            requestOnShowRationale(request)
        } else {
            hasCurrentPermissionsRequest = false
        }
    }



    fun requestOnShowRationale(request: Request? = this.request) {
        if (request == null) {
            hasCurrentPermissionsRequest = false
            return
        }
        if (!isDetached && host != null) {
            hasCurrentPermissionsRequest = true
            requestPermissions(request.permissions, request.code)
        } else {
            request.callback.onRequestCanceled(request.code)
            hasCurrentPermissionsRequest = false
        }
    }



    private fun shouldGrantPermissions(permissions: Array<out String>, checkWithOps: Boolean): Array<String> {
        return permissions.filter { !checkPermission(it, checkWithOps) }.toTypedArray()
    }

    private fun checkPermission(permission: String, checkWithOps: Boolean): Boolean {
        val activity = this.activity ?: return false
        val appOpsManager = activity.getSystemService(Context.APP_OPS_SERVICE) as AppOpsManager
        return ContextCompat.checkSelfPermission(
            activity,
            permission
        ) == PERMISSION_GRANTED && (!checkWithOps || appOpsManager.checkOpNoThrow(
            AppOpsManager.permissionToOp(permission),
            Process.myUid(),
            activity.packageName
        ) == MODE_ALLOWED)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        val finalRequest = request
        if (CODE_SETTINGS == requestCode && finalRequest != null) {
            val groupedMap = finalRequest.permissions.groupBy {
                if (checkPermission(it, finalRequest.checkWithOps)) PERMISSION_GRANTED else  PERMISSION_DENIED
            }
            disPatchResult(finalRequest, groupedMap)
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        val request = this.request ?: return
        val callback = request.callback
        if (permissions.isEmpty() && grantResults.isEmpty()) {
            //request more than one set of permissions at a time, canceled
            callback.onRequestCanceled(requestCode)
            hasCurrentPermissionsRequest = false
            return
        }
        val groupedMap = permissions.withIndex().groupBy {
            when (grantResults[it.index]) {
                PERMISSION_GRANTED -> PERMISSION_GRANTED
                else -> PERMISSION_DENIED
            }
        }.mapValues { entry -> entry.value.map { it.value } }
        val deniedPerms = groupedMap[PERMISSION_DENIED]?.toTypedArray() ?: arrayOf()
        if (deniedPerms.all { !shouldShowRequestPermissionRationale(it) } && request.turnToSettings) {
            turnToSettings(activity ?: return)
        } else {
            disPatchResult(request, groupedMap)
        }
    }

    private fun disPatchResult(request: Request, groupedPerms: Map<Int, List<String>>) {
        val permissions = request.permissions
        val callback = request.callback
        val grantedPerms = groupedPerms[PERMISSION_GRANTED]?.toTypedArray() ?: arrayOf()
        val deniedPerms = groupedPerms[PERMISSION_DENIED]?.toTypedArray() ?: arrayOf()
        callback.onPermissionGranted(permissions.contentDeepEquals(grantedPerms), grantedPerms)
        callback.onPermissionDenied(permissions.contentDeepEquals(deniedPerms), deniedPerms)
        hasCurrentPermissionsRequest = false
    }

    private fun turnToSettings(activity: Activity) {
        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
        val uri = Uri.fromParts("package", activity.packageName, null)
        intent.data = uri
        startActivityForResult(intent, CODE_SETTINGS)
    }

    override fun onViewStateRestored(savedInstanceState: Bundle?) {
        super.onViewStateRestored(savedInstanceState)
        val savedRequest = savedInstanceState?.getParcelable<Request>(STATE_REQUEST)
        this.request = savedRequest
        val savedState = savedInstanceState?.getBoolean(STATE_HAS_CURRENT_REQUEST, false) ?: false
        this.hasCurrentPermissionsRequest = savedState
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putParcelable(STATE_REQUEST, request)
        outState.putBoolean(STATE_HAS_CURRENT_REQUEST, hasCurrentPermissionsRequest)
    }

    override fun onStart() {
        super.onStart()
        delayedRequests.forEach {
            request(it)
        }
        delayedRequests.clear()
    }

    override fun onDestroy() {
        super.onDestroy()
        hasCurrentPermissionsRequest = false
        delayedRequests.clear()
        requestManager = null
        request = null
    }

    companion object {
        const val CODE_SETTINGS = 0x01
        const val STATE_REQUEST = "request"
        const val STATE_HAS_CURRENT_REQUEST = "hasCurrentRequest"
    }
}