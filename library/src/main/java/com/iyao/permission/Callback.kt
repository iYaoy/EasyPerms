package com.iyao.permission

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
open class Callback: Parcelable {
    open fun onGrantPermissionStart(shouldGrantPerms: Array<String>, goon: ()->Unit): Boolean { return false }
    open fun onShowRequestPermissionsRationale(rationalePerms: Array<String>, goon: ()->Unit): Boolean { return false }
    open fun onPermissionGranted(allGranted: Boolean, grantedPerms: Array<String>) {}
    open fun onPermissionDenied(allDenied: Boolean, deniedPerms: Array<String>) {}
    open fun onRequestCanceled(requestCode: Int) {}
}