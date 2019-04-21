package com.iyao.permission

import androidx.fragment.app.FragmentActivity
import com.iyao.permission.interceptor.Interceptor
import com.iyao.permission.internal.RequestManager

object EasyPerms {

    internal val interceptors = ArrayList<Interceptor>()

    fun with(activity: FragmentActivity): RequestManager {
        return RequestManager.get(activity)
    }

    fun addInterceptor(interceptor: Interceptor) {
        if (!interceptors.contains(interceptor)) {
            interceptors.add(0, interceptor)
        }
    }
}