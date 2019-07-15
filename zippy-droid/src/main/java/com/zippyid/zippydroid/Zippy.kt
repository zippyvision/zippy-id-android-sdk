package com.zippyid.zippydroid

import com.zippyid.zippydroid.network.model.ZippyCallback

object Zippy {
    const val host = "https://app.zippyid.com/api"
    lateinit var token: String
        private set
    var callback: ZippyCallback? = null
    var isInitialized = false

    @JvmStatic
    fun initialize(apiKey: String) {
        this.token = apiKey
        this.isInitialized = true
    }
}