package com.gbazilio.qrcodescanner

import android.hardware.camera2.CameraCaptureSession
import android.media.ImageReader
import android.view.Surface
import com.gbazilio.qrcodescanner.pipeline.*
import java.util.concurrent.Future

class ScannerPresenter(private val streamType: StreamType,
                       private val getCharacteristicsUseCase: GetScannerIdUseCase,
                       private val getPreviewSizeUseCase: GetPreviewSizeUseCase,
                       private val openCameraUseCase: OpenCameraUseCase,
                       private val captureRequestUseCase: CreateCaptureRequestUseCase,
                       private val captureSessionUseCase: CreateSessionUseCase,
                       private val createProcessingBackendUseCase: CreateProcessingBackendUseCase,
                       private val subscriptionUseCase: ProcessingSurfaceSubscriptionUseCase
) {

    private var session: CameraCaptureSession? = null
    private var processingBackendFuture: Future<ImageReader>? = null
    private var previewSizeFuture: Future<PreviewSize>? = null

    fun pause() {
        session?.stopRepeating()
        session?.abortCaptures()
        session?.close()
    }

    fun start(surface: Surface, observer: (String) -> Unit) {
        val cameraId = getCharacteristicsUseCase.execute().get()
        previewSizeFuture = getPreviewSizeUseCase.execute(cameraId)

        val cameraDevice = openCameraUseCase.execute(cameraId).get()
        if (cameraDevice != null) {
            val previewSize = previewSizeFuture?.get()!!
            processingBackendFuture = createProcessingBackendUseCase.execute(previewSize)

            val processingSurface = processingBackendFuture?.get()?.surface!!
            val surfaces = listOf(surface, processingSurface)

            val captureRequest = captureRequestUseCase.execute(cameraDevice, surfaces).get()
            session = captureSessionUseCase.execute(cameraDevice, captureRequest, surfaces).get()

            subscriptionUseCase.execute(processingBackendFuture?.get(), previewSizeFuture?.get(), streamType, observer)
        }
    }

    fun subscribe(observer: (String) -> Unit) {
        subscriptionUseCase.execute(processingBackendFuture?.get(), previewSizeFuture?.get(), streamType, observer)
    }

}