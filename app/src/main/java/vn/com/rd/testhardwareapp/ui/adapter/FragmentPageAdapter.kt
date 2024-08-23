package vn.com.rd.testhardwareapp.ui.adapter

import android.util.Log
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle
import androidx.viewpager2.adapter.FragmentStateAdapter
import vn.com.rd.testhardwareapp.ui.fragment.FifthFragment
import vn.com.rd.testhardwareapp.ui.fragment.FirstFragment
import vn.com.rd.testhardwareapp.ui.fragment.FouthFragment
import vn.com.rd.testhardwareapp.ui.fragment.SecondFragment
import vn.com.rd.testhardwareapp.ui.fragment.SixthFragment
import vn.com.rd.testhardwareapp.ui.fragment.ThirdFragment

class FragmentPageAdapter(
    fragmentManager: FragmentManager,
    lifecycle: Lifecycle
) : FragmentStateAdapter(fragmentManager, lifecycle) {

    // Danh sách các fragment, bao gồm cả fragment mới sẽ được chèn
    private val fragments: MutableList<Fragment> = mutableListOf(
        FirstFragment(),
        SecondFragment(),
        ThirdFragment(),
        FouthFragment(),
        FifthFragment(),
        SixthFragment()
    )

    override fun getItemId(position: Int): Long {
        return fragments[position].hashCode().toLong()
    }

    override fun containsItem(itemId: Long): Boolean {
        return fragments.any { fragment ->
            fragment.hashCode().toLong() == itemId
        }
    }

    override fun getItemCount(): Int {
        return fragments.size
    }

    override fun createFragment(position: Int): Fragment {
        return fragments[position]
    }

    // Hàm để chèn một fragment mới vào vị trí được chỉ định
    fun insertFragment(newFragment: Fragment): Int? {
        if(!fragments.contains(newFragment)){
            fragments.add(fragments.size -1 , newFragment)
            Log.i("TAG", "inserted Fragment: ")
            Log.i("TAG", " Fragments: $${fragments}")
            notifyItemInserted(fragments.size -1) // Cập nhật giao diện
            return fragments.size -2
        }
        return null
    }

    // Hàm để xóa fragment ở vị trí được chỉ định
    fun removeFragment(position: Int) {
        fragments.removeAt(position)
        notifyItemRemoved(position) // Cập nhật giao diện
    }

    fun removeFragment(fragmentToRemove: Fragment) {
        val position = fragments.indexOf(fragmentToRemove)
        if (position != -1) {
            fragments.removeAt(position)
            notifyItemRemoved(position) // Cập nhật giao diện
        }
    }

    fun getFragmentAtPosition(position: Int): Fragment {
        return fragments[position]
    }
}
