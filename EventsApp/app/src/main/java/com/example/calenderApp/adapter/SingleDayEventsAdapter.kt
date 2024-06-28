package com.example.calenderApp.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.calenderApp.R
import com.example.calenderApp.databinding.ItemDateBinding
import com.example.calenderApp.response.SpecificDateEvents
import com.example.calenderApp.util.CalenderUtil

/**
 * @author Abhishek Mishra
 * Created 18/06/24
 * Used to display events for a specific date
 */
class SingleDayEventsAdapter(
    private val events: ArrayList<SpecificDateEvents>?,
    private val clickListener: EventListInteraction
) :
    RecyclerView.Adapter<SingleDayEventsAdapter.SingleDayEventsAdapter>() {

    private val currentDate by lazy { CalenderUtil().getCurrentDateTimeInIST() }

    /**
     * Create new views (invoked by the layout manager)
     */
    class SingleDayEventsAdapter(
        private val binding: ItemDateBinding,
        private val clickListener: EventListInteraction,
        private val currentDate: String
    ) :
        RecyclerView.ViewHolder(binding.root) {

        /**
         * Bind the data with the view (invoked by the layout manager)
         * @param event The data to be displayed by this view
         */
        fun bind(event: SpecificDateEvents?) {
            event ?: return
            binding.apply {

                tvDate.text = event.date

                if (event.date == currentDate) {
                    tvDate.background = ResourcesCompat.getDrawable(
                        root.resources,
                        R.drawable.current_date_bg,
                        null
                    )
                    tvDate.setPadding(5, 5, 5, 5)
                } else {
                    tvDate.background = null
                    tvDate.setPadding(0, 0, 0, 0)
                }

                layEvents.apply {
                    removeAllViews()
                    event.events?.forEach { event ->
                        val inflater = LayoutInflater.from(context)
                        val customView: View = inflater.inflate(R.layout.item_event, this, false)
                        customView.findViewById<TextView>(R.id.tv_summary).text = event.summary
                        customView.findViewById<TextView>(R.id.tv_time).text =
                            if (event.endDate.isNullOrBlank()) {
                                event.startDate
                            } else {
                                event.startDate + " - " + event.endDate
                            }
                        customView.setOnClickListener {
                            if (event.id?.isNotBlank() == true) {
                                clickListener.onEventItemClicked(event.id)
                            }
                        }
                        if (event.id.isNullOrBlank()) {
                            customView.background = null
                        } else {
                            customView.background = ContextCompat.getDrawable(
                                root.context,
                                R.drawable.card_bg
                            )
                        }
                        addView(customView)
                    }
                }
            }
        }
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SingleDayEventsAdapter {
        val binding = ItemDateBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return SingleDayEventsAdapter(binding, clickListener, currentDate)
    }

    override fun onBindViewHolder(holder: SingleDayEventsAdapter, position: Int) {
        holder.bind(events?.get(position))
    }

    override fun getItemCount(): Int {
        return events?.size ?: 0
    }

    // function used to reset the list or add new events
    fun addEvents(newEvents: List<SpecificDateEvents>?) {
        if (newEvents == null || events.isNullOrEmpty()) {
            events?.clear()
            newEvents?.let { events?.addAll(it) }
            notifyDataSetChanged()
            return
        }
        val startPosition = events.size
        events.addAll(newEvents)
        notifyItemRangeInserted(startPosition, newEvents.size)
    }
}

interface EventListInteraction {
    fun onEventItemClicked(eventId: String?)
}