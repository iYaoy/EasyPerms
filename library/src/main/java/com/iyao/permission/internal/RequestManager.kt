package com.iyao.permission.internal

import android.os.Handler
import android.os.Looper
import androidx.core.app.ActivityCompat
import androidx.core.content.PermissionChecker.PERMISSION_DENIED
import androidx.core.content.PermissionChecker.PERMISSION_GRANTED
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import com.iyao.permission.EasyPerms
import com.iyao.permission.interceptor.Interceptor
import java.lang.ref.WeakReference

class RequestManager private constructor(private var handler: PermissionsRequestHandler) {

    companion object {
        private const val TAG_FRAGMENT = "com.iyao.permission.internal.RequestManager:FRAGMENT"
        fun get(activity: FragmentActivity): RequestManager {
            val fragmentManager = activity.supportFragmentManager
            var requestFragment = fragmentManager.findFragmentByTag(TAG_FRAGMENT) as? RequestFragmentHandler
            if (requestFragment == null) {
                requestFragment = RequestFragmentHandler()
            }
            var requestManager = requestFragment.manager
            if (requestManager == null) {
                requestManager = RequestManager(
                    WeakHandlerDelegate(WeakReference(requestFragment))
                )
                requestFragment.manager = requestManager
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

    private val delayQueue = arrayListOf<Request>()

    fun builder(): Request.Builder {
        return Request.Builder(this)
    }

    internal fun request(request: Request) {
        val fragment = (handler as WeakHandlerDelegate).handler.get() as? Fragment
        if (fragment == null || fragment.isAdded && !PermissionChain(
                0,
                request,
                handler
            ).proceed(request)) {
            request.callback.onRequestCanceled(request.code)
        } else {
            delayQueue.add(request)
        }
    }

    private fun handleDelayed() {
        delayQueue.forEach {
            request(it)
        }
    }

    internal class PermissionChain(private val index: Int,
                                   private val request: Request,
                                   private val handler: PermissionsRequestHandler
    ): Interceptor.Chain, ActivityCompat.OnRequestPermissionsResultCallback {

        override fun handler(): PermissionsRequestHandler {
            return handler
        }

        override fun request(): Request {
            return request
        }

        override fun proceed(request: Request): Boolean {
            val interceptors = request.interceptors
            return if (request.useInterceptor && index < interceptors.size) {
                val nextChain = PermissionChain(index + 1, request, handler)
                interceptors[index].intercept(nextChain)
            } else {
                handler.setResultCallback(this)
                realRequest(request)
                true
            }
        }

        private fun realRequest(request: Request) {
            val needPerms = request.permissions
            val requestPerms = handler.checkPermissions(needPerms)
            val callback = request.callback
            if (callback.dispatchRequest(needPerms.filterNot { requestPerms.contains(it) }.toTypedArray(), requestPerms)) {
                callback.onRequestStart(requestPerms)
                if (requestPerms.isNotEmpty()) {
                    handler.requestPermissions(request.code, *requestPerms)
                } else {
                    callback.onRequestResult(needPerms, arrayOf(), arrayOf())
                }
            }
        }

        override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
            val callback = request.callback
            if (permissions.isEmpty()) {
                callback.onRequestCanceled(requestCode)
                return
            }
            val groupedResult = permissions.withIndex().groupBy { grantResults[it.index] }
                .mapValues { entry -> entry.value.map { it.value } }
            val grantedPerms = groupedResult[PERMISSION_GRANTED].orEmpty().toTypedArray()
            val deniedPerms = groupedResult[PERMISSION_DENIED].orEmpty().toTypedArray()
            val doNotAskAgainPerms = handler.doNotAskAgainPermissions(deniedPerms)
            callback.onRequestResult(grantedPerms, deniedPerms, doNotAskAgainPerms)
        }
    }

    internal class WeakHandlerDelegate(val handler: WeakReference<PermissionsRequestHandler>):
        PermissionsRequestHandler {

        override fun setResultCallback(resultCallback: ActivityCompat.OnRequestPermissionsResultCallback) {
            handler.get()?.setResultCallback(resultCallback)
        }

        override fun checkPermissions(permissions: Array<String>): Array<String> {
            return handler.get()?.checkPermissions(permissions) ?: arrayOf()
        }

        override fun shouldShowRequestRationalePermissions(vararg permissions: String): Array<String> {
            return handler.get()?.shouldShowRequestRationalePermissions(*permissions) ?: arrayOf()
        }

        override fun doNotAskAgainPermissions(permissions: Array<String>): Array<String> {
            return handler.get()?.doNotAskAgainPermissions(permissions) ?: arrayOf()
        }

        override fun requestPermissions(requestCode: Int, vararg permissions: String) {
            handler.get()?.requestPermissions(requestCode, *permissions)
        }

    }
}