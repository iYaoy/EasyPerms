package com.iyao.permission.interceptor

import com.iyao.permission.internal.PermissionsRequestHandler
import com.iyao.permission.internal.Request

interface Interceptor {

    fun intercept(chain: Chain): Boolean

    interface Chain {
        fun handler(): PermissionsRequestHandler
        fun request(): Request
        fun proceed(request: Request): Boolean
    }
}