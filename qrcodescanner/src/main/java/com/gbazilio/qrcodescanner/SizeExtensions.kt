package com.gbazilio.qrcodescanner

import android.util.Size

fun Array<Size>.minSize(defaultSize: Size) =
    filter { size -> size.width * size.height >= defaultSize.width * defaultSize.height }
        .minBy { size -> size.width * size.height } ?: defaultSize