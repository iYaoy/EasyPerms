package com.iyao.sample

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import com.iyao.permission.interceptor.CheckPermissionsInterceptor
import com.iyao.permission.interceptor.Interceptor
import com.iyao.permission.interceptor.NotAskAgainPermsInterceptor
import com.iyao.permission.interceptor.ShowRationaleInterceptor

object GlobalCheckPermsInterceptor: CheckPermissionsInterceptor() {
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

object GlobalNotAskPermsPermsInterceptor: NotAskAgainPermsInterceptor(), ActivityCompat.PermissionCompatDelegate {

    private var chain: Interceptor.Chain? = null

    override fun requestPermissions(activity: Activity, permissions: Array<out String>, requestCode: Int): Boolean {
        return false
    }

    override fun onActivityResult(activity: Activity, requestCode: Int, resultCode: Int, data: Intent?): Boolean {
        ActivityCompat.setPermissionCompatDelegate(null)
        if (requestCode == REQUEST_SETTINGS) {
            chain?.proceed(chain?.request() ?: return false)
            chain = null
            return true
        } else return false
    }

    override fun intercept(context: Context, chain: Interceptor.Chain, permissions: Array<String>): Boolean {
        if (permissions.isEmpty()) {
            return chain.proceed(chain.request())
        }
        AlertDialog.Builder(context)
            .setTitle("这些权限被拒绝,且不再提示")
            .setItems(permissions, null)
            .setNeutralButton("好的") { dialog, _ ->
                this.chain = chain
                ActivityCompat.setPermissionCompatDelegate(this)
                turnToSettings(context as Activity)
                dialog.dismiss()
            }
            .create()
            .show()
        return true
    }

    private fun turnToSettings(activity: Activity) {
        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
        val uri = Uri.fromParts("package", activity.packageName, null)
        intent.data = uri
        activity.startActivityForResult(intent, REQUEST_SETTINGS)
    }

    private const val REQUEST_SETTINGS = 0x01
}