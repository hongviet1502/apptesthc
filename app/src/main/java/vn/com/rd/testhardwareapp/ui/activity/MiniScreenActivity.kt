package vn.com.rd.testhardwareapp.ui.activity

import android.os.Bundle
import android.util.Log
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.Observer
import androidx.viewpager2.widget.ViewPager2
import vn.com.rd.testhardwareapp.MainActivity
import vn.com.rd.testhardwareapp.MainViewModel
import vn.com.rd.testhardwareapp.R
import vn.com.rd.testhardwareapp.databinding.ActivityMiniScreenBinding
import vn.com.rd.testhardwareapp.databinding.ActivitySensorBinding
import vn.com.rd.testhardwareapp.ui.adapter.FragmentPageAdapter

class MiniScreenActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMiniScreenBinding
    private val viewModel: MainViewModel by viewModels()

    companion object {
        const val TAG = "MainActivity"
        private lateinit var viewPager2Adapter: FragmentPageAdapter
        private var instance: MiniScreenActivity? = null

        fun getViewPager2Adapter(): FragmentPageAdapter {
            return viewPager2Adapter
        }
        fun getInstance(): MiniScreenActivity? {
            return instance
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityMiniScreenBinding.inflate(layoutInflater)
        val view = binding.root
        instance = this
        setContentView(view)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val backArrowDrawable = ContextCompat.getDrawable(this, R.drawable.ic_arrow_left)
        setSupportActionBar(binding.tbMiniScreen)
        supportActionBar?.setDisplayHomeAsUpEnabled(true);
        supportActionBar?.setDisplayShowHomeEnabled(true);
        supportActionBar?.setHomeAsUpIndicator(backArrowDrawable)

        val miniScreenIndex = viewModel.miniScreen.value

        val miniScreenObserver = Observer<Int> { newMiniScreen ->
            Log.i(TAG, "on change miniscrenn: $newMiniScreen")
            binding.mainViewPager2.setCurrentItem(newMiniScreen-1, true)
        }

        // Observe the LiveData, passing in this activity as the LifecycleOwner and the observer.
        viewModel.miniScreen.observe(this, miniScreenObserver)
        viewPager2Adapter = FragmentPageAdapter(supportFragmentManager, lifecycle)
        binding.mainViewPager2.adapter = viewPager2Adapter
        if (miniScreenIndex != null) {
            binding.mainViewPager2.setCurrentItem(miniScreenIndex-1, true)
        }
        binding.mainViewPager2.registerOnPageChangeCallback(object :
            ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                Log.i(TAG, "onPageSelected: $position")
                Log.i(TAG, "livedata miniscreen:" + viewModel.miniScreen.value)
                super.onPageSelected(position)
            }
        })
    }
    fun updateMiniScreen(newMiniScreen: Int) {
        viewModel.updateMiniScreen(newMiniScreen)
    }
    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()  // Xử lý khi nhấn nút back
        return true
    }
}