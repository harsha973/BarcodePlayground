package nz.co.sha.zxing

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import nz.co.sha.zxing.databinding.ActivityLaunchBinding
import android.widget.Toast
import androidx.activity.result.ActivityResultCallback

import com.journeyapps.barcodescanner.ScanContract

import com.journeyapps.barcodescanner.ScanOptions

import androidx.activity.result.ActivityResultLauncher
import com.google.android.libraries.barhopper.RecognitionOptions.AZTEC
import com.google.zxing.BarcodeFormat
import com.journeyapps.barcodescanner.ScanIntentResult


class LaunchActivity : AppCompatActivity() {
    private val binding: ActivityLaunchBinding by lazy {
        ActivityLaunchBinding.inflate(layoutInflater)
    }

    private lateinit var barcodeLauncher: ActivityResultLauncher<ScanOptions>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        binding.zxingButton.setOnClickListener {
            val intent = Intent(this, ZXingActivity::class.java)
            startActivity(intent)
        }

        binding.mlKit.setOnClickListener {
            val intent = Intent(this, MLKitActivity::class.java)
            startActivity(intent)
        }

        register()
        binding.zxingExternalButton.setOnClickListener {
            barcodeLauncher.launch(ScanOptions().setDesiredBarcodeFormats(ScanOptions.QR_CODE, ScanOptions.DATA_MATRIX))
        }
    }

    private fun register() {
        barcodeLauncher = registerForActivityResult(
            ScanContract()
        ) { result: ScanIntentResult ->
            if (result.contents == null) {
                Toast.makeText(this, "Cancelled", Toast.LENGTH_LONG).show()
            } else {
                Toast.makeText(
                    this,
                    "Scanned: " + result.contents,
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }
}