package com.zippyid.zippydroid
import com.zippyid.zippydroid.network.model.ZippyCallback

object Zippy {
    const val host = "https://app.zippyid.com/api"
    lateinit var key: String
        private set
    lateinit var secret: String
        private set
    var customerUid: Int = -1
    internal var isInitialized = false
    lateinit var zippyCallback: ZippyCallback

    @JvmStatic
    fun initialize(key: String, secret: String, customerUid: Int = -1) {
        this.key = key
        this.secret = secret
        this.customerUid = customerUid
        isInitialized = true
    }

    @JvmStatic
    fun createCallback(callback: ZippyCallback) {
        this.zippyCallback = callback
    }
}