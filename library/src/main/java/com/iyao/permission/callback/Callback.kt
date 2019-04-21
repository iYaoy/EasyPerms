package com.iyao.permission.callback

/**
 * permission request lifecycle.
 */

open class Callback {

    internal open fun dispatchRequest(alreadyGrantedPerms: Array<String>, needGrantPerms: Array<String>): Boolean {
        return true
    }

    open fun onRequestStart(needGrantPerms: Array<String>) {}

    open fun onRequestResult(grantedPerms: Array<String>, deniedPerms: Array<String>, doNotAskAgainPerms: Array<String>) {}

    open fun onRequestCanceled(requestCode: Int) {}
}