package be.buithg.etghaifgte.presentation.ui.fragments.onboarding

import android.content.pm.ActivityInfo
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.edit
import androidx.fragment.app.Fragment
import be.buithg.etghaifgte.databinding.FragmentWelcomeBinding
import be.buithg.etghaifgte.presentation.ui.fragments.main.HomeFragment
import be.buithg.etghaifgte.utils.BabuAppConstants.BABU_WELCOME_KEY
import be.buithg.etghaifgte.utils.BabuAppConstants.getBabuPreferences
import be.buithg.etghaifgte.utils.BabuAppConstants.openBabuFragmentNoHistory

class WelcomeFragment : Fragment() {

    private lateinit var binding: FragmentWelcomeBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        binding = FragmentWelcomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        binding.nextMaterialButton.setOnClickListener {
            context?.getBabuPreferences()?.edit { putBoolean(BABU_WELCOME_KEY, true).apply() }
            parentFragmentManager.openBabuFragmentNoHistory(HomeFragment())
        }
    }
}