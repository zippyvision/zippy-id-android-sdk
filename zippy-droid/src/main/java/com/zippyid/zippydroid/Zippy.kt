package com.zippyid.zippydroid
import com.zippyid.zippydroid.network.model.ZippyCallback

object Zippy {
    const val host = "https://demo.zippyid.com/api"
    lateinit var key: String
        private set
    lateinit var secret: String
        private set
    var customerUid: Int = -1
    var zippyCallback: ZippyCallback? = null

    @JvmStatic
    fun initialize(key: String, secret: String, customerUid: Int = -1) {
        this.key = key
        this.secret = secret
        this.customerUid = customerUid
    }

    @JvmStatic
    fun createCallback(callback: ZippyCallback) {
        this.zippyCallback = callback
    }
}