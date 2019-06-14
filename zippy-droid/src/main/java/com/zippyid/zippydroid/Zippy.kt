package com.zippyid.zippydroid
import com.zippyid.zippydroid.network.model.ZippyCallback

object Zippy {
    const val host = "https://app.zippyid.com/api"
    lateinit var token: String
        private set
    var customerUid: String = "1"
    var zippyOnSubmit: ZippyCallback? = null
    var zippyOnReceiveResponse: ZippyCallback? = null

    @JvmStatic
    fun initialize(token: String, customerUid: String, onSubmit: ZippyCallback, onReceiveResponse: ZippyCallback) {
        this.token = token
        this.customerUid = customerUid
        this.zippyOnSubmit = onSubmit
        this.zippyOnReceiveResponse = onReceiveResponse
    }
}