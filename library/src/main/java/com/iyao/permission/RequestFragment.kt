package com.iyao.permission

import android.content.pm.PackageManager.PERMISSION_DENIED
import android.content.pm.PackageManager.PERMISSION_GRANTED
import android.os.Bundle
import android.util.SparseArray
import androidx.core.util.putAll
import androidx.fragment.app.Fragment
import java.util.*
import kotlin.collections.HashMap

class RequestFragment: Fragment() {

    var requestManager: RequestManager? = null
    private val requests = SparseArray<Request>()
    private val delayedRequests = Collections.newSetFromMap(HashMap<Request, Boolean>())
    fun request(request: Request) {
        if (isDetached || host == null) {
            delayedRequests.add(request)
        } else {
            realRequest(request)
        }
    }

    private fun realRequest(request: Request) {
        if (!isDetached && host != null) {
            requests.put(request.requestCode, request)
            requestPermissions(request.permissions, request.requestCode)
        } else {
            request.callback.onPermissionDenied(true, *request.permissions)
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        val request = requests.get(requestCode) ?: return
        requests.remove(requestCode)
        val groupedMap = permissions.withIndex().groupBy {
            when (grantResults[it.index]) {
                PERMISSION_GRANTED -> PERMISSION_GRANTED
                else -> PERMISSION_DENIED
            }
        }
        val grantedPermissions = groupedMap[PERMISSION_GRANTED]?.map { it.value }?.toTypedArray() ?: arrayOf()
        request.callback.onPermissionGranted(request.permissions.contentDeepEquals(grantedPermissions), *grantedPermissions)
        val deniedPermissions = groupedMap[PERMISSION_DENIED]?.map { it.value }?.toTypedArray() ?: arrayOf()
        request.callback.onPermissionDenied(request.permissions.contentDeepEquals(deniedPermissions), *deniedPermissions)
    }

    override fun onViewStateRestored(savedInstanceState: Bundle?) {
        super.onViewStateRestored(savedInstanceState)
        val savedRequests = savedInstanceState?.getSparseParcelableArray<Request>(STATE_REQUESTS) ?: return
        requests.putAll(savedRequests)
    }
    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putSparseParcelableArray(STATE_REQUESTS, requests)
    }

    override fun onStart() {
        super.onStart()
        delayedRequests.forEach {
            realRequest(it)
        }
        delayedRequests.clear()
    }

    override fun onDestroy() {
        super.onDestroy()
        requests.clear()
        delayedRequests.clear()
        requestManager = null
    }

    companion object {
        const val STATE_REQUESTS = "requests"
    }
}