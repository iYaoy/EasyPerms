package com.iyao.permission

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize
import kotlin.properties.Delegates

@Parcelize
class Request private constructor(val requestCode: Int, val permissions: Array<out String>, val callback: Callback): Parcelable {

    class Builder(private val requestManager: RequestManager) {
        private var requestCode: Int by Delegates.notNull()
        private var permissions: Array<out String> by Delegates.notNull()
        private var callback: Callback by Delegates.notNull()

        fun requestCode(code: Int): Builder {
            this.requestCode = code
            return this
        }

        fun permissions(vararg permissions: String): Builder {
            this.permissions = permissions
            return this
        }

        fun callback(callback: Callback): Builder {
            this.callback = callback
            return this
        }

        fun check() {
            val request = Request(requestCode, permissions, callback)
            requestManager.request(request)
        }
    }

}