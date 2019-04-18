package com.iyao.permission

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize
import kotlin.properties.Delegates

/**
 * permission request lifecycle.
 */
@Parcelize
open class Callback: Parcelable {
    /**
     * invoked when request started
     *
     * @param shouldGrantPerms permissions should be allow
     * @return true if you want to do some thing before a real permission request starts, such as showing
     * a dialog to show user the permissions you need. false otherwise.
     */
    open fun onGrantPermissionStart(requestManager: RequestManager, shouldGrantPerms: Array<String>): Boolean { return false }

    /**
     * in this method, you can show a UI with rationale for requesting a permissions to show user why the
     * permissions you need. always be invoked after [onGrantPermissionStart].
     *
     * @param rationalePerms permissions for which the rationale should be show.
     * @see androidx.fragment.app.Fragment.shouldShowRequestPermissionRationale
     */
    open fun onShowRequestPermissionsRationale(requestManager: RequestManager, rationalePerms: Array<String>): Boolean { return false }

    open fun onPermissionGranted(allGranted: Boolean, grantedPerms: Array<String>) {}

    open fun onPermissionDenied(allDenied: Boolean, deniedPerms: Array<String>) {}

    open fun onRequestCanceled(requestCode: Int) {}
}