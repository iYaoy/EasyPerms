package com.iyao.permission.interceptor

import android.content.Context
import androidx.fragment.app.Fragment
import com.iyao.permission.internal.RequestManager

open class PermissionsInterceptor : Interceptor {

    @Suppress("DEPRECATION")
    final override fun intercept(chain: Interceptor.Chain): Boolean {
        val permissions = findPermissions(chain)
        return intercept((((chain as RequestManager.PermissionChain).handler() as RequestManager.WeakHandlerDelegate).handler.get() as? Fragment)?.activity
                ?: return chain.proceed(chain.request()), chain, permissions
        )
    }

    protected open fun findPermissions(chain: Interceptor.Chain) = chain.request().permissions

    @Deprecated("Make sure to avoid 'Context Memory Leak'!", ReplaceWith(""))
    protected open fun intercept(context: Context, chain: Interceptor.Chain, permissions: Array<String>) = false
}