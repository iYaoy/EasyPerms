package com.iyao.permission.interceptor

open class ShowRationaleInterceptor: PermissionsInterceptor() {

    final override fun findPermissions(chain: Interceptor.Chain): Array<String> {
        return chain.handler().shouldShowRequestRationalePermissions(*chain.request().permissions)
    }

}