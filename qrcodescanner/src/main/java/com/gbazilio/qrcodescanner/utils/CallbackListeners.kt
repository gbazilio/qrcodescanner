package com.gbazilio.qrcodescanner.utils

import android.graphics.SurfaceTexture
import android.hardware.camera2.CameraCaptureSession
import android.hardware.camera2.CameraDevice
import android.view.TextureView

fun OnSurfaceAvailable(onSurfaceAvailable: (SurfaceTexture?) -> Unit) = object : TextureView.SurfaceTextureListener {
    override fun onSurfaceTextureSizeChanged(surface: SurfaceTexture?, width: Int, height: Int) {
    }

    override fun onSurfaceTextureUpdated(surface: SurfaceTexture?) {
    }

    override fun onSurfaceTextureDestroyed(surface: SurfaceTexture?): Boolean {
        return false
    }

    override fun onSurfaceTextureAvailable(surface: SurfaceTexture?, width: Int, height: Int) {
        onSurfaceAvailable(surface)
    }

}

fun OnCameraOpened(onCameraOpened: (CameraDevice) -> Unit) = object : CameraDevice.StateCallback() {
    override fun onOpened(camera: CameraDevice) {
        onCameraOpened(camera)
    }

    override fun onDisconnected(camera: CameraDevice) {
        camera.close()
    }

    override fun onError(camera: CameraDevice, error: Int) {
        camera.close()
    }
}

fun OnSessionConfigured(onSessionCaptured: (CameraCaptureSession) -> Unit) =
    object : CameraCaptureSession.StateCallback() {
        override fun onConfigureFailed(session: CameraCaptureSession) {}

        override fun onConfigured(session: CameraCaptureSession) {
            onSessionCaptured(session)
        }

    }