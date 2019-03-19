package com.gbazilio.qrcodescanner.pipeline

import android.hardware.camera2.CameraAccessException
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraManager
import android.util.Log
import java.util.concurrent.Callable
import java.util.concurrent.ExecutorService
import java.util.concurrent.Future

class GetScannerIdUseCase(private val executor: ExecutorService, private val cameraManager: CameraManager) : Callable<String>{

    companion object {
        private val TAG = javaClass.canonicalName
    }

    fun execute() : Future<String> = executor.submit(this)

    override fun call(): String {
        var cameraId = "-1"

        try {
            for (currentCameraId in cameraManager.cameraIdList) {
                val currentCharacteristics = cameraManager.getCameraCharacteristics(currentCameraId)

                if (currentCharacteristics[CameraCharacteristics.LENS_FACING]
                    != CameraCharacteristics.LENS_FACING_BACK
                ) continue

                cameraId = currentCameraId
            }
        } catch (e: CameraAccessException) {
            Log.e(TAG, e.message)
        } catch (e: IllegalArgumentException) {
            Log.e(TAG, e.message)
        } finally {
            return cameraId
        }
    }

}