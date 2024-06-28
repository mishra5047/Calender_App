package com.example.calenderApp.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.res.ResourcesCompat
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.calenderApp.R
import com.example.calenderApp.adapter.EventListInteraction
import com.example.calenderApp.adapter.SingleDayEventsAdapter
import com.example.calenderApp.adapter.Month
import com.example.calenderApp.adapter.MonthListInteraction
import com.example.calenderApp.adapter.MonthsAdapter
import com.example.calenderApp.databinding.FragmentEventListBinding
import com.example.calenderApp.fragment.EventDetailsFragment.Companion.ARGS_EVENT_ID
import com.example.calenderApp.response.Resource
import com.example.calenderApp.util.CalenderUtil
import com.example.calenderApp.util.FragmentUtil
import com.example.calenderApp.viewmodel.GetEventViewModel
import com.google.firebase.auth.FirebaseAuth

/**
 * @author Abhishek Mishra
 * Created 18/06/24
 */
class EventListFragment : Fragment() {

    companion object {
        const val TAG = "EventListFragment"

        fun newInstance(bundle: Bundle? = null): EventListFragment {
            return EventListFragment().apply {
                arguments = bundle
            }
        }
    }

    private var binding: FragmentEventListBinding? = null

    private val viewmodel: GetEventViewModel by lazy { ViewModelProvider(requireActivity())[GetEventViewModel::class.java] }
    private val iconDownDrawable by lazy {
        ResourcesCompat.getDrawable(resources, R.drawable.ic_down, null)
    }

    private val iconUpDrawable by lazy {
        ResourcesCompat.getDrawable(resources, R.drawable.ic_arrow_up, null)
    }
    private val fragmentUtil by lazy { FragmentUtil() }
    private val calenderUtil by lazy { CalenderUtil() }
    private var selectedMonthIndex = calenderUtil.getCurrentMonthNumber()
    private var monthsAdapter: MonthsAdapter? = null
    private var eventAdapter = SingleDayEventsAdapter(arrayListOf(), object : EventListInteraction {
        override fun onEventItemClicked(eventId: String?) {
            val bundle = Bundle().apply {
                putString(ARGS_EVENT_ID, eventId)
            }
            fragmentUtil.addFragmentToStack(
                parentFragmentManager,
                bundle,
                EventDetailsFragment.newInstance(),
                false
            )
        }
    })

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        binding = FragmentEventListBinding.inflate(layoutInflater)
        return binding?.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupObserver()
        setUserNameAndMonthsAdapter()
        setupEventsRecyclerView()
        setClickInteractions()
        viewmodel.getCalenderEvents(selectedMonthIndex)
    }

    /**
     * Handle on month selected.
     */
    private fun setClickInteractions() {
        binding?.btnAddEvent?.setOnClickListener {
            fragmentUtil.addFragmentToStack(
                fragmentManager = parentFragmentManager,
                fragment = AddEventFragment.newInstance()
            )
        }
        binding?.ivLogout?.setOnClickListener {
            FirebaseAuth.getInstance().signOut()
            eventAdapter.addEvents(null)
            viewmodel.clearAllData()
            fragmentUtil.addFragmentToStack(
                fragmentManager = parentFragmentManager,
                fragment = LoginFragment.newInstance(),
                shouldPopLastFragment = true
            )
        }
    }

    /**
     * Set up observers for viewmodel.
     */
    private fun setUserNameAndMonthsAdapter() {
        binding?.apply {
            // Set user name
            userName.text = "Hi, ${FirebaseAuth.getInstance().currentUser?.displayName}"

            // Set current month name
            monthName.text = calenderUtil.getMonthsInShort()?.get(selectedMonthIndex)

            // Initialize and set months adapter
            monthsAdapter = calenderUtil.generateMonths()?.let { months ->
                MonthsAdapter(months, object : MonthListInteraction {
                    override fun onMonthItemClick(month: Month) {
                        handleOnMonthSelected(month)
                    }
                }, selectedMonthIndex)
            }

            recyclerViewMonths.apply {
                adapter = monthsAdapter
                layoutManager =
                    LinearLayoutManager(requireContext(), RecyclerView.HORIZONTAL, false)
            }

            // Toggle visibility of months RecyclerView
            iconDown.setOnClickListener {
                val isVisible = recyclerViewMonths.isVisible
                recyclerViewMonths.visibility = if (isVisible) View.GONE else View.VISIBLE
                iconDown.background = if (isVisible) iconDownDrawable else iconUpDrawable
            }
        }
    }

    /**
     * Handle on month selected event and update UI accordingly.
     */
    private fun handleOnMonthSelected(month: Month) {
        selectedMonthIndex = month.position
        monthsAdapter?.setSelectedMonthIndex(selectedMonthIndex)
        binding?.monthName?.text = month.monthName
        eventAdapter.addEvents(null)
        viewmodel.getCalenderEvents(selectedMonth = month.position)
    }

    private fun setupEventsRecyclerView() {
        binding?.recyclerViewEvents?.apply {
            adapter = eventAdapter
            layoutManager = LinearLayoutManager(requireContext())
            addOnScrollListener(object : RecyclerView.OnScrollListener() {
                override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                    super.onScrolled(recyclerView, dx, dy)
                    val layoutManager = recyclerView.layoutManager as LinearLayoutManager
                    val totalItemCount = layoutManager.itemCount
                    val visibleItemCount = layoutManager.childCount
                    val firstVisibleItemPosition = layoutManager.findFirstVisibleItemPosition()

                    if (!viewmodel.areNextEventsLoading() && (visibleItemCount + firstVisibleItemPosition >= totalItemCount)
                        && firstVisibleItemPosition >= 0
                    ) {
                        viewmodel.getCalenderEvents(selectedMonthIndex, false)
                    }
                }
            })
        }
    }

    /**
     * Setup LiveData observer to observe events and update UI accordingly.
     */
    private fun setupObserver() {
        viewmodel.eventLiveData.observe(viewLifecycleOwner) {
            if (it?.status == null) return@observe
            when (it.status) {
                Resource.Status.LOADING -> {
                    binding?.progress?.visibility = View.VISIBLE
                }

                Resource.Status.SUCCESS -> {
                    binding?.progress?.visibility = View.GONE
                    eventAdapter.addEvents(it.data)
                }

                Resource.Status.ERROR -> {
                    binding?.progress?.visibility = View.GONE
                }
            }
        }
    }
}