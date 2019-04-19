package com.iyao.permission.interceptor

import com.iyao.permission.PermissionsRequestHandler
import com.iyao.permission.Request

interface Interceptor {

    fun intercept(chain: Chain): Boolean

    interface Chain {
        fun handler(): PermissionsRequestHandler
        fun request(): Request
        fun proceed(request: Request): Boolean
    }
}