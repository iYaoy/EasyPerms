package com.iyao.permission

import android.app.AppOpsManager
import android.app.AppOpsManager.MODE_ALLOWED
import android.content.Context
import android.content.pm.PackageManager.PERMISSION_DENIED
import android.content.pm.PackageManager.PERMISSION_GRANTED
import android.os.Bundle
import android.os.Process
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import java.util.*

class RequestFragment : Fragment() {

    var requestManager: RequestManager? = null
    private var request: Request? = null
    private var hasCurrentPermissionsRequest = false
    private val delayedRequests = Collections.newSetFromMap(HashMap<Request, Boolean>())

    fun request(request: Request) {
        if (hasCurrentPermissionsRequest) {
            request.callback.onRequestCanceled(request.code)
            return
        }
        val activity = activity
        if (activity != null && request.permissions.isNotEmpty()) {
            this.request = request
            hasCurrentPermissionsRequest = true
            val shouldGrantPerms = shouldGrantPermissions(activity, request.permissions, request.checkWithOps)
            if (shouldGrantPerms.isNotEmpty()) {
                val rationalePerms = shouldShowRequestRationalePermissions(shouldGrantPerms)
                if (!request.callback.onGrantPermissionStart(shouldGrantPerms) {
                        doShouldShowRequestPermissionsRationale(request, rationalePerms)
                    }) {
                    doShouldShowRequestPermissionsRationale(request, rationalePerms)
                }
            } else {
                request.callback.onPermissionGranted(true, request.permissions)
                hasCurrentPermissionsRequest = false
            }
        } else if (!isAdded) {
            delayedRequests.add(request)
        } else {
            request.callback.onRequestCanceled(request.code)
            hasCurrentPermissionsRequest = false
        }
    }

    private fun doShouldShowRequestPermissionsRationale(request: Request, rationalePerms: Array<String>) {
        if (rationalePerms.isEmpty() || !request.callback.onShowRequestPermissionsRationale(rationalePerms) {
                realRequest(request)
            }) {
            realRequest(request)
        }
    }
    private fun realRequest(request: Request) {
        if (!isDetached && host != null) {
            requestPermissions(request.permissions, request.code)
        } else {
            request.callback.onRequestCanceled(request.code)
            hasCurrentPermissionsRequest = false
        }
    }
    private fun shouldGrantPermissions(context: Context, permissions: Array<out String>, checkWithOps: Boolean): Array<String> {
        return permissions.filter { !checkPermission(context, it, checkWithOps) }.toTypedArray()
    }

    private fun shouldShowRequestRationalePermissions(permissions: Array<out String>): Array<String> {
        return permissions.filter { shouldShowRequestPermissionRationale(it) }.toTypedArray()
    }

    private fun checkPermission(context: Context, permission: String, checkWithOps: Boolean): Boolean {
        val appOpsManager = context.getSystemService(Context.APP_OPS_SERVICE) as AppOpsManager
        return ContextCompat.checkSelfPermission(
            context,
            permission
        ) == PERMISSION_GRANTED && (!checkWithOps || appOpsManager.checkOpNoThrow(
            AppOpsManager.permissionToOp(permission),
            Process.myUid(),
            context.packageName
        ) == MODE_ALLOWED)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        hasCurrentPermissionsRequest = false
        val request = this.request ?: return
        if (permissions.isEmpty() && grantResults.isEmpty()) {
            //request more than one set of permissions at a time, canceled
            request.callback.onRequestCanceled(requestCode)
            return
        }
        val groupedMap = permissions.withIndex().groupBy {
            when (grantResults[it.index]) {
                PERMISSION_GRANTED -> PERMISSION_GRANTED
                else -> PERMISSION_DENIED
            }
        }
        val grantedPermissions = groupedMap[PERMISSION_GRANTED]?.map { it.value }?.toTypedArray() ?: arrayOf()
        request.callback.onPermissionGranted(
            request.permissions.contentDeepEquals(grantedPermissions),
            grantedPermissions
        )
        val deniedPermissions = groupedMap[PERMISSION_DENIED]?.map { it.value }?.toTypedArray() ?: arrayOf()
        request.callback.onPermissionDenied(
            request.permissions.contentDeepEquals(deniedPermissions),
            deniedPermissions
        )
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
        request = null
        requestManager = null
    }

    companion object {
        const val STATE_REQUEST = "request"
        const val STATE_HAS_CURRENT_REQUEST = "hasCurrentRequest"
    }
}