package nz.co.sha.zxing

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import nz.co.sha.zxing.databinding.ActivityLaunchBinding

class LaunchActivity : AppCompatActivity() {
    private val binding: ActivityLaunchBinding by lazy {
        ActivityLaunchBinding.inflate(layoutInflater)
    }

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
    }
}