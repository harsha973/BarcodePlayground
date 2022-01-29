package nz.co.sha.zxing

import android.Manifest
import android.os.Bundle
import android.util.Log
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import com.google.common.util.concurrent.ListenableFuture
import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import nz.co.sha.zxing.databinding.ActivityMlKitBinding

class MLKitActivity : AppCompatActivity() {

    private lateinit var cameraProviderFuture: ListenableFuture<ProcessCameraProvider>

    private val binding: ActivityMlKitBinding by lazy { ActivityMlKitBinding.inflate(layoutInflater) }

    private val requestPermission =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            if (isGranted) {
//                binding.previewView.resume()
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        cameraProviderFuture = ProcessCameraProvider.getInstance(this)
        cameraProviderFuture.addListener({
            val cameraProvider = cameraProviderFuture.get()
            bindPreview(cameraProvider)
        }, ContextCompat.getMainExecutor(this))
    }

    override fun onResume() {
        super.onResume()
        requestPermission.launch(Manifest.permission.CAMERA)
    }

    private fun bindPreview(cameraProvider: ProcessCameraProvider): Camera {
        val preview: Preview = Preview.Builder()
            .build().also { it.setSurfaceProvider(binding.previewView.surfaceProvider) }

        val cameraSelector: CameraSelector = CameraSelector.Builder()
            .requireLensFacing(CameraSelector.LENS_FACING_BACK)
            .build()

        val analyzer = YourImageAnalyzer {
            binding.resultTV.text = it
            lifecycleScope.launch {
                delay(5000)
                if(binding.resultTV.text == it)
                    binding.resultTV.text = ""
            }
        }
        val imageAnalysis = ImageAnalysis.Builder().build()
        imageAnalysis.setAnalyzer(ContextCompat.getMainExecutor(this), analyzer)

        return cameraProvider.bindToLifecycle(
            this as LifecycleOwner,
            cameraSelector,
            preview,
            imageAnalysis
        )
    }
}

private class YourImageAnalyzer(private val callback: (String?) -> Unit) : ImageAnalysis.Analyzer {
    @ExperimentalGetImage
    override fun analyze(imageProxy: ImageProxy) {
        val mediaImage = imageProxy.image
        if (mediaImage != null) {
            val image = InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)
            // Pass image to an ML Kit Vision API
            // ...

            val options = BarcodeScannerOptions.Builder()
                .setBarcodeFormats(Barcode.FORMAT_QR_CODE)
                .build()
            val scanner = BarcodeScanning.getClient(options)
            val result = scanner.process(image)
                .addOnSuccessListener { barcodes ->
                    Log.d("Barcodes", barcodes.toString())
                    if(barcodes.isNotEmpty())
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
}