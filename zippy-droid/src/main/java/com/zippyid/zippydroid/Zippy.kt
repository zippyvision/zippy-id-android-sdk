package com.zippyid.zippydroid

object Zippy {
    const val host = "https://app.zippyid.com/api"
    lateinit var key: String
        private set
    lateinit var secret: String
        private set
    internal var isInitialized = false

    @JvmStatic
    fun initialize(key: String, secret: String) {
        this.key = key
        this.secret = secret
        isInitialized = true
    }
}