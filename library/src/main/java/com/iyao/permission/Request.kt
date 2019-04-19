package com.iyao.permission

import android.os.Parcelable
import kotlinx.android.parcel.IgnoredOnParcel
import kotlinx.android.parcel.Parcelize
import kotlin.properties.Delegates

@Parcelize
class Request private constructor(val code: Int, val permissions: Array<String>, internal val callback: Callback): Parcelable {

    class Builder(private val requestManager: RequestManager) {
        private var requestCode = 100
        private var permissions = arrayOf<String>()

        fun requestCode(code: Int): Builder {
            this.requestCode = code
            return this
        }

        fun permissions(vararg permissions: String): Builder {
            this.permissions = permissions.toList().toTypedArray()
            return this
        }

        fun check(callback: Callback) {
            if (permissions.isEmpty()) {
                throw IllegalAccessException("permissions requested should not be empty.")
            }
            val request = Request(requestCode, permissions, callback)
            requestManager.request(request)
        }
    }

}