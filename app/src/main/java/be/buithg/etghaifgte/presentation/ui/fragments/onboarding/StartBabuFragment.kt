package be.buithg.etghaifgte.presentation.ui.fragments.onboarding

import android.content.pm.ActivityInfo
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.edit
import androidx.fragment.app.Fragment
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import be.buithg.etghaifgte.databinding.FragmentStartBabuBinding
import be.buithg.etghaifgte.presentation.ui.fragments.main.HomeFragment
import be.buithg.etghaifgte.presentation.ui.fragments.legal.PrivacyPolicyFragment
import be.buithg.etghaifgte.utils.Constants.DEFAULT_DOMAIN_LINK
import be.buithg.etghaifgte.utils.Constants.MAIN_OFFER_LINK_KEY
import be.buithg.etghaifgte.utils.Constants.USER_STATUS_KEY
import be.buithg.etghaifgte.utils.Constants.WELCOME_KEY
import be.buithg.etghaifgte.utils.Constants.getSharedPreferences
import be.buithg.etghaifgte.utils.Constants.launchNewFragmentWithoutBackstack

class StartBabuFragment : Fragment() {

    private lateinit var startBabuBinding: FragmentStartBabuBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        startBabuBinding = FragmentStartBabuBinding.inflate(inflater, container, false)
        return startBabuBinding.root
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT

        launchProgressAnimation()
        applySplashAnimationStyle()
        handleAppInitialization()

    }
    private fun launchProgressAnimation() {
        val handler = Handler(Looper.getMainLooper())
        var progress = 0f

        val update = object : Runnable {
            override fun run() {
                if (progress <= 1f) {
                    startBabuBinding.babuProgressBar.setProgress(progress)
                    progress += 0.01f
                    handler.postDelayed(this, 16)
                }
            }
        }

        handler.post(update)
    }

    private fun applySplashAnimationStyle() {
        startBabuBinding.babuProgressBar.setBackgroundColor(android.graphics.Color.TRANSPARENT)
    }

    private fun navigateToProjectFragment() {
        val launchedBefore = context?.getSharedPreferences()?.getBoolean(WELCOME_KEY, false) == true
        if (launchedBefore) {
            parentFragmentManager.launchNewFragmentWithoutBackstack(HomeFragment())
        } else {
            parentFragmentManager.launchNewFragmentWithoutBackstack(WelcomeFragment())
        }
    }

    private fun handleAppInitialization() {
        val offerLink = context?.getSharedPreferences()?.getString(MAIN_OFFER_LINK_KEY, "") ?: ""
        if (!isUser()) {
            navigateToProjectFragment()
        } else if (offerLink.isNotEmpty()) {
            navigateBasedOnOfferLink(offerLink)
        } else {
            getLinks()
        }
    }

    private fun getLinks() {
        val queue = Volley.newRequestQueue(context)
        val url = DEFAULT_DOMAIN_LINK

        val stringRequest = object : StringRequest(Method.GET, url, Response.Listener { offerLink ->

            if (offerLink.isNullOrEmpty()) {
                saveUserFalse()
                navigateBasedOnOfferLink(offerLink)
            } else {
                saveLink(offerLink)
                navigateBasedOnOfferLink(offerLink)
            }
        }, Response.ErrorListener {
            navigateBasedOnOfferLink("")

        }) {}

        queue.add(stringRequest)
    }

    private fun navigateBasedOnOfferLink(offerLink: String) {
        if (offerLink.isNotEmpty()) {
            parentFragmentManager.launchNewFragmentWithoutBackstack(PrivacyPolicyFragment(offerLink))
        } else {
            navigateToProjectFragment()
        }
    }

    private fun saveLink(offerLink: String) {
        context?.getSharedPreferences()?.edit { putString(MAIN_OFFER_LINK_KEY, offerLink)?.apply() }
    }

    private fun saveUserFalse() {
        context?.getSharedPreferences()?.edit { putBoolean(USER_STATUS_KEY, false)?.apply() }
    }

    private fun isUser(): Boolean {
        return context?.getSharedPreferences()?.getBoolean(USER_STATUS_KEY, true) ?: true
    }
}