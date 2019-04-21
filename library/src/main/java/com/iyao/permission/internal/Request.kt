package com.iyao.permission.internal

import com.iyao.permission.EasyPerms
import com.iyao.permission.callback.BaseCallback
import com.iyao.permission.callback.Callback
import com.iyao.permission.callback.ContextCallback
import com.iyao.permission.interceptor.Interceptor

class Request private constructor(private val requestManager: RequestManager,
                                  val code: Int,
                                  val permissions: Array<String>,
                                  internal val useInterceptor: Boolean,
                                  internal val interceptors: ArrayList<Interceptor>,
                                  internal val callback: Callback) {

    fun request() {
        requestManager.request(this)
    }

    internal fun newBuilder() = Builder(requestManager, code, permissions, interceptors, useInterceptor)

    class Builder internal constructor(private val requestManager: RequestManager,
                                       private var requestCode: Int = 100,
                                       private var permissions: Array<String> = arrayOf(),
                                       private var interceptors: ArrayList<Interceptor> = arrayListOf(),
                                       private var useInterceptor: Boolean = true) {
        init {
            if (useInterceptor && interceptors.isEmpty()) {
                interceptors.addAll(EasyPerms.interceptors)
            }
        }
        fun requestCode(code: Int): Builder {
            if (requestCode and -0x10000 != 0) {
                throw IllegalArgumentException("Can only use lower 16 bits for requestCode")
            }
            this.requestCode = code
            return this
        }

        fun permissions(vararg permissions: String): Builder {
            if (permissions.isEmpty()) {
                throw IllegalAccessException("permissions requested should not be empty.")
            }
            this.permissions = permissions.toList().toTypedArray()
            return this
        }

        fun interceptors(vararg interceptor: Interceptor): Builder {
            interceptors.clear()
            interceptors.addAll(interceptor)
            return useInterceptor(true)
        }

        fun useInterceptor(use: Boolean): Builder {
            useInterceptor = use
            return this
        }

        fun request(callback: Callback) {
            val request = Request(requestManager, requestCode, permissions, useInterceptor, interceptors, callback)
            if (callback is ContextCallback) {
                callback.request = request
            }
            request.request()
        }

        fun request(onStart: ((Array<String>)->Unit)? = null, onResult: ((Array<String>, Array<String>, Array<String>)->Unit)? = null, onCancel: ((Int)->Unit)? = null) {
            request(object: Callback() {
                override fun onRequestStart(needGrantPerms: Array<String>) {
                    onStart?.invoke(needGrantPerms)
                }

                override fun onRequestResult(
                    grantedPerms: Array<String>,
                    deniedPerms: Array<String>,
                    doNotAskAgainPerms: Array<String>
                ) {
                    onResult?.invoke(grantedPerms, deniedPerms, doNotAskAgainPerms)
                }

                override fun onRequestCanceled(requestCode: Int) {
                    onCancel?.invoke(requestCode)
                }
            })
        }

        fun request(onStart: ((Array<String>)->Unit)? = null, onAllGranted: (()->Unit)? = null, onPermsDenied: ((Array<String>, Array<String>)->Unit)? = null, onCancel: ((Int)->Unit)? = null) {
            request(object: BaseCallback() {

                override fun onRequestStart(needGrantPerms: Array<String>) {
                    onStart?.invoke(needGrantPerms)
                }

                override fun onAllPermsGrated() {
                    onAllGranted?.invoke()
                }

                override fun onPermsDenied(deniedPerms: Array<String>, doNotAskAgainPerms: Array<String>) {
                    onPermsDenied?.invoke(deniedPerms, doNotAskAgainPerms)
                }

                override fun onRequestCanceled(requestCode: Int) {
                    onCancel?.invoke(requestCode)
                }
            })
        }
    }
}