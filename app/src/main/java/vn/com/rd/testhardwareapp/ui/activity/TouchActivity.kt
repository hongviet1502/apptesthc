package vn.com.rd.testhardwareapp.ui.activity

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import vn.com.rd.testhardwareapp.R
import vn.com.rd.testhardwareapp.databinding.ActivityTouchBinding


open class TouchActivity : AppCompatActivity() {
    private lateinit var binding: ActivityTouchBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityTouchBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val backArrowDrawable = ContextCompat.getDrawable(this, R.drawable.ic_arrow_left)
        setSupportActionBar(binding.tbTouch)
        supportActionBar?.setDisplayHomeAsUpEnabled(true);
        supportActionBar?.setDisplayShowHomeEnabled(true);
        supportActionBar?.setHomeAsUpIndicator(backArrowDrawable)
    }
    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()  // Xử lý khi nhấn nút back
        return true
    }
}