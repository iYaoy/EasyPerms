package com.iyao.permission

import androidx.fragment.app.FragmentActivity

object Permissions {

    fun with(activity: FragmentActivity): Request.Builder {
        return RequestManager.get(activity)
    }

}