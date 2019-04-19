package com.iyao.permission

import androidx.fragment.app.FragmentActivity
import com.iyao.permission.interceptor.Interceptor
import com.iyao.permission.interceptor.RealRequestInterceptor

object EasyPerms {

    internal val interceptors = ArrayList<Interceptor>()

    init {
        addInterceptor(RealRequestInterceptor())
    }

    fun with(activity: FragmentActivity): RequestManager {
        return RequestManager.get(activity, this)
    }

    fun addInterceptor(interceptor: Interceptor) {
        if (!interceptors.contains(interceptor)) {
            interceptors.add(0, interceptor)
        }
    }
}