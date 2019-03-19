package com.gbazilio.qrcodescanner.pipeline

import android.util.Size

data class PreviewSize(
    private val _width: Int? = DEFAULT_SIZE.width,
    private val _height: Int? = DEFAULT_SIZE.height) {

    companion object {
        val DEFAULT_SIZE = Size(640, 480)
    }

    val width
        get() = _width ?: DEFAULT_SIZE.width

    val height
        get() = _height?: DEFAULT_SIZE.height
}