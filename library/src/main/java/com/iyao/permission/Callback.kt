package com.iyao.permission

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

/**
 * permission request lifecycle.
 */
@Parcelize
open class Callback: Parcelable {

    open fun onPermissionsGranted(allGranted: Boolean, grantedPerms: Array<String>, requestedPerms: Array<String>) {}

    open fun onPermissionsDenied(allDenied: Boolean, deniedPerms: Array<String>, requestedPerms: Array<String>) {}

    open fun onRequestCanceled(requestCode: Int) {}
}