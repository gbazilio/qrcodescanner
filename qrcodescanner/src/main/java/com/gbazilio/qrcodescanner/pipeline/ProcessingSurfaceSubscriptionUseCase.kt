package com.gbazilio.qrcodescanner.pipeline

import android.media.ImageReader
import android.os.Handler
import com.google.firebase.ml.vision.FirebaseVision
import com.google.firebase.ml.vision.barcode.FirebaseVisionBarcode
import com.google.firebase.ml.vision.barcode.FirebaseVisionBarcodeDetectorOptions
import com.google.firebase.ml.vision.common.FirebaseVisionImage
import com.google.firebase.ml.vision.common.FirebaseVisionImageMetadata
import java.util.concurrent.Callable
import java.util.concurrent.ExecutorService
import java.util.concurrent.Future

enum class StreamType {
    STREAM, SINGLE
}

class ProcessingSurfaceSubscriptionUseCase(
    private val executor: ExecutorService,
    private val handler: Handler
) : Callable<Boolean> {

    private var surface: ImageReader? = null
    private var previewSize: PreviewSize?= null
    private lateinit var streamType: StreamType
    private lateinit var notifyObserver: (String) -> Unit

    private val metadata by lazy {
        FirebaseVisionImageMetadata.Builder()
            .setWidth(previewSize!!.width)
            .setHeight(previewSize!!.height)
            .setFormat(FirebaseVisionImageMetadata.IMAGE_FORMAT_YV12)
            .build()
    }

    private val detector by lazy {
        val options = FirebaseVisionBarcodeDetectorOptions.Builder()
            .setBarcodeFormats(FirebaseVisionBarcode.FORMAT_QR_CODE)
            .build()
        FirebaseVision.getInstance().getVisionBarcodeDetector(options)
    }

    fun execute(
        surface: ImageReader?,
        previewSize: PreviewSize?,
        streamType: StreamType,
        observer: (String) -> Unit
    ): Future<Boolean> {
        this.surface = surface
        this.previewSize = previewSize
        this.notifyObserver = observer
        this.streamType = streamType
        return executor.submit(this)
    }

    override fun call(): Boolean {
        if (surface == null || previewSize == null) return false

        surface!!.setOnImageAvailableListener({
            val latestImage = it.acquireNextImage() ?: return@setOnImageAvailableListener

            val plane = latestImage.planes[0]
            if (plane != null) {
                val byteBuffer = plane.buffer

                val image = FirebaseVisionImage.fromByteBuffer(byteBuffer, metadata)
                detector.detectInImage(image)
                    .addOnSuccessListener { barcodes ->
                        for (barcode in barcodes) {
                            val rawValue = barcode.rawValue
                            rawValue?.let(notifyObserver)
                        }

                        if (barcodes.isNotEmpty() && streamType == StreamType.SINGLE) {
                            notifyObserver = { }
                        }

                    }
            }

            latestImage.close()
        }, handler)

        return true
    }

}