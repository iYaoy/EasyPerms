package com.iyao.permission

import androidx.fragment.app.FragmentActivity

class RequestManager private constructor(private val requestFragment: RequestFragment) {

    companion object {
        private const val TAG_FRAGMENT = "com.iyao.permission.RequestManager:FRAGMENT"
        fun get(activity: FragmentActivity): Request.Builder {
            val fragmentManager = activity.supportFragmentManager
            var requestFragment = fragmentManager.findFragmentByTag(TAG_FRAGMENT) as? RequestFragment
            if (requestFragment == null) {
                requestFragment = RequestFragment()
            }
            var requestManager = requestFragment.requestManager
            if (requestManager == null) {
                requestManager = RequestManager(requestFragment)
                requestFragment.requestManager = requestManager
            }
            if (!fragmentManager.isStateSaved && !fragmentManager.isDestroyed) {
                if (requestFragment.isDetached || !requestFragment.isAdded) {
                    val transaction = fragmentManager.beginTransaction()
                    transaction.add(requestFragment, TAG_FRAGMENT)
                    transaction.commitAllowingStateLoss()
                }
            }
            return Request.Builder(requestManager)
        }
    }

    fun requestOnStart() {
        requestFragment.requestOnStart()
    }

    fun requestOnShowRationale() {
        requestFragment.requestOnShowRationale()
    }

    internal fun request(request: Request) {
        requestFragment.request(request)
    }
}