package com.example.calenderApp.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.res.ResourcesCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.calenderApp.R
import com.example.calenderApp.databinding.ItemPillBinding

/**
 * @author Abhishek Mishra
 * Created 19/06/24
 */
class MonthsAdapter(
    private val months: List<Month>,
    private val monthListInteraction: MonthListInteraction,
    private var selectedMonthIndex : Int
) : RecyclerView.Adapter<MonthsAdapter.MonthsViewHolder>() {

    class MonthsViewHolder(
        private val binding: ItemPillBinding,
        private val monthsListInteraction: MonthListInteraction
    ) : RecyclerView.ViewHolder(binding.root) {
        fun bind(month: Month?, isSelectedMonth : Boolean) {
            month ?: return
            binding.pillTextView.text = month.monthName
            binding.root.setOnClickListener {
                monthsListInteraction.onMonthItemClick(month)
            }
            if (isSelectedMonth) {
                binding.root.background = ResourcesCompat.getDrawable(
                    binding.root.resources,
                    R.drawable.current_date_bg,
                    null
                )
            } else {
                binding.root.background = null
            }
        }
    }

    fun setSelectedMonthIndex(selected : Int){
        val oldSelection = selectedMonthIndex
        selectedMonthIndex = selected
        notifyItemChanged(oldSelection)
        notifyItemChanged(selected)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MonthsViewHolder {
        val binding = ItemPillBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return MonthsViewHolder(binding, monthListInteraction)
    }

    override fun getItemCount(): Int {
        return months.size
    }

    override fun onBindViewHolder(holder: MonthsViewHolder, position: Int) {
        holder.bind(months[position], position == selectedMonthIndex)
    }
}

interface MonthListInteraction {
    fun onMonthItemClick(month: Month)
}

data class Month(
    val monthName: String = "",
    val position: Int = 0
)