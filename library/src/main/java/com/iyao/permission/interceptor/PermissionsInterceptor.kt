package com.iyao.permission.interceptor

import android.content.Context
import androidx.fragment.app.Fragment

open class PermissionsInterceptor: Interceptor {
    final override fun intercept(chain: Interceptor.Chain): Boolean {
        val permissions = findPermissions(chain)
        return intercept((chain.handler() as Fragment).activity ?: return false, chain, permissions)
    }

    protected open fun findPermissions(chain: Interceptor.Chain) = chain.request().permissions

    protected open fun intercept(context: Context, chain: Interceptor.Chain, permissions: Array<String>) = false
}