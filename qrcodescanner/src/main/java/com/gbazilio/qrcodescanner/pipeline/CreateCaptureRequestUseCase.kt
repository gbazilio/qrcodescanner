package com.gbazilio.qrcodescanner.pipeline

import android.hardware.camera2.CameraDevice
import android.hardware.camera2.CaptureRequest
import android.view.Surface
import java.util.concurrent.Callable
import java.util.concurrent.ExecutorService
import java.util.concurrent.Future

class CreateCaptureRequestUseCase(private val executor: ExecutorService) : Callable<CaptureRequest> {
    companion object {
        private val TAG = this.javaClass.canonicalName
    }

    private lateinit var cameraDevice: CameraDevice
    private lateinit var surfaces: List<Surface>

    fun execute(cameraDevice: CameraDevice, surfaces: List<Surface>): Future<CaptureRequest> {
        this.cameraDevice = cameraDevice
        this.surfaces = surfaces
        return executor.submit(this)
    }

    override fun call(): CaptureRequest {
        val requestBuilder = cameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW)
        surfaces.forEach { requestBuilder.addTarget(it) }
        return requestBuilder.build()
    }
}