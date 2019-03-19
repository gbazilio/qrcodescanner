package com.gbazilio.qrcodescanner

import android.arch.lifecycle.Lifecycle
import android.arch.lifecycle.LifecycleObserver
import android.arch.lifecycle.OnLifecycleEvent
import android.content.Context
import android.graphics.SurfaceTexture
import android.hardware.camera2.CameraCaptureSession
import android.hardware.camera2.CameraManager
import android.media.ImageReader
import android.os.Handler
import android.os.HandlerThread
import android.util.AttributeSet
import android.view.Surface
import android.view.TextureView
import java.util.concurrent.Executors

class ScannerView(context: Context, attributes: AttributeSet) : TextureView(context, attributes), LifecycleObserver {

    private var streamType = StreamType.STREAM

    private var session: CameraCaptureSession? = null
    private var processingBackend: ImageReader? = null
    private var previewSize: PreviewSize? = null

    private val cameraManager = context.getSystemService(Context.CAMERA_SERVICE) as CameraManager

    private val executor = Executors.newSingleThreadExecutor()

    private val getCharacteristicsUseCase: GetScannerIdUseCase
    private val getPreviewSizeUseCase: GetPreviewSizeUseCase
    private val openCameraUseCase: OpenCameraUseCase
    private val captureRequestUseCase: CreateCaptureRequestUseCase
    private val captureSessionUseCase: CreateSessionUseCase
    private val createProcessingBackendUseCase: CreateProcessingBackendUseCase
    private val subscriptionUseCase: ProcessingSurfaceSubscriptionUseCase

    private val scannerHandler by lazy {
        Handler(HandlerThread("${context.packageName}.qrcode_scanner_view_handler").also { it.start() }.looper)
    }

    init {
        getCharacteristicsUseCase = GetScannerIdUseCase(executor, cameraManager)
        getPreviewSizeUseCase = GetPreviewSizeUseCase(executor, cameraManager)
        openCameraUseCase = OpenCameraUseCase(executor, scannerHandler, context, cameraManager)
        captureRequestUseCase = CreateCaptureRequestUseCase(executor)
        captureSessionUseCase = CreateSessionUseCase(executor, scannerHandler)
        createProcessingBackendUseCase = CreateProcessingBackendUseCase(executor, scannerHandler)
        subscriptionUseCase = ProcessingSurfaceSubscriptionUseCase(executor, scannerHandler)

        surfaceTextureListener = OnSurfaceAvailable { surface ->
            startUseCases(surface)
        }

        context.theme.obtainStyledAttributes(attributes, R.styleable.ScannerView, 0, 0).apply {
            if (hasValue(R.styleable.ScannerView_streamType)) {
                val streamTypeAttribute = getInt(R.styleable.ScannerView_streamType, 0)
                streamType = StreamType.values().first { it.ordinal == streamTypeAttribute }
            }
        }.recycle()
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_PAUSE)
    fun pause() {
        session?.stopRepeating()
        session?.abortCaptures()
        session?.close()
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_RESUME)
    fun start() {
        startUseCases(surfaceTexture)
    }

    fun subscribe(observer: (String) -> Unit) {
        processingBackend?.let {
            if (previewSize != null) {
                subscriptionUseCase.execute(it, previewSize!!, streamType, observer)
            }
        }
    }

    private fun startUseCases(surface: SurfaceTexture?) {
        if (surface == null) return
        val previewSurface = Surface(surface)

        val cameraId = getCharacteristicsUseCase.execute().get()
        val previewSizeFuture = getPreviewSizeUseCase.execute(cameraId)
        val cameraDevice = openCameraUseCase.execute(cameraId).get()

        if (cameraDevice != null) {
            val previewSize = previewSizeFuture.get()
            this.previewSize = previewSize
            processingBackend = createProcessingBackendUseCase.execute(previewSize).get()

            val processingSurface = processingBackend!!.surface
            val surfaces = listOf(previewSurface, processingSurface)

            val captureRequest = captureRequestUseCase.execute(cameraDevice, surfaces).get()
            session = captureSessionUseCase.execute(cameraDevice, captureRequest, surfaces).get()
        }
    }

}