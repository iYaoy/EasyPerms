package com.iyao.permission.interceptor

open class NotAskAgainPermsInterceptor: PermissionsInterceptor() {

    final override fun findPermissions(chain: Interceptor.Chain): Array<String> {
        val handler = chain.handler()
        val request = chain.request()
        return handler.doNotAskAgainPermissions(request.permissions)
    }

}