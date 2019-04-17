package com.iyao.permission

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
open class Callback: Parcelable {
    open fun onPermissionGranted(allGranted: Boolean, vararg grantedPermissions: String) {}
    open fun onPermissionDenied(allDenied: Boolean, vararg deniedPermissions: String) {}
}