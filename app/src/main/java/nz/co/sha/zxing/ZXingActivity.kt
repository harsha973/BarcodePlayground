package nz.co.sha.zxing

import android.Manifest
import android.os.Bundle
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.google.zxing.BarcodeFormat
import com.google.zxing.client.android.BeepManager
import com.journeyapps.barcodescanner.DefaultDecoderFactory
import com.journeyapps.barcodescanner.camera.CameraSettings
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import nz.co.sha.zxing.databinding.ActivityZxingBinding
import timber.log.Timber

class ZXingActivity : AppCompatActivity() {
    private val binding: ActivityZxingBinding by lazy { ActivityZxingBinding.inflate(layoutInflater) }

    private val requestPermission =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            if (isGranted) {
                binding.barCodeView.resume()
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        val beepManager = BeepManager(this)
        val formats = listOf(BarcodeFormat.QR_CODE, BarcodeFormat.AZTEC, BarcodeFormat.DATA_MATRIX)

        with(binding.barCodeView) {
            decoderFactory = DefaultDecoderFactory(formats)

            cameraSettings.isAutoFocusEnabled = true
            cameraSettings.focusMode = CameraSettings.FocusMode.CONTINUOUS // Tried AUTO, MACRO
            cameraSettings.isMeteringEnabled = true
            cameraSettings.isExposureEnabled = true

            decodeContinuous { result ->
                // SET result
                Timber.d(result.text)
                setResult(result.text)
                beepManager.playBeepSoundAndVibrate()
            }
        }
    }

    override fun onResume() {
        super.onResume()
        requestPermission.launch(Manifest.permission.CAMERA)
    }

    override fun onPause() {
        super.onPause()
        binding.barCodeView.pause()
    }

    private fun setResult(result: String) {
        binding.decodedText.text = result
        // CLEAR text after 5 secs
        lifecycleScope.launch {
            delay(5000)
            if (binding.decodedText.text == result)
                binding.decodedText.text = ""
        }
    }
}