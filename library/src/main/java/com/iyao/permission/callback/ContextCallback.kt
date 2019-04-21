package com.iyao.permission.callback

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.core.app.ActivityCompat
import com.iyao.permission.internal.Request
import java.lang.ref.WeakReference
import kotlin.properties.Delegates

open class ContextCallback: BaseCallback(), ActivityCompat.PermissionCompatDelegate {

    private var requestBySettings = false
    private var activityRef = WeakReference<Activity>(null)
    internal var request: Request by Delegates.notNull()

    final override fun requestPermissions(activity: Activity, permissions: Array<out String>, requestCode: Int): Boolean {
        this.activityRef = WeakReference(activity)
        return false
    }

    final override fun onActivityResult(activity: Activity, requestCode: Int, resultCode: Int, data: Intent?): Boolean {
        if (activity == activityRef.get() && requestCode == request.code) {
            ActivityCompat.setPermissionCompatDelegate(null)
            request.newBuilder().useInterceptor(false).request(this)
            activityRef.clear()
        }
        return true
    }

    final override fun dispatchRequest(alreadyGrantedPerms: Array<String>, needGrantPerms: Array<String>): Boolean {
        val bySettings = requestBySettings
        if (bySettings) {
            requestBySettings = false
        }
        when {
            needGrantPerms.isEmpty() -> onAllPermsGrated()
            bySettings -> onPermsDeniedFinally(needGrantPerms, needGrantPerms)
            else -> {
                ActivityCompat.setPermissionCompatDelegate(this)
                super.dispatchRequest(alreadyGrantedPerms, needGrantPerms)
            }
        }
        return !bySettings
    }

    final override fun onPermsDenied(deniedPerms: Array<String>, doNotAskAgainPerms: Array<String>) {
        val activity = activityRef.get()
        if (deniedPerms.contentEquals( doNotAskAgainPerms) && activity != null && checkBySettings(activity, doNotAskAgainPerms)) {
            requestBySettings = true
        } else {
            onPermsDeniedFinally(deniedPerms, doNotAskAgainPerms)
        }
    }

    open fun onPermsDeniedFinally(deniedPerms: Array<String>, doNotAskAgainPerms: Array<String>) {

    }

    open fun checkBySettings(activity: Activity, doNotAskAgainPerms: Array<String>): Boolean {
        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
        val uri = Uri.fromParts("package", activity.packageName, null)
        intent.data = uri
        activity.startActivityForResult(intent, request.code)
        return true
    }
}