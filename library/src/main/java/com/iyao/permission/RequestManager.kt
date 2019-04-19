package com.iyao.permission

import android.os.Handler
import android.os.Looper
import android.util.SparseArray
import androidx.core.app.ActivityCompat
import androidx.core.content.PermissionChecker.PERMISSION_DENIED
import androidx.core.content.PermissionChecker.PERMISSION_GRANTED
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import com.iyao.permission.interceptor.Interceptor

class RequestManager private constructor(private val handler: PermissionsRequestHandler, private val easyPerms: EasyPerms): ActivityCompat.OnRequestPermissionsResultCallback {

    companion object {
        private const val TAG_FRAGMENT = "com.iyao.permission.RequestManager:FRAGMENT"
        fun get(activity: FragmentActivity, easyPerms: EasyPerms): RequestManager {
            val fragmentManager = activity.supportFragmentManager
            var requestFragment = fragmentManager.findFragmentByTag(TAG_FRAGMENT) as? RequestFragmentHandler
            if (requestFragment == null) {
                requestFragment = RequestFragmentHandler()
            }
            var requestManager = requestFragment.manager
            if (requestManager == null) {
                requestManager = RequestManager(requestFragment, easyPerms)
                requestFragment.manager = requestManager
                requestFragment.setResultCallback(requestManager)
            }
            if (!fragmentManager.isStateSaved && !fragmentManager.isDestroyed) {
                if (requestFragment.isDetached || !requestFragment.isAdded) {
                    val transaction = fragmentManager.beginTransaction()
                    transaction.add(requestFragment, TAG_FRAGMENT)
                    transaction.commitAllowingStateLoss()
                    Handler(Looper.getMainLooper()) {
                        if (it.what == 1) {
                            requestManager.handleDelayed()
                            return@Handler true
                        }  else return@Handler false
                    }.sendEmptyMessage(1)
                }
            }
            return requestManager
        }
    }

    private var hasCurrentPermissionsRequest = false
    private val callbackHolder = SparseArray<Callback>(1)
    private val delayQueue = arrayListOf<Request>()

    fun builder(): Request.Builder {
        return Request.Builder(this)
    }

    internal fun request(request: Request) {
        if (hasCurrentPermissionsRequest) {
            request.callback.onRequestCanceled(request.code)
            return
        }
        val fragment = handler as Fragment
        if (fragment.isAdded && !fragment.isDetached) {
            hasCurrentPermissionsRequest = true
            callbackHolder.put(request.code, request.callback)
            if (!PermissionChain(0, request, handler).proceed(request)) {
                request.callback.onPermissionsGranted(true, arrayOf(), arrayOf())
                hasCurrentPermissionsRequest = false
            }
        } else {
            delayQueue.add(request)
        }
    }

    private fun handleDelayed() {
        delayQueue.forEach {
            request(it)
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        val callback = callbackHolder[requestCode]
        callbackHolder.clear()
        val groupedResult = permissions.withIndex().groupBy { grantResults[it.index] }
            .mapValues { entry -> entry.value.map { it.value } }
        val grantedPerms = groupedResult[PERMISSION_GRANTED].orEmpty().toTypedArray()
        val deniedPerms = groupedResult[PERMISSION_DENIED].orEmpty().toTypedArray()
        if (grantedPerms.isNotEmpty()) {
            callback?.onPermissionsGranted(grantedPerms.contentEquals(permissions), grantedPerms, permissions)
        }
        if (deniedPerms.isNotEmpty()) {
            callback?.onPermissionsDenied(deniedPerms.contentEquals(permissions), deniedPerms, permissions)
        }
        hasCurrentPermissionsRequest = false
    }

    private inner class PermissionChain(private val index: Int,
                                private val request: Request,
                                private val handler: PermissionsRequestHandler
    ): Interceptor.Chain {

        override fun handler(): PermissionsRequestHandler {
            return handler
        }

        override fun request(): Request {
            return request
        }

        override fun proceed(request: Request): Boolean {
            val interceptors = easyPerms.interceptors
            return if (index < interceptors.size) {
                val nextChain = PermissionChain(index+1, request, handler)
                interceptors[index].intercept(nextChain)
            } else {
                false
            }
        }
    }
}