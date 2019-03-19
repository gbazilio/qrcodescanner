package com.gbazilio.qrcodescanner.pipeline

import android.graphics.ImageFormat
import android.media.ImageReader
import android.os.Handler
import java.util.concurrent.Callable
import java.util.concurrent.ExecutorService
import java.util.concurrent.Future

class CreateProcessingBackendUseCase(private val executor: ExecutorService,
                                     private val handler: Handler) : Callable<ImageReader> {

    private lateinit var previewSize: PreviewSize

    fun execute(previewSize: PreviewSize): Future<ImageReader> {
        this.previewSize = previewSize
        return executor.submit(this)
    }

    override fun call(): ImageReader = ImageReader.newInstance(
        previewSize.width,
        previewSize.height, ImageFormat.YUV_420_888, 1
    ).also { surface ->
        surface.setOnImageAvailableListener({
            val latestImage = it.acquireNextImage() ?: return@setOnImageAvailableListener
            latestImage.close()
        }, handler)
    }
}