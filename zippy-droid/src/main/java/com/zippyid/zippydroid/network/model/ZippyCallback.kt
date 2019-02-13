package com.zippyid.zippydroid.network.model

interface ZippyCallback {
    fun onSubmit()
    fun onTextExtracted()
    fun onFinished()
}