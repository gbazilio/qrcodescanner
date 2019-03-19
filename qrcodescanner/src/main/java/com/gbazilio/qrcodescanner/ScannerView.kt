package com.gbazilio.qrcodescanner

import android.arch.lifecycle.Lifecycle
import android.arch.lifecycle.LifecycleObserver
import android.arch.lifecycle.OnLifecycleEvent
import android.content.Context
import android.hardware.camera2.CameraManager
import android.os.Handler
import android.os.HandlerThread
import android.util.AttributeSet
import android.view.Surface
import android.view.TextureView
import com.gbazilio.qrcodescanner.pipeline.*
import com.gbazilio.qrcodescanner.utils.OnSurfaceAvailable
import java.util.concurrent.Executors

class ScannerView(context: Context, attributes: AttributeSet) : TextureView(context, attributes), LifecycleObserver {

    private val scannerPresenter: ScannerPresenter

    init {
        val cameraManager = context.getSystemService(Context.CAMERA_SERVICE) as CameraManager
        val scannerHandler = Handler(HandlerThread("${context.packageName}.qrcode_scanner_view_handler").also { it.start() }.looper)
        val executor = Executors.newSingleThreadExecutor()
        var streamType = StreamType.STREAM

        val getCharacteristicsUseCase = GetScannerIdUseCase(executor, cameraManager)
        val getPreviewSizeUseCase = GetPreviewSizeUseCase(executor, cameraManager)
        val openCameraUseCase =
            OpenCameraUseCase(executor, scannerHandler, context, cameraManager)
        val captureRequestUseCase = CreateCaptureRequestUseCase(executor)
        val captureSessionUseCase = CreateSessionUseCase(executor, scannerHandler)
        val createProcessingBackendUseCase =
            CreateProcessingBackendUseCase(executor, scannerHandler)
        val subscriptionUseCase =
            ProcessingSurfaceSubscriptionUseCase(executor, scannerHandler)

        context.theme.obtainStyledAttributes(attributes, R.styleable.ScannerView, 0, 0).apply {
            if (hasValue(R.styleable.ScannerView_streamType)) {
                val streamTypeAttribute = getInt(R.styleable.ScannerView_streamType, 0)
                streamType = StreamType.values().first { it.ordinal == streamTypeAttribute }
            }
        }.recycle()

        scannerPresenter = ScannerPresenter(streamType,
            getCharacteristicsUseCase,
            getPreviewSizeUseCase,
            openCameraUseCase,
            captureRequestUseCase,
            captureSessionUseCase,
            createProcessingBackendUseCase,
            subscriptionUseCase)

    }

    @OnLifecycleEvent(Lifecycle.Event.ON_PAUSE)
    fun pause() {
        scannerPresenter.pause()
    }

    fun start(observer: (String) -> Unit) {
        if (isAvailable) {
            scannerPresenter.start(Surface(surfaceTexture), observer)
        } else {
            surfaceTextureListener = OnSurfaceAvailable { surface ->
                scannerPresenter.start(Surface(surface), observer)
            }
        }
    }

    fun subscribe(observer: (String) -> Unit) {
        scannerPresenter.subscribe(observer)
    }

}