package com.iyao.permission

import android.os.Parcelable
import kotlinx.android.parcel.IgnoredOnParcel
import kotlinx.android.parcel.Parcelize
import java.util.function.BiFunction
import kotlin.properties.Delegates

@Parcelize
class Request private constructor(val code: Int, val checkWithOps: Boolean, val permissions: Array<String>, private val realCallback: Callback?): Parcelable {

    @IgnoredOnParcel
    val callback by lazy {
        object: Callback() {
            override fun onGrantPermissionStart(shouldGrantPerms: Array<String>, goon: () -> Unit): Boolean {
                return realCallback?.onGrantPermissionStart(shouldGrantPerms, goon) ?: false
            }
            override fun onShowRequestPermissionsRationale(rationalePerms: Array<String>, goon: () -> Unit): Boolean {
                return realCallback?.onShowRequestPermissionsRationale(rationalePerms, goon) ?: false
            }
            override fun onPermissionGranted(allGranted: Boolean, grantedPerms: Array<String>) {
                realCallback?.onPermissionGranted(allGranted, grantedPerms)
            }

            override fun onPermissionDenied(allDenied: Boolean, deniedPerms: Array<String>) {
                realCallback?.onPermissionDenied(allDenied, deniedPerms)
            }

            override fun onRequestCanceled(requestCode: Int) {
                realCallback?.onRequestCanceled(requestCode)
            }
        }
    }

    class Builder(private val requestManager: RequestManager) {
        private var requestCode = 100
        private var checkWithOps = false
        private var permissions: Array<String> by Delegates.notNull()

        fun requestCode(code: Int): Builder {
            this.requestCode = code
            return this
        }

        fun checkWithOps(ops: Boolean):Builder {
            checkWithOps = ops
            return this
        }

        fun permissions(vararg permissions: String): Builder {
            this.permissions = permissions.toList().toTypedArray()
            return this
        }

        fun check(callback: Callback? = null) {
            val request = Request(requestCode, checkWithOps, permissions, callback)
            requestManager.request(request)
        }
    }

}