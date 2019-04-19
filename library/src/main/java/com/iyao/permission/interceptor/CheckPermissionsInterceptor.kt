package com.iyao.permission.interceptor

open class CheckPermissionsInterceptor: PermissionsInterceptor() {

    final override fun findPermissions(chain: Interceptor.Chain): Array<String> {
        val request = chain.request()
        return chain.handler().checkPermissions(request.permissions)
    }
}
