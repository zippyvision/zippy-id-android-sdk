package com.zippyid.zippydroid.network

import com.android.volley.VolleyError

interface AsyncResponse<in T> {
    fun onSuccess(response: T)
    fun onError(error: VolleyError)
}