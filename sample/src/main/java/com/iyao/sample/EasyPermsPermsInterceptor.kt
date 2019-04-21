package com.iyao.sample

import android.content.Context
import androidx.appcompat.app.AlertDialog
import com.iyao.permission.interceptor.CheckPermissionsInterceptor
import com.iyao.permission.interceptor.Interceptor
import com.iyao.permission.interceptor.ShowRationaleInterceptor

object GlobalCheckPermsInterceptor: CheckPermissionsInterceptor() {

    @Suppress("OverridingDeprecatedMember")
    override fun intercept(context: Context, chain: Interceptor.Chain, permissions: Array<String>): Boolean {
        if (permissions.isEmpty()) {
            return chain.proceed(chain.request())
        }
        AlertDialog.Builder(context)
            .setTitle("需要的权限")
            .setItems(permissions, null)
            .setNeutralButton("好的") { dialog, _ ->
                chain.proceed(chain.request())
                dialog.dismiss()
            }
            .create()
            .show()
        return true
    }
}

object GlobalRationalePermsInterceptor: ShowRationaleInterceptor() {

    @Suppress("OverridingDeprecatedMember")
    override fun intercept(context: Context, chain: Interceptor.Chain, permissions: Array<String>): Boolean {
        if (permissions.isEmpty()) {
            return chain.proceed(chain.request())
        }
        AlertDialog.Builder(context)
            .setTitle("需要说明原因的权限")
            .setItems(permissions, null)
            .setNeutralButton("好的") { dialog, _ ->
                chain.proceed(chain.request())
                dialog.dismiss()
            }
            .create()
            .show()
        return true
    }
}