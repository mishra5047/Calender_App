package com.example.calenderApp.fragment

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.os.Bundle
import android.util.Patterns
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.calenderApp.R
import com.example.calenderApp.databinding.FragmentAddEventBinding
import com.example.calenderApp.response.Resource
import com.example.calenderApp.util.CalenderUtil
import com.example.calenderApp.viewmodel.GetEventViewModel
import com.google.api.client.util.DateTime
import com.google.api.services.calendar.model.Event
import com.google.api.services.calendar.model.EventAttendee
import com.google.api.services.calendar.model.EventDateTime
import java.util.Calendar

/**
 * @author Abhishek Mishra
 * Created 20/06/24
 */
class AddEventFragment : Fragment() {

    companion object {
        const val TAG = "AddEventFragment"
        fun newInstance(bundle: Bundle? = null): AddEventFragment {
            return AddEventFragment().apply {
                arguments = bundle
            }
        }
    }

    private val viewmodel: GetEventViewModel by lazy { ViewModelProvider(requireActivity())[GetEventViewModel::class.java] }

    private var binding: FragmentAddEventBinding? = null

    private val calenderUtil by lazy { CalenderUtil() }

    private val listOfAttendees = arrayListOf<String>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentAddEventBinding.inflate(layoutInflater)
        return binding?.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupInitialUI()
        setUpClickInteractions()
        setupObservers()
    }

    /**
     * Set up click interactions for date, time, and attendees
     */
    private fun setupObservers() {
        viewmodel.addEventLiveData.observe(viewLifecycleOwner) {
            when (it?.status) {
                Resource.Status.LOADING -> {
                    binding?.progress?.visibility = View.VISIBLE
                }

                Resource.Status.SUCCESS -> {
                    binding?.progress?.visibility = View.GONE
                    Toast.makeText(
                        requireContext(),
                        "Event Added successfully",
                        Toast.LENGTH_SHORT
                    ).show()
                    (activity)?.onBackPressedDispatcher?.onBackPressed()
                }

                Resource.Status.ERROR -> {
                    binding?.progress?.visibility = View.GONE
                }

                null -> Unit
            }
        }
    }

    /**
     * Check if all input fields are valid
     */
    private fun setupInitialUI() {
        binding?.apply {
            tvDateStart.text = calenderUtil.getCurrentDateFormatted()
            tvDateEnd.text = calenderUtil.getCurrentDateFormatted()
            tvTimeStart.text = calenderUtil.getCurrentTimeFormatted()
            tvTimeEnd.text = calenderUtil.getCurrentTimePlusOneHour()
        }
    }

    /**
     * setup Click listeners for date, time, and attendees
     */
    private fun setUpClickInteractions() {
        binding?.apply {
            btnSave.setOnClickListener {
                if (areAllInputFieldsValid()) {
                    // add event here
                    val event = getEventRequestData()
                    if (event != null) {
                        if (viewmodel.addEventLiveData.value?.status == Resource.Status.LOADING) {
                            Toast.makeText(
                                requireContext(),
                                "Please wait",
                                Toast.LENGTH_SHORT
                            ).show()
                        } else {
                            viewmodel.addEvent(event = event)
                        }
                    }
                }
            }
            tvDateStart.setOnClickListener {
                showCalenderAndSetResult(tvDateStart)
            }
            tvDateEnd.setOnClickListener {
                showCalenderAndSetResult(tvDateEnd)
            }
            tvTimeStart.setOnClickListener {
                showTimePickerAndSetResult(tvTimeStart)
            }
            tvTimeEnd.setOnClickListener {
                showTimePickerAndSetResult(tvTimeEnd)
            }
            // Add attendees
            btnAddAttendee.setOnClickListener {
                val attendeeInput = binding?.etAttendees?.text
                if (!isValidEmail(attendeeInput.toString())) {
                    val errorMessage = "Please enter valid email"
                    Toast.makeText(requireContext(), errorMessage, Toast.LENGTH_SHORT)
                        .show()
                    binding?.etAttendees?.error = errorMessage
                } else {
                    listOfAttendees.add(attendeeInput.toString())
                    val attendeeView: View =
                        layoutInflater.inflate(R.layout.item_attendee, binding?.root, false)
                    attendeeView.findViewById<TextView>(R.id.tv_title).text = attendeeInput
                    attendeeView.findViewById<ImageView>(R.id.iv_cross).setOnClickListener {
                        listOfAttendees.remove(attendeeInput.toString())
                        binding?.root?.removeView(attendeeView)
                    }
                    binding?.root?.addView(attendeeView)
                    binding?.etAttendees?.setText("")
                }
            }
        }
    }

    /**
     * Show date picker dialog and set selected date as result in the text view.
     */
    private fun isValidEmail(email: String?): Boolean {
        // Define a regex pattern for a valid email address
        return Patterns.EMAIL_ADDRESS.matcher(email.toString()).matches()
    }

    /**
     * Show date picker dialog and set selected date as result in the text view.
     */
    private fun getEventRequestData(): Event? {
        val title = binding?.etTitle?.text.toString()
        val location = binding?.etLocation?.text.toString()
        val startDate = binding?.tvDateStart?.text.toString()
        val startTime = binding?.tvTimeStart?.text.toString()
        val endDate = binding?.tvDateEnd?.text.toString()
        val endTime = binding?.tvTimeEnd?.text.toString()

        val startDateTime = calenderUtil.convertTimeToDateTime(startDate, startTime)
        if (startDateTime == null) {
            Toast.makeText(requireContext(), "Invalid start date format", Toast.LENGTH_SHORT).show()
            return null
        }
        val endDateTime = calenderUtil.convertTimeToDateTime(endDate, endTime)
        if (endDateTime == null) {
            Toast.makeText(requireContext(), "Invalid end date format", Toast.LENGTH_SHORT).show()
            return null
        }
        return Event().apply {
            summary = title
            if (location.isNotBlank()) {
                this.location = location
            }
            startDateTime.let {
                start = EventDateTime().apply {
                    dateTime = DateTime(it.toString())
                }
            }

            endDateTime.let {
                end = EventDateTime().apply {
                    dateTime = DateTime(it.toString())
                }
            }

            attendees = listOfAttendees.map { email ->
                EventAttendee().setEmail(email)
            }
        }
    }

    /**
     * Show time picker dialog and set selected time as result in the text view.
     * @param textView The text view where the selected time will be displayed.
     *  */
    private fun showTimePickerAndSetResult(textView: TextView) {
        val calendar = Calendar.getInstance()
        val hour = calendar.get(Calendar.HOUR_OF_DAY)
        val minute = calendar.get(Calendar.MINUTE)

        val timePickerDialog = TimePickerDialog(
            requireContext(),
            { _, hourOfDay, minute ->
                val hourOfDayTemp = hourOfDay.toString().padStart(2, '0')
                val tempMinute = minute.toString().padStart(2, '0')
                textView.text = "$hourOfDayTemp:$tempMinute"
            },
            hour,
            minute,
            true // Use true for 24-hour format, false for 12-hour format
        )

        timePickerDialog.show()
    }

    /**
     * Show date picker dialog and set selected date as result in the text view.
     * @param textView The text view where the selected date will be displayed.
     *  */
    private fun showCalenderAndSetResult(textView: TextView) {
        val calendar = Calendar.getInstance()

        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        val datePickerDialog = DatePickerDialog(
            requireContext(),
            { _, year, monthOfYear, dayOfMonth ->
                val dateOfMonth = if (dayOfMonth < 10) {
                    "0$dayOfMonth"
                } else {
                    dayOfMonth.toString()
                }
                val monthOfYearTemp = if (monthOfYear < 10) {
                    "0${monthOfYear + 1}"
                } else {
                    { monthOfYear + 1 }.toString()
                }
                textView.text =
                    (dateOfMonth + "-" + monthOfYearTemp + "-" + year)
            },
            year,
            month,
            day
        )
        datePickerDialog.show()
    }

    /**
     * Check if all required input fields are filled and valid.
     * @return true if all fields are valid, false otherwise.
     *  */
    private fun areAllInputFieldsValid(): Boolean {
        val titleInput = binding?.etTitle?.text
        val isTitleValid =
            (titleInput.toString().isNotBlank() && (titleInput?.length ?: 0) >= 3)
        if (!isTitleValid) {
            binding?.etTitle?.error = "Please enter valid meeting title"
        }
        return isTitleValid
    }

    override fun onDestroyView() {
        viewmodel.clearAddEventsData()
        super.onDestroyView()
    }
}