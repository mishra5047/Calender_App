package com.example.calenderApp.util

import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import com.example.calenderApp.R

/**
 * @author Abhishek Mishra
 * Created 21/06/24
 */
class FragmentUtil {

    // function to add a fragment to the stack
    fun addFragmentToStack(
        fragmentManager: FragmentManager,
        bundle: Bundle? = null, fragment: Fragment,
        shouldPopLastFragment: Boolean = false
    ) {
        if (shouldPopLastFragment) {
            fragmentManager.popBackStackImmediate()
        }
        fragment.arguments = bundle
        val ft = fragmentManager.beginTransaction()
        ft.add(R.id.container, fragment, fragment.tag)
        ft.addToBackStack(fragment.tag)
        ft.commit()
    }
}