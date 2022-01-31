package nz.co.sha.zxing

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import nz.co.sha.zxing.databinding.ActivityKBarcodeBinding

class KBarcodeActivity : AppCompatActivity() {
    private val binding: ActivityKBarcodeBinding by lazy { ActivityKBarcodeBinding.inflate(layoutInflater) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        lifecycle.addObserver(binding.viewBarcode)

        binding.viewBarcode.barcodes.observe(this, Observer { barcodes ->
            if(barcodes.isNotEmpty()) {
                binding.kbarcodeTV.text = barcodes.first().rawValue
                lifecycleScope.launch {
                    delay(5000)
                    if(binding.kbarcodeTV.text == barcodes.first().rawValue)
                        binding.kbarcodeTV.text = ""
                }
            }

        })
    }
}