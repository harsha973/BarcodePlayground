package nz.co.sha.zxing

import android.Manifest
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.google.zxing.integration.android.IntentIntegrator
import android.widget.Toast

import com.google.zxing.integration.android.IntentResult

import android.content.Intent
import android.util.Log
import android.util.TimeUtils
import android.widget.FrameLayout
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.lifecycleScope
import com.google.zxing.BarcodeFormat
import com.google.zxing.client.android.BeepManager
import com.journeyapps.barcodescanner.BarcodeCallback
import com.journeyapps.barcodescanner.BarcodeResult
import com.journeyapps.barcodescanner.BarcodeView
import com.journeyapps.barcodescanner.DefaultDecoderFactory
import com.journeyapps.barcodescanner.camera.CameraSettings
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import nz.co.sha.zxing.databinding.ActivityMainBinding
import nz.co.sha.zxing.databinding.ActivityMlKitBinding
import timber.log.Timber
import java.util.*

class ZXingActivity : AppCompatActivity() {
    //    private var barcodeView: BarcodeView? = null
    private val binding: ActivityMainBinding by lazy { ActivityMainBinding.inflate(layoutInflater) }

    private val requestPermission =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            if (isGranted) {
                binding.barCodeView.resume()
            }
        }
    private var timeInSeconds = 0L
    private val allRecordedTimes = mutableListOf<Long>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        val beepManager = BeepManager(this)
//        barcodeView = BarcodeView(this)
        val formats = listOf(BarcodeFormat.QR_CODE, BarcodeFormat.AZTEC, BarcodeFormat.DATA_MATRIX)

        with(binding.barCodeView) {
           decoderFactory = DefaultDecoderFactory(formats)

            decodeContinuous { result ->
                timeInSeconds = (Date().time - timeInSeconds)/1000
                binding.barCodeView.pause()

                allRecordedTimes.add(timeInSeconds)
                val median = allRecordedTimes.fold(0L){ acc, l -> acc + l}/allRecordedTimes.size

                Timber.d(result.text)
                setResult("Current seconds is $timeInSeconds \n" +
                        "Median seconds is $median \n" +
                        "\n" +
                        "${result.text}")
                beepManager.playBeepSoundAndVibrate()
            }
            viewFinder.setLaserVisibility(false)

            cameraSettings.isAutoFocusEnabled = true
            cameraSettings.focusMode = CameraSettings.FocusMode.CONTINUOUS // th
            cameraSettings.isMeteringEnabled = true
            cameraSettings.isExposureEnabled = true

            binding.start.setOnClickListener {
                timeInSeconds = Date().time
                requestPermission.launch(Manifest.permission.CAMERA)
            }
        }
    }

//    override fun onResume() {
//        super.onResume()
//        requestPermission.launch(Manifest.permission.CAMERA)
//    }

    override fun onPause() {
        super.onPause()
        binding.barCodeView.pause()
    }

    private fun setResult(result: String) {
        binding.decodedText.text = result
        lifecycleScope.launch {
            delay(5000)
            if (binding.decodedText.text == result)
                binding.decodedText.text = ""
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        val result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data)
        if (result != null) {
            if (result.contents == null) {
                Toast.makeText(this, "Cancelled", Toast.LENGTH_LONG).show()
            } else {
                Toast.makeText(this, "Scanned: " + result.contents, Toast.LENGTH_LONG).show()
            }
        }
        super.onActivityResult(requestCode, resultCode, data)
    }
}