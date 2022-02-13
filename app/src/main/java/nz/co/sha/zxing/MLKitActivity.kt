package nz.co.sha.zxing

import android.Manifest
import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.util.Rational
import android.util.Size
import android.view.MotionEvent
import android.view.View
import android.view.ViewTreeObserver
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.*
import androidx.camera.core.impl.ImageCaptureConfig
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.google.common.util.concurrent.ListenableFuture
import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import nz.co.sha.zxing.databinding.ActivityMlKitBinding
import timber.log.Timber
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

class MLKitActivity : AppCompatActivity() {

    private lateinit var cameraProviderFuture: ListenableFuture<ProcessCameraProvider>

    private val binding: ActivityMlKitBinding by lazy { ActivityMlKitBinding.inflate(layoutInflater) }

    private val requestPermission =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            if (isGranted) {
            }
        }

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        cameraProviderFuture = ProcessCameraProvider.getInstance(this)
        cameraProviderFuture.addListener({
            val cameraProvider = cameraProviderFuture.get()
            val camera = bindPreview(cameraProvider)
            binding.previewView.afterMeasured {
                lifecycleScope.launch {
                    repeatOnLifecycle(Lifecycle.State.STARTED) {
                        do {
                            setAutoFocus(camera)
                            delay(3000)
                        } while (true)
                    }
                }

//                binding.previewView.setOnTouchListener { _, event ->
//                    return@setOnTouchListener when (event.action) {
//                        MotionEvent.ACTION_DOWN -> {
//                            true
//                        }
//                        MotionEvent.ACTION_UP -> {
//                            val factory: MeteringPointFactory = SurfaceOrientedMeteringPointFactory(
//                                binding.previewView.width.toFloat(), binding.previewView.height.toFloat()
//                            )
//                            val autoFocusPoint = factory.createPoint(event.x, event.y)
//                            try {
//                                Log.d("MlKit", "Focussing")
//                                camera.cameraControl.startFocusAndMetering(
//                                    FocusMeteringAction.Builder(
//                                        autoFocusPoint,
//                                        FocusMeteringAction.FLAG_AF
//                                    ).apply {
//                                        //focus only when the user tap the preview
//                                        disableAutoCancel()
//                                    }.build()
//                                )
//                            } catch (e: CameraInfoUnavailableException) {
//                                Log.d("ERROR", "cannot access camera", e)
//                            }
//                            true
//                        }
//                        else -> false // Unhandled event.
//                    }
//                }
            }
        }, ContextCompat.getMainExecutor(this))


    }

    override fun onResume() {
        super.onResume()
        requestPermission.launch(Manifest.permission.CAMERA)
    }

    private fun bindPreview(cameraProvider: ProcessCameraProvider): Camera {
        val preview: Preview = Preview.Builder()
            .build().also {
                it.setSurfaceProvider(binding.previewView.surfaceProvider)
            }

        val cameraSelector: CameraSelector = CameraSelector.Builder()
            .requireLensFacing(CameraSelector.LENS_FACING_BACK)
            .build()

        val analyzer = YourImageAnalyzer {
            binding.resultTV.text = it
            lifecycleScope.launch {
                delay(5000)
                if (binding.resultTV.text == it)
                    binding.resultTV.text = ""
            }
        }

//        val imageCaptureConfig = ImageCaptureConfig.Builder()
//            .setLensFacing(BACK)
//            .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
//            .setTargetResolution(Size(1200, 1600))
//            .setTargetAspectRatio(Rational(3,4))
//            .build()

//        val imageCapture = ImageCapture.Builder()
//            .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
//            .setTargetResolution(android.util.Size(1980, 1024))
//            .build()

        val imageAnalysis = ImageAnalysis
            .Builder()
            .setOutputImageFormat(ImageAnalysis.OUTPUT_IMAGE_FORMAT_YUV_420_888)
//            .setTargetResolution(android.util.Size(1080, 1920))
            .build()
        imageAnalysis.setAnalyzer(Executors.newSingleThreadExecutor(), analyzer)

        return cameraProvider.bindToLifecycle(
            this as LifecycleOwner,
            cameraSelector,
            preview,
//            imageCapture,
            imageAnalysis,
        )
    }

    private fun setAutoFocus(camera: Camera) {
        val autoFocusPoint = SurfaceOrientedMeteringPointFactory(1f, 1f)
            .createPoint(.5f, .5f)
        try {
            val autoFocusAction = FocusMeteringAction.Builder(
                autoFocusPoint,
                FocusMeteringAction.FLAG_AF
            ).apply {
                //  start cancel auto-focusing after 2 seconds
//                disableAutoCancel()
                setAutoCancelDuration(2, TimeUnit.SECONDS)
            }.build()
            camera.cameraControl.startFocusAndMetering(autoFocusAction)
        } catch (e: CameraInfoUnavailableException) {
            Timber.e("cannot access camera")
        }
    }
}

private class YourImageAnalyzer(private val callback: (String?) -> Unit) : ImageAnalysis.Analyzer {
    val options = BarcodeScannerOptions.Builder()
        .setBarcodeFormats(Barcode.FORMAT_QR_CODE)
        .build()
    val scanner = BarcodeScanning.getClient(options)

    @ExperimentalGetImage
    override fun analyze(imageProxy: ImageProxy) {
        val mediaImage = imageProxy.image ?: kotlin.run {
            imageProxy.close()
            return
        }

        val image = InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)
        // Pass image to an ML Kit Vision API
        // ...

        val result = scanner.process(image)
            .addOnSuccessListener { barcodes ->
//                    Log.d("Barcodes", barcodes.toString())
                if (barcodes.isNotEmpty())
                    callback(barcodes.first().rawValue)
            }
            .addOnFailureListener {
                // Task failed with an exception
                // ...
            }
            .addOnCompleteListener {
                imageProxy.close()
            }
    }
}
//
//inline fun View.afterMeasured(crossinline block: () -> Unit) {
//    if (measuredWidth > 0 && measuredHeight > 0) {
//        block()
//    } else {
//        viewTreeObserver.addOnGlobalLayoutListener(object :
//            ViewTreeObserver.OnGlobalLayoutListener {
//            override fun onGlobalLayout() {
//                if (measuredWidth > 0 && measuredHeight > 0) {
//                    viewTreeObserver.removeOnGlobalLayoutListener(this)
//                    block()
//                }
//            }
//        })
//    }
//}