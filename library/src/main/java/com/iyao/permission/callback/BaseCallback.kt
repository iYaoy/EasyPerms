package com.iyao.permission.callback

open class BaseCallback: Callback() {

    final override fun onRequestResult(grantedPerms: Array<String>, deniedPerms: Array<String>, doNotAskAgainPerms: Array<String>) {
        if (deniedPerms.isEmpty()) {
            onAllPermsGrated()
        } else {
            onPermsDenied(deniedPerms, doNotAskAgainPerms)
        }
    }

    open fun onAllPermsGrated() {

    }

    open fun onPermsDenied(deniedPerms: Array<String>, doNotAskAgainPerms: Array<String>) {

    }
}