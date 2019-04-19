package com.iyao.permission.interceptor

import android.content.Context

internal class RealRequestInterceptor: PermissionsInterceptor() {

    override fun findPermissions(chain: Interceptor.Chain): Array<String> {
        return chain.handler().checkPermissions(chain.request().permissions)
    }
    override fun intercept(context: Context, chain: Interceptor.Chain, permissions: Array<String>): Boolean {
        if (permissions.isNotEmpty()) {
            chain.handler().requestPermissions(chain.request().code, *permissions)
            return true
        }
        return false
    }
}