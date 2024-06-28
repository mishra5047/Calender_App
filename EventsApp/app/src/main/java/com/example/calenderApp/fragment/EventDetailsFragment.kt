package com.example.calenderApp.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.calenderApp.databinding.FragmentEventDetailsBinding
import com.example.calenderApp.response.Resource
import com.example.calenderApp.util.CalenderUtil
import com.example.calenderApp.viewmodel.GetEventViewModel
import com.google.api.services.calendar.model.Event

/**
 * @author Abhishek Mishra
 * Created 19/06/24
 */
class EventDetailsFragment : Fragment() {

    companion object {
        const val TAG = "EventDetailsFragment"
        const val ARGS_EVENT_ID = "args_event_id"

        fun newInstance(bundle: Bundle? = null): EventDetailsFragment {
            return EventDetailsFragment().apply {
                arguments = bundle
            }
        }
    }

    private val eventId by lazy {
        arguments?.getString(ARGS_EVENT_ID)
    }

    private val viewmodel: GetEventViewModel by lazy { ViewModelProvider(requireActivity())[GetEventViewModel::class.java] }

    private var binding: FragmentEventDetailsBinding? = null
    private val calenderUtil by lazy { CalenderUtil() }


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentEventDetailsBinding.inflate(layoutInflater)
        return binding?.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        getEventData()
        setupObservers()
        setClickListeners()
    }

    private fun setClickListeners() {
        binding?.ivBack?.setOnClickListener {
            activity?.onBackPressedDispatcher?.onBackPressed()
        }
    }

    /**
     * Fetch event data from the viewmodel
     */
    private fun setupObservers() {
        viewmodel.eventDetailsLiveData.observe(viewLifecycleOwner) {
            when (it?.status) {
                Resource.Status.LOADING -> {
                    binding?.progress?.visibility = View.VISIBLE
                }

                Resource.Status.SUCCESS -> {
                    binding?.progress?.visibility = View.GONE
                    setUiForEvent(it.data)
                }

                Resource.Status.ERROR -> {
                    binding?.progress?.visibility = View.VISIBLE
                }

                null -> Unit
            }
        }
    }

    /**
     * set UI for event data
     * @param event event data to be displayed in the UI
     */
    private fun setUiForEvent(event: Event?) {
        binding?.apply {
            setTextWithVisibility(tvMeetingTitle, event?.summary + " : ")
            setTextWithVisibility(
                tvMeetingDate,
                calenderUtil.getDateInSingleLine(event?.start?.dateTime)
            )
            setTextWithVisibility(
                tvMeetingTime,
                calenderUtil.getTimeInIst(event?.start?.dateTime) + " - " + calenderUtil.getTimeInIst(
                    event?.end?.dateTime
                )
            )
            if (event?.reminders?.overrides?.get(0) != null) {
                tvReminderDetails.visibility = View.VISIBLE
                ivReminder.visibility = View.VISIBLE
                tvReminderDetails.text = event.reminders?.overrides?.get(0).toString()
            } else {
                tvReminderDetails.visibility = View.GONE
                ivReminder.visibility = View.GONE

            }
            setTextWithVisibility(tvOrganiserName, event?.organizer?.email, tvOrganiserTitle)
            setTextWithVisibility(tvLocationName, event?.location, tvLocationTitle)
        }
    }

    /**
     * Set text with visibility
     * @param textView text view to be updated
     * @param text text to be displayed
     * @param titleView title view to be updated
     * */
    private fun setTextWithVisibility(
        textView: TextView,
        text: String?,
        titleView: TextView? = null
    ) {
        if (!text.isNullOrBlank()) {
            textView.visibility = View.VISIBLE
            titleView?.visibility = View.VISIBLE
            textView.text = text
        } else {
            textView.visibility = View.GONE
            titleView?.visibility = View.GONE
        }
    }

    private fun getEventData() {
        viewmodel.getEventDetails(eventId)
    }

    override fun onDestroyView() {
        viewmodel.clearEventDetailsData()
        super.onDestroyView()
    }
}