package vn.com.rd.testhardwareapp

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Switch
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class MainAdapter(private var categoryList: List<String>) :
    RecyclerView.Adapter<MainAdapter.MainViewHolder>() {
    class MainViewHolder(itemView : View) : RecyclerView.ViewHolder(itemView){
        val tvCategory = itemView.findViewById<TextView>(R.id.tv_category)
        val swSuccess = itemView.findViewById<Switch>(R.id.sw_success)
    }
    private var itemClickListener: OnItemClickListener? = null
    fun setOnItemClickListener(listener: OnItemClickListener?) {
        itemClickListener = listener
    }

    interface OnItemClickListener {
        fun onItemClick(position: Int)
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MainViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_main, parent, false)
        return MainViewHolder(view)
    }

    override fun getItemCount(): Int {
        return categoryList.size
    }

    override fun onBindViewHolder(holder: MainViewHolder, position: Int) {
        val category = categoryList[position]
        holder.tvCategory.text = category
        holder.itemView.setOnClickListener{
            itemClickListener?.onItemClick(position)
        }
        holder.swSuccess.setOnCheckedChangeListener{ _ , isChecked ->
            if(position == 0){
                Utils.sendTestBigScreenReport(if(isChecked) 0 else 1)
            }
            else if(position == 1){
                Utils.sendTestSmallScreenReport(if(isChecked) 0 else 1)
            }
            else if(position == 2){
                Utils.sendTestMicReport(if(isChecked) 0 else 1)
            }
            else if(position == 3){
                Utils.sendTestSpeakerReport(if(isChecked) 0 else 1)
            }
            else if(position == 4){
                Utils.sendTestTouchBigScreen(if(isChecked) 0 else 1)
            }
            else if(position == 5){
                Utils.sendTestPresenceReport(if(isChecked) 0 else 1)
            } else {}
//            Utils.sendTestReport()
        }
    }
}