# Calender Events App

A calendar app with Google Calendar integration, allowing users to view, add, and manage events.

Powered by google's calendar API

Features

- View All events month wise
- Ability to select month by choosing from the list of months
- User can add events with all relevant details like Event Name, event time, attendees etc.

## Tech Stack

* Language used -> Kotlin
* Arch pattern -> MVVM

## Approach used

**Fetching Events**

* Each month has been divided into a group of 5 dates
  [1 jan - 5 jan], [6 jan - 10 jan], [11 jan - 15 jan], [16 jan - 20 jan], [21 jan - 25 jan], [26 jan - 30 jan], [31 jan]
* These groups are iterated using a global index, the index is reset whenever a user changes the
  month (for example - he was seeing events for june and selected july post that)
* The loading of next pair for any month is handled by adding scroll listener to the recycler view.
  As soon as user scrolls to the last loaded date we check if there next pair to be loaded.

* Events loading logic ->
* A given pair is passed to the calender api to get all the events between these dates, the response
  handling is done according to event type.
* For a normal event -> the details are added into the formatting list by just parsing the start
  date, time etc.
* For a recurring event -> Calender API is used to fetch details of a specific event using the
  recurring meeting id, then this response is formatted and added.
* After all returned results are added, we check for missing dates from the given pair. i.e all
  those dates that don't have any events in the first, for them a dummy UI object is added saying no
  events for that day.

**Why this custom logic to pass pair of 5 days rather than single dates in a loop?**

* It's better in terms of loading events as it loads events for 5 days rather than requesting for
  each date.
* Also fetching in this way will optimise the API calls too

## File structure

* MainActivity -> checks if the user session already exists and redirects to the event screen. If
  new user, he's sent to loginFragment. Else creates the relevant objects needed by the whole app
  and redirects to event screen
* SingleDayEventsAdapter -> The adapter responsible for showing all events on any given day.
* MonthsAdapter -> the adapter that displays the month name horizontally at the top.
* AddEventFragment -> Fragment responsible for adding new
* EventDetailsFragment -> Fragment that shows all the details for a existing event.
* EventListFragment -> The fragment that contains all list of all events in a month
* LoginFragment -> The Fragment that contains logic related to login and google authentication.
* CalenderUtil -> All util function that handle date parsing according to different use-cases.
* FragmentUtil -> contains a single function to load fragment
* MainActivity -> The only activity in the project, hosts all the fragment