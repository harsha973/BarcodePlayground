package nz.co.sha.zxing

import android.Manifest
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.google.zxing.integration.android.IntentIntegrator
import android.widget.Toast

import com.google.zxing.integration.android.IntentResult

import android.content.Intent
import android.widget.FrameLayout
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import com.google.zxing.BarcodeFormat
import com.google.zxing.client.android.BeepManager
import com.journeyapps.barcodescanner.BarcodeCallback
import com.journeyapps.barcodescanner.BarcodeResult
import com.journeyapps.barcodescanner.BarcodeView
import com.journeyapps.barcodescanner.DefaultDecoderFactory

class MainActivity : AppCompatActivity() {
    private var barcodeView: BarcodeView? = null

    private val requestPermission =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            if (isGranted) {
                barcodeView?.resume()
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val beepManager = BeepManager(this)
        barcodeView = BarcodeView(this)
        val formats = listOf(BarcodeFormat.QR_CODE)//, BarcodeFormat.AZTEC, BarcodeFormat.DATA_MATRIX)
        barcodeView?.decoderFactory = DefaultDecoderFactory(formats)

        val callback = object : BarcodeCallback {
            override fun barcodeResult(result: BarcodeResult) {
                if (result.text == null) {
                    return
                }
                findViewById<TextView>(R.id.decodedText).text = result.text
                beepManager.playBeepSoundAndVibrate()
            }
        }
        barcodeView?.decodeContinuous(callback)
        findViewById<FrameLayout>(R.id.content_fl).addView(barcodeView)

//        IntentIntegrator(this).initiateScan()
    }

    override fun onResume() {
        super.onResume()
        requestPermission.launch(Manifest.permission.CAMERA)
    }

    override fun onPause() {
        super.onPause()
        barcodeView?.pause()
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