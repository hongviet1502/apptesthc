package vn.com.rd.testhardwareapp.ui.activity

import android.content.Context
import android.content.Intent
import android.media.AudioManager
import android.media.MediaPlayer
import android.os.Build
import android.os.Bundle
import android.os.VibrationEffect
import android.os.Vibrator
import android.provider.Settings
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.SeekBar
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import vn.com.rd.testhardwareapp.R
import vn.com.rd.testhardwareapp.databinding.ActivitySoundBinding
import java.lang.String
import kotlin.math.max
import kotlin.math.min

class SoundActivity : AppCompatActivity() {
    private lateinit var binding : ActivitySoundBinding

    private var vibrateType = 0
    private var isRinging = false
    private var isVibrating = false
    private var mediaPlayer: MediaPlayer? = null
    private lateinit var vibratorService: Vibrator

    private val maxProgress = 100 // you can set it as you want

    private var minVolumeIndex = 0
    private var maxVolumeIndex: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivitySoundBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        val backArrowDrawable = ContextCompat.getDrawable(this, R.drawable.ic_arrow_left)
        setSupportActionBar(binding.tbSound)
        supportActionBar?.setDisplayHomeAsUpEnabled(true);
        supportActionBar?.setDisplayShowHomeEnabled(true);
        supportActionBar?.setHomeAsUpIndicator(backArrowDrawable)

        requestPermission(applicationContext)
//        stopAutoBrightness(applicationContext)

        binding.sbBrightness.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            var brightness = 0
            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                brightness = progress
                if (!hasWriteSettingsPermission(applicationContext)) {
                    changeWriteSettingsPermission(applicationContext)
                } else {
                    changeBrightness(progress, applicationContext)
                }
            }

            override fun onStartTrackingTouch(seekBar: SeekBar) {}
            override fun onStopTrackingTouch(seekBar: SeekBar) {}
        })

        val audioManager = getSystemService(AUDIO_SERVICE) as AudioManager
        maxVolumeIndex = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC)
        minVolumeIndex =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) audioManager.getStreamMinVolume(
                AudioManager.STREAM_MUSIC
            ) else 0
        // sync progress of seekbar and textview with current volume
        val currentVolume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC)
        val progress: Int = getSeekBarProgressFromVolume(currentVolume)
        binding.sbVolume.progress = progress
        binding.sbVolume.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if (fromUser) {
                    val volumeIndex = getRightVolume(progress)
                    audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, volumeIndex, 0)
                }
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {
            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {
                playSoundAtCurrentVolume(audioManager)
            }

        })

        init()
    }

    private fun init() {
        this@SoundActivity.let { context ->
            vibratorService = context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
            val array = resources.getStringArray(R.array.vibrate_string_array)
            val adapter = ArrayAdapter(
                context,
                android.R.layout.simple_spinner_item, array
            )
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
//            binding.vibrateSpinner.adapter = adapter
//            binding.vibrateSpinner.onItemSelectedListener =
//                object : AdapterView.OnItemSelectedListener {
//                    override fun onItemSelected(
//                        adapterView: AdapterView<*>?, view: View,
//                        position: Int, id: Long
//                    ) {
//                        vibrateType = position
//                        stopVibrate()
//                    }
//
//                    override fun onNothingSelected(arg0: AdapterView<*>?) {}
//                }
            setListener()
        }
    }

    private fun startVibrate() {
        when (vibrateType) {
            0 -> {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    vibratorService.vibrate(
                        VibrationEffect.createOneShot(
                            30000,
                            VibrationEffect.DEFAULT_AMPLITUDE
                        )
                    )
                } else {
                    @Suppress("DEPRECATION")
                    vibratorService.vibrate(30000)
                }
                binding.vibrateButton.setText(R.string.vibrate_stop_button)
                isVibrating = true
            }
            1 -> {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    vibratorService.vibrate(
                        VibrationEffect.createWaveform(
                            longArrayOf(100, 200, 100), 0
                        )
                    )
                } else {
                    @Suppress("DEPRECATION")
                    vibratorService.vibrate(longArrayOf(100, 200, 100), 0)
                }
                binding.vibrateButton.setText(R.string.vibrate_stop_button)
                isVibrating = true
            }
        }
    }

    private fun setListener() {
        binding.ringButton.setOnClickListener {
            if (isRinging) {
                stopPlayer()
            } else {
                startPlayer()
            }
        }
     binding.vibrateButton.setOnClickListener {
            if (isVibrating) {
                stopVibrate()
            } else {
                startVibrate()
            }
        }
    }

    override fun onPause() {
        if (isVibrating) {
            stopVibrate()
        }
        if (isRinging) {
            stopPlayer()
        }
        super.onPause()
    }

    private fun startPlayer() {
        try {
            mediaPlayer = MediaPlayer.create(this@SoundActivity, R.raw.testring)
            mediaPlayer?.let { mediaPlayer ->
                mediaPlayer.setOnCompletionListener { stopPlayer() }
                mediaPlayer.start()
                isRinging = true
                binding.ringButton.setText(R.string.ring_stop_button)
            }
        } catch (e: Exception) {
            Toast.makeText(this@SoundActivity, "ERROR", Toast.LENGTH_SHORT).show()
        }
    }

    private fun stopPlayer() {
        if (isRinging) {
            binding.ringButton.setText(R.string.ring_button)
            isRinging = false
            mediaPlayer?.let { mediaPlayer ->
                mediaPlayer.stop()
                mediaPlayer.release()
            }
        }
    }

    private fun stopVibrate() {
        vibratorService.cancel()
        binding.vibrateButton.setText(R.string.vibrate_button)
        isVibrating = false
    }

    private fun playSoundAtCurrentVolume(audioManager: AudioManager) {
        mediaPlayer?.release()
        mediaPlayer = MediaPlayer.create(this, R.raw.testring)
        mediaPlayer?.apply {
            setOnCompletionListener { release() }
            setAudioStreamType(AudioManager.STREAM_MUSIC)
            start()
        }
    }

    private fun getRightVolume(input: Int): Int {
        // Ensure that the input is within the valid range (0 to a maxValue)
        var input = input
        input = max(0.0, min(input.toDouble(), maxProgress.toDouble())).toInt()

        // Calculate the right volume index from minVolumeIndex(x) to maxVolumeIndex(y)
        val ratio = input.toDouble() / (maxProgress + 1)
        return Math.round(minVolumeIndex + (maxVolumeIndex - minVolumeIndex) * ratio).toInt()
    }

    private fun getSeekBarProgressFromVolume(value: Int): Int {
        // Calculate the reverse interpolated input based on the value
        val ratio = (value - minVolumeIndex).toDouble() / (maxVolumeIndex - minVolumeIndex)
        val result = Math.round(ratio * maxProgress).toInt()

        // Ensure that the result is within the valid range (0 to maxValue)
        return max(0.0, min(result.toDouble(), maxProgress.toDouble())).toInt()
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()  // Xử lý khi nhấn nút back
        return true
    }

    private fun requestPermission(context: Context) {
        if(!hasWriteSettingsPermission(context)){
            Log.i("TAG", "requestPermission: no permission")
            changeWriteSettingsPermission(context)
        }
    }

    private fun hasWriteSettingsPermission(context: Context): Boolean {
        var ret = true
        // Get the result from below code.
        ret = Settings.System.canWrite(context)
        return ret
    }

    // Start can modify system settings panel to let user change the write
    // settings permission.
    private fun changeWriteSettingsPermission(context: Context) {
        val intent = Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS)
        context.startActivity(intent)
    }
    private fun stopAutoBrightness(context: Context) {
        Settings.System.putInt(
            context.contentResolver,
            Settings.System.SCREEN_BRIGHTNESS_MODE,
            Settings.System.SCREEN_BRIGHTNESS_MODE_MANUAL
        )
    }
    private fun changeBrightness(brightness: Int, context: Context){
        stopAutoBrightness(context)
        Settings.System.putInt(
            applicationContext.contentResolver,
            Settings.System.SCREEN_BRIGHTNESS, brightness
        )
    }
}