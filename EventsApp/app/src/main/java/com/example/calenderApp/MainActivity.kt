package com.example.calenderApp

import android.os.Bundle
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.example.calenderApp.databinding.ActivityMainBinding
import com.example.calenderApp.fragment.EventListFragment
import com.example.calenderApp.fragment.LoginFragment
import com.example.calenderApp.util.FragmentUtil
import com.example.calenderApp.viewmodel.GetEventViewModel
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential
import com.google.api.client.http.javanet.NetHttpTransport
import com.google.api.client.json.gson.GsonFactory
import com.google.api.client.util.ExponentialBackOff
import com.google.api.services.calendar.Calendar
import com.google.api.services.calendar.CalendarScopes
import com.google.firebase.auth.FirebaseAuth

/**
 * Approach used ->
 * In case of already existing auth redirecting to GetEventViewModel
 * In case of new auth redirecting to LoginFragment
 *
 * All Fragments are hosted in MainActivity
 * Fragments are changes using common method mentioned below
 */
class MainActivity : AppCompatActivity() {

    private val viewmodel: GetEventViewModel by viewModels { GetEventViewModel.Factory }
    private val firebaseAuth by lazy { FirebaseAuth.getInstance() }

    private var binding: ActivityMainBinding? = null
    private var mCredential: GoogleAccountCredential? = null
    private var calendar: Calendar? = null
    private val fragmentUtil by lazy { FragmentUtil() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding?.root)

        initCalenderForApplication()
        setBackPress()
    }

    private fun setBackPress() {
        val callback: OnBackPressedCallback =
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    if (supportFragmentManager.fragments.size == 1) {
                        finish()
                    } else {
                        supportFragmentManager.popBackStack()
                    }
                }
            }
        onBackPressedDispatcher.addCallback(this, callback)
    }

    private fun initCalender() {
        mCredential = GoogleAccountCredential.usingOAuth2(
            this,
            arrayListOf(CalendarScopes.CALENDAR)
        )
            .setBackOff(ExponentialBackOff())
        mCredential?.selectedAccountName = firebaseAuth.currentUser?.email
        calendar = Calendar.Builder(
            NetHttpTransport(), GsonFactory.getDefaultInstance(), mCredential
        )
            .setApplicationName("GetEventCalendar")
            .build()
        viewmodel.setCalender(calendar)
    }

    private fun initCalenderForApplication() {
        if (firebaseAuth.currentUser != null) {
            initCalender()
            fragmentUtil.addFragmentToStack(
                supportFragmentManager,
                fragment = EventListFragment.newInstance()
            )
        } else {
            fragmentUtil.addFragmentToStack(
                supportFragmentManager,
                fragment = LoginFragment.newInstance()
            )
        }
    }

}