package vn.com.rd.testhardwareapp.ui.activity

import android.Manifest
import android.Manifest.permission.RECORD_AUDIO
import android.Manifest.permission.WRITE_EXTERNAL_STORAGE
import android.content.pm.PackageManager
import android.media.MediaPlayer
import android.media.MediaRecorder
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import vn.com.rd.testhardwareapp.R
import vn.com.rd.testhardwareapp.databinding.ActivityMicrophoneBinding
import java.io.File
import java.io.IOException


class MicrophoneActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMicrophoneBinding
    private val RECORD_AUDIO_REQUEST_CODE = 1
    private var mMediaRecorder: MediaRecorder? = null
    private var mMediaPlayer: MediaPlayer? = null
    private var mIsRecording = false
    private var mIsPlaying = false
    private lateinit var mFile: File
    val permissions = arrayOf(RECORD_AUDIO)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityMicrophoneBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)
        val backArrowDrawable = ContextCompat.getDrawable(this, R.drawable.ic_arrow_left)
        setSupportActionBar(findViewById(R.id.tb_microphone))
        supportActionBar?.setDisplayHomeAsUpEnabled(true);
        supportActionBar?.setDisplayShowHomeEnabled(true);
        supportActionBar?.setHomeAsUpIndicator(backArrowDrawable)

        binding.playBtn.isEnabled = false
        mFile = File(this@MicrophoneActivity.filesDir, "TestYourAndroidMicTest.3gp")
        binding.recordBtn.setOnClickListener {
            if (mIsRecording) {
                stopRecording()
            } else {
                startRecording()
            }
        }
        binding.playBtn.setOnClickListener {
            if (mIsPlaying) {
                stopPlaying()
            } else {
                startPlaying()
            }
        }
        if (ContextCompat.checkSelfPermission(this, RECORD_AUDIO)
            != PackageManager.PERMISSION_GRANTED) {
            // Quyền chưa được cấp, yêu cầu quyền
            ActivityCompat.requestPermissions(this,
                arrayOf(RECORD_AUDIO),
                RECORD_AUDIO_REQUEST_CODE)
        } else {
            Log.i("MicrophoneActivity", "have RECORD_AUDIO permission")
        }
    }

    override fun onPause() {
        super.onPause()
        stopRecording()
        stopPlaying()
    }

    private fun startPlaying() {
        try {
            if (mFile.exists()) {
                mMediaPlayer = MediaPlayer()
                mMediaPlayer?.let { mMediaPlayer ->
                    mMediaPlayer.setDataSource(mFile.absolutePath)
                    mMediaPlayer.prepare()
                    mMediaPlayer.setOnCompletionListener {
                        stopPlaying()
                    }
                    mMediaPlayer.start()
                    binding.playBtn.setText(R.string.mic_stop)
                    binding.recordBtn.isEnabled = false
                    mIsPlaying = true
                } ?: run {
                    Toast.makeText(this@MicrophoneActivity, "Fail", Toast.LENGTH_SHORT).show()
                }
            }
            else {
                Log.e("MicrophoneActivity", "File does not exist: ${mFile.absolutePath}")
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(this@MicrophoneActivity, "Fail", Toast.LENGTH_SHORT).show()
        }
    }

    private fun startRecording() {
        try {
            mMediaRecorder = MediaRecorder()
            mMediaRecorder?.let { mMediaRecorder ->
                mMediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC)
                mMediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.AMR_NB)
                mMediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB)
                mMediaRecorder.setAudioEncodingBitRate(16)
                mMediaRecorder.setAudioSamplingRate(44100)
                mMediaRecorder.setOutputFile(mFile.absolutePath)
                mMediaRecorder.prepare()
                mMediaRecorder.start()
                binding.recordBtn.setText(R.string.mic_stop)
                binding.playBtn.isEnabled = false
                mIsRecording = true
                Log.i("MicrophoneActivity", "File path: ${mFile.absolutePath}")

            } ?: run {
                Toast.makeText(this@MicrophoneActivity, "Fail", Toast.LENGTH_SHORT).show()
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(this@MicrophoneActivity, "Fail", Toast.LENGTH_SHORT).show()
        }
    }

    private fun stopPlaying() {
        mMediaPlayer?.let { mMediaPlayer ->
            if (mIsPlaying) {
                mIsPlaying = false
                try {
                    mMediaPlayer.stop()
                } catch (ignored: Exception) {
                    ignored.printStackTrace()
                }
            }
//            mMediaPlayer.reset()
            mMediaPlayer.release()
        }
        binding.playBtn.isEnabled = true
        binding.recordBtn.isEnabled = true
        binding.playBtn.setText(R.string.mic_start_playing)
    }

    private fun stopRecording() {
        mMediaRecorder?.let { mMediaRecorder ->
            if (mIsRecording) {
                mIsRecording = false
                try {
                    mMediaRecorder.stop()
                } catch (ignored: Exception) {
                    ignored.printStackTrace()
                }
            }
//            mMediaRecorder.reset()
            mMediaRecorder.release()
        }
        binding.playBtn.isEnabled = true
        binding.recordBtn.isEnabled = true
        binding.recordBtn.setText(R.string.mic_start_recording)
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()  // Xử lý khi nhấn nút back
        return true
    }
}