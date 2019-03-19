package com.gbazilio.qrcodescanner.pipeline

import android.graphics.ImageFormat
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraManager
import android.util.Log
import com.gbazilio.qrcodescanner.pipeline.PreviewSize.Companion.DEFAULT_SIZE
import com.gbazilio.qrcodescanner.utils.minSize
import java.util.concurrent.Callable
import java.util.concurrent.ExecutorService
import java.util.concurrent.Future

class GetPreviewSizeUseCase(
    private val executor: ExecutorService,
    private val cameraManager: CameraManager
) : Callable<PreviewSize> {

    companion object {
        private val TAG = this.javaClass.name
    }

    private lateinit var cameraId: String

    fun execute(cameraId: String): Future<PreviewSize> {
        this.cameraId = cameraId
        return executor.submit(this)
    }

    override fun call(): PreviewSize {
        try {
            val streamConfigurationMap = cameraManager
                .getCameraCharacteristics(cameraId)
                .get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP) ?: return PreviewSize()

            val outputSizes = streamConfigurationMap.getOutputSizes(ImageFormat.YUV_420_888)
            val minPreviewSize = outputSizes.minSize(DEFAULT_SIZE)

            return PreviewSize(minPreviewSize.width, minPreviewSize.height)

        } catch (e: IllegalArgumentException) {
            Log.e(TAG, e.message)
            return PreviewSize()
        }
    }
}