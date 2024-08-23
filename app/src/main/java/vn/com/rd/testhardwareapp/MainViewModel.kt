package vn.com.rd.testhardwareapp

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class MainViewModel:ViewModel() {
    private val _liveDataMiniScreen = MutableLiveData<Int>()

    val miniScreen : LiveData<Int> = _liveDataMiniScreen

    fun updateMiniScreen(newMiniScreen : Int){
        Log.i("TAG", "updatelivedataMiniScreen to: $newMiniScreen")
        _liveDataMiniScreen.postValue(newMiniScreen)
    }
}