package com.gbazilio.qrcodescanner.pipeline

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.hardware.camera2.CameraAccessException
import android.hardware.camera2.CameraDevice
import android.hardware.camera2.CameraManager
import android.os.Build
import android.os.Handler
import android.util.Log
import com.gbazilio.qrcodescanner.utils.OnCameraOpened
import java.util.concurrent.*

class OpenCameraUseCase(private val executor: ExecutorService,
                        private val handler: Handler,
                        private val context: Context,
                        private val cameraManager: CameraManager): Callable<CameraDevice?> {
    companion object {
        private val TAG = this.javaClass.canonicalName
    }

    private lateinit var cameraId: String

    fun execute(cameraId: String): Future<CameraDevice?> {
        this.cameraId = cameraId
        return executor.submit(this)
    }

    override fun call(): CameraDevice? {
        var cameraDevice: CameraDevice? = null

        val lock = Semaphore(1)

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (context.checkSelfPermission(Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
                    val lockAcquired = lock.tryAcquire()
                    if (lockAcquired) {
                        cameraManager.openCamera(cameraId, OnCameraOpened {
                            cameraDevice = it
                            lock.release()
                        }, handler)
                    }
                }
            } else {
                cameraManager.openCamera(cameraId,
                    OnCameraOpened { cameraDevice = it }, handler)
            }
        } catch (e: CameraAccessException) {
            Log.e(TAG, e.message)
        }

        val lockAquired = lock.tryAcquire(10, TimeUnit.SECONDS)
        if (lockAquired) lock.release()

        return cameraDevice
    }

}