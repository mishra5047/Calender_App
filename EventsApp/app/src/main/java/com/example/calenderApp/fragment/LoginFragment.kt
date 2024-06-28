package com.example.calenderApp.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.calenderApp.databinding.FragmentLoginBinding
import com.example.calenderApp.util.FragmentUtil
import com.example.calenderApp.viewmodel.GetEventViewModel
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.common.api.Scope
import com.google.android.gms.tasks.Task
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential
import com.google.api.client.http.javanet.NetHttpTransport
import com.google.api.client.json.gson.GsonFactory
import com.google.api.client.util.ExponentialBackOff
import com.google.api.services.calendar.Calendar
import com.google.api.services.calendar.CalendarScopes
import com.google.firebase.auth.AuthCredential
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider

/**
 * @author Abhishek Mishra
 * Created 19/06/24
 */
class LoginFragment : Fragment() {

    companion object {
        const val TAG = "LoginFragment"

        fun newInstance(bundle: Bundle? = null): LoginFragment {
            return LoginFragment().apply {
                arguments = bundle
            }
        }
    }

    private val googleSignInRequest by lazy {
        GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken("407930914901-5mpbe6hiqenejgu6iddtfev8e1v3jrvn.apps.googleusercontent.com")
            .requestEmail()
            .requestScopes(Scope(CalendarScopes.CALENDAR))
            .build()
    }

    private val firebaseAuth by lazy {
        FirebaseAuth.getInstance()
    }

    private var mCredential: GoogleAccountCredential? = null //to access our account
    private val mGoogleSignInClient: GoogleSignInClient? by lazy {
        activity?.let {
            GoogleSignIn.getClient(it, googleSignInRequest)
        } ?: run {
            null
        }
    }
    private val viewmodel: GetEventViewModel by lazy { ViewModelProvider(requireActivity())[GetEventViewModel::class.java] }
    private var calendar: Calendar? = null

    private var binding: FragmentLoginBinding? = null
    private val fragmentUtil by lazy { FragmentUtil() }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        binding = FragmentLoginBinding.inflate(layoutInflater)
        return binding?.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding?.buttonLogin?.setOnClickListener {
            requestLogin()
        }
        if(firebaseAuth.currentUser != null){
            initCalender()
        }
    }

    /**
     * Initiate Google Sign-In
     */
    private fun requestLogin() {
        mGoogleSignInClient?.signInIntent?.let {
            resultFromGoogleSignIn.launch(it)
        }
    }

    private val resultFromGoogleSignIn =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            val task: Task<GoogleSignInAccount> =
                GoogleSignIn.getSignedInAccountFromIntent(it.data)
            if (task.isSuccessful) {
                val googleSignInAccount = task.getResult(ApiException::class.java)
                if (googleSignInAccount != null) {
                    val authCredential: AuthCredential = GoogleAuthProvider.getCredential(
                        googleSignInAccount.idToken, null
                    )
                    firebaseAuth.signInWithCredential(authCredential)
                        .addOnCompleteListener { result ->
                            if (result.isSuccessful) {
                                Toast.makeText(
                                    requireContext(),
                                    "Google authentication success",
                                    Toast.LENGTH_SHORT
                                ).show()
                                initCalender()

                            } else {
                                Toast.makeText(
                                    requireContext(),
                                    "Google authentication failed",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        }
                }
            }
        }

    /**
     * Initialize Google Calendar API
     */
    private fun initCalender() {
        mCredential = GoogleAccountCredential.usingOAuth2(
            requireContext(),
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

        fragmentUtil.addFragmentToStack(
            fragmentManager = parentFragmentManager,
            fragment = EventListFragment.newInstance(),
            shouldPopLastFragment = true
        )
    }
}