package com.example.calenderApp.response

/**
 * @author Abhishek Mishra
 * Created 19/06/24
 */
data class GetEventModel(
    val summary: String? = "",
    val startDate: String = "",
    val endDate: String? = "",
    val id : String? = ""
)