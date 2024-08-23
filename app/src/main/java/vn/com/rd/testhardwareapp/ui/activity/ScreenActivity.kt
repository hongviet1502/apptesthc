package vn.com.rd.testhardwareapp.ui.activity

import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.os.CountDownTimer
import android.view.LayoutInflater
import android.view.WindowManager
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import vn.com.rd.testhardwareapp.R
import vn.com.rd.testhardwareapp.Utils
import vn.com.rd.testhardwareapp.databinding.ActivityScreenBinding
import vn.com.rd.testhardwareapp.helpers.MColor
import vn.com.rd.testhardwareapp.helpers.MonitorHelper

class ScreenActivity : AppCompatActivity() {
    private lateinit var binding: ActivityScreenBinding
    private val colorList = arrayListOf<Any>()

    private var i = -1
    private val timer: CountDownTimer = object : CountDownTimer(1200000, 500) {
        override fun onFinish() = finish()
        override fun onTick(millisUntilFinished: Long) = changeColor()
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityScreenBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)
        Utils.setFullScreen(window)
        binding.colorView.setOnClickListener { finish() }

        colorList.addAll(MonitorHelper.getColorList(this))

    }

    override fun onPause() {
        timer.cancel()
        window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        super.onPause()
    }

    override fun onResume() {
        super.onResume()
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        openDialogTestMode()
    }

    private fun changeColor() {
        i++
        var selected = colorList.getOrNull(i)
        if (selected == null) {
            i = 0
            selected = colorList[i]
        }

        if (selected is MColor) {
            binding.colorView.setBackgroundColor(ContextCompat.getColor(this, selected.colorId))
        } else if (selected is GradientDrawable) {
            binding.colorView.background = selected
        }
    }

    private fun openDialogTestMode() {
        MaterialAlertDialogBuilder(this)
            .setTitle(R.string.ui_caution)
            .setMessage(R.string.color_test_message)
            .setCancelable(false)
            .setPositiveButton(R.string.ui_okay) { _, _ ->
                timer.start()
            }
            .setNegativeButton(R.string.ui_cancel) { _, _ ->
                finish()
            }
            .show()
    }
}