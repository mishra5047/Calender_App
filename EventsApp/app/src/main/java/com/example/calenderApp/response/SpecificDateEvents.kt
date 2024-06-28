package com.example.calenderApp.response

/**
 * @author Abhishek Mishra
 * Created 19/06/24
 */
data class SpecificDateEvents(
    val date: String? = null,
    val events: ArrayList<GetEventModel>? = null
)