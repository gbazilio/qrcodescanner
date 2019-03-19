package com.gbazilio.qrcodescanner.pipeline

import android.hardware.camera2.CameraAccessException
import android.hardware.camera2.CameraCaptureSession
import android.hardware.camera2.CameraDevice
import android.hardware.camera2.CaptureRequest
import android.os.Handler
import android.util.Log
import android.view.Surface
import com.gbazilio.qrcodescanner.utils.OnSessionConfigured
import java.util.concurrent.*

class CreateSessionUseCase(
    private val executor: ExecutorService,
    private val handler: Handler
) : Callable<CameraCaptureSession?> {

    companion object {
        private val TAG = this.javaClass.canonicalName
    }

    private lateinit var cameraDevice: CameraDevice
    private lateinit var request: CaptureRequest
    private lateinit var surfaces: List<Surface>

    fun execute(
        cameraDevice: CameraDevice,
        request: CaptureRequest,
        surfaces: List<Surface>
    ): Future<CameraCaptureSession?> {
        this.cameraDevice = cameraDevice
        this.request = request
        this.surfaces = surfaces
        return executor.submit(this)
    }

    override fun call(): CameraCaptureSession? {
        var captureSession: CameraCaptureSession? = null

        val lock = Semaphore(1)

        val lockAcquired = lock.tryAcquire()
        if (lockAcquired) {
            cameraDevice.createCaptureSession(
                surfaces,
                OnSessionConfigured { session ->
                    captureSession = session
                    try {
                        session.setRepeatingRequest(request, null, handler)
                    } catch (e: CameraAccessException) {
                        Log.e(TAG, e.message)
                    } finally {
                        lock.release()
                    }
                }, handler
            )
        }

        val lockAquired = lock.tryAcquire(10, TimeUnit.SECONDS)
        if (lockAquired) lock.release()

        return captureSession
    }
}