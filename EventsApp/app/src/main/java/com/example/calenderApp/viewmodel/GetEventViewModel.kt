package com.example.calenderApp.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.CreationExtras
import com.example.calenderApp.response.GetEventModel
import com.example.calenderApp.response.Resource
import com.example.calenderApp.response.SpecificDateEvents
import com.example.calenderApp.util.CalenderUtil
import com.google.api.client.util.DateTime
import com.google.api.services.calendar.Calendar
import com.google.api.services.calendar.model.Event
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


/**
 * @author Abhishek Mishra
 * Created 18/06/24
 */
class GetEventViewModel : ViewModel() {
    companion object {
        const val TAG = "GetEventViewModel"

        val Factory: ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(
                modelClass: Class<T>,
                extras: CreationExtras
            ): T {
                return GetEventViewModel() as T
            }
        }
    }

    private val calenderUtil by lazy { CalenderUtil() }

    private var pairOfFiveDatesForAllMonths: ArrayList<ArrayList<Pair<DateTime, DateTime>>>? = null

    init {
        pairOfFiveDatesForAllMonths = calenderUtil.getPairOfFiveForAllMonths()
    }

    private var currentMonthIteratingIndex = 0

    private var calendar: Calendar? = null

    fun setCalender(calender: Calendar?) {
        this@GetEventViewModel.calendar = calender
    }

    private val _eventLiveData = MutableLiveData<Resource<ArrayList<SpecificDateEvents>>>()
    val eventLiveData: LiveData<Resource<ArrayList<SpecificDateEvents>>> get() = _eventLiveData

    private val _eventDetailsLiveData = MutableLiveData<Resource<Event>?>()
    val eventDetailsLiveData: LiveData<Resource<Event>?> get() = _eventDetailsLiveData

    private val _addEventLiveData = MutableLiveData<Resource<Event>?>()
    val addEventLiveData: LiveData<Resource<Event>?> get() = _addEventLiveData

    private var areNextEventsLoading = false

    /**
     * Fetches calendar events for the specified month, in paginated format if required.
     *
     * @param selectedMonth The month for which to fetch events (0-based index, where 0 = January).
     * @param isInitialLoad Boolean indicating if this is the initial load of events. Defaults to true.
     */
    fun getCalenderEvents(
        selectedMonth: Int, isInitialLoad: Boolean = true
    ) {

        if (isInitialLoad) {
            currentMonthIteratingIndex = 0
        }
        areNextEventsLoading = true
        // check and return if already at last pair of that given month
        if (currentMonthIteratingIndex >= (pairOfFiveDatesForAllMonths?.get(selectedMonth)?.size
                ?: 0)
        ) {
            return
        }
        viewModelScope.launch(Dispatchers.IO) {
            val firstAndLastDateOfMonth =
                pairOfFiveDatesForAllMonths?.get(selectedMonth)?.get(currentMonthIteratingIndex)
            firstAndLastDateOfMonth ?: return@launch

            currentMonthIteratingIndex++


            _eventLiveData.postValue(Resource.loading())
            val eventsForSpecificDate =
                calendar?.events()?.list("primary")?.setOrderBy("startTime")
                    ?.setSingleEvents(true)?.setTimeMin(firstAndLastDateOfMonth.first)
                    ?.setTimeMax(firstAndLastDateOfMonth.second)
                    ?.execute()

            val mapOfEvents = hashMapOf<String, ArrayList<GetEventModel>>()
            val listOfEvents = eventsForSpecificDate?.items

            listOfEvents?.forEach { event ->
                if (event.recurringEventId == null) {
                    addNonRecurringEvents(event, mapOfEvents)
                } else {
                    addEventDetailsForNestedEvents(event, mapOfEvents)
                }
            }
            addEmptyEventDays(
                mapOfEvents, Pair(
                    firstAndLastDateOfMonth.first,
                    firstAndLastDateOfMonth.second
                )
            )

            areNextEventsLoading = false
            _eventLiveData.postValue(
                Resource.success(
                    sortEventsByDateAndTime(mapOfEvents)
                )
            )
        }
    }

    private fun sortEventsByDateAndTime(mapOfEvents: java.util.HashMap<String, java.util.ArrayList<GetEventModel>>): ArrayList<SpecificDateEvents> {
        val events = arrayListOf<SpecificDateEvents>()
        mapOfEvents.toSortedMap(compareBy { it.split("\n")[1].toInt() }).forEach {
            events.add(
                SpecificDateEvents(
                    date = it.key,
                    events = it.value
                )
            )
        }
        return events
    }

    private fun addNonRecurringEvents(
        event: Event?,
        mapOfEvents: java.util.HashMap<String, java.util.ArrayList<GetEventModel>>
    ) {
        val eventDate = calenderUtil.getDateInIst(event?.start?.dateTime)
        mapOfEvents[eventDate] =
            mapOfEvents.getOrDefault(eventDate, arrayListOf()).apply {
                event?.toGetEventModel()?.let {
                    add(
                        it
                    )
                }
            }
    }

    private fun addEventDetailsForNestedEvents(
        event: Event,
        mapOfEvents: HashMap<String, ArrayList<GetEventModel>>
    ) {
        val calenderEvent =
            calendar?.Events()?.get("primary", event.recurringEventId)?.execute()
        val eventDate = calenderUtil.getDateInIst(event.start?.dateTime)
        if (eventDate.isNotBlank()) {
            mapOfEvents[eventDate] =
                mapOfEvents.getOrDefault(eventDate, arrayListOf()).apply {
                    calenderEvent?.toGetEventModel()?.let { add(it) }
                }
        }
    }

    private fun Event.toGetEventModel(): GetEventModel {
        return GetEventModel(
            summary = summary,
            startDate = calenderUtil.getTimeInIst(start.dateTime),
            endDate = calenderUtil.getTimeInIst(end.dateTime),
            id = id
        )
    }

    private fun addEmptyEventDays(
        mapOfEvents: HashMap<String, ArrayList<GetEventModel>>,
        dateRange: Pair<DateTime, DateTime>
    ) {
        val allDates = calenderUtil.getAllDatesBetween(dateRange.first, dateRange.second)
        allDates.forEach {
            val dateInString = calenderUtil.getDateInIst(it)
            if (!mapOfEvents.containsKey(dateInString)) {
                mapOfEvents[dateInString] = arrayListOf(
                    GetEventModel(
                        summary = "No Events"
                    )
                )
            }
        }
    }

    fun getEventDetails(eventId: String?) = viewModelScope.launch(Dispatchers.IO) {
        eventId ?: return@launch
        _eventDetailsLiveData.postValue(Resource.loading())
        try {
            val calenderEvent = calendar?.Events()?.get("primary", eventId)?.execute()
            if (calenderEvent != null) {
                _eventDetailsLiveData.postValue(Resource.success(calenderEvent))
            } else {
                _eventDetailsLiveData.postValue(Resource.error())
            }
        } catch (thr: Throwable) {
            _eventDetailsLiveData.postValue(Resource.error())
        }
    }

    fun addEvent(event: Event) = viewModelScope.launch(Dispatchers.IO) {
        _addEventLiveData.postValue(Resource.loading())
        try {
            val calenderEvent = calendar?.Events()?.insert("primary", event)?.execute()
            if (calenderEvent != null) {
                _addEventLiveData.postValue(Resource.success(calenderEvent))
            } else {
                _addEventLiveData.postValue(Resource.error())
            }
        } catch (thr: Throwable) {
            _addEventLiveData.postValue(Resource.error())
        }
    }

    fun areNextEventsLoading() = areNextEventsLoading

    fun clearEventDetailsData() {
        _eventDetailsLiveData.value = null
    }

    fun clearAddEventsData() {
        _addEventLiveData.value = null
    }

    fun clearAllData() {
        clearEventDetailsData()
        clearAddEventsData()
        currentMonthIteratingIndex = 0
        _eventLiveData.value = null
    }
}