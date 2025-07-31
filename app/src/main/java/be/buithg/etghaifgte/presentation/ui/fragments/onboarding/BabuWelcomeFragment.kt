package be.buithg.etghaifgte.presentation.ui.fragments.onboarding

import android.content.pm.ActivityInfo
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.edit
import androidx.fragment.app.Fragment
import be.buithg.etghaifgte.databinding.FragmentBabuWelcomeBinding
import be.buithg.etghaifgte.presentation.ui.fragments.main.BabuHomeFragment
import be.buithg.etghaifgte.utils.BabuAppConstants.BABU_WELCOME_KEY
import be.buithg.etghaifgte.utils.BabuAppConstants.getBabuPreferences
import be.buithg.etghaifgte.utils.BabuAppConstants.openBabuFragmentNoHistory

class BabuWelcomeFragment : Fragment() {

    private lateinit var binding: FragmentBabuWelcomeBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?,
    ): View? {
        binding = FragmentBabuWelcomeBinding.inflate(inflater, container, false)
        return binding.root

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        binding.babuNextMaterialButton.setOnClickListener {

            context?.getBabuPreferences()?.edit { putBoolean(BABU_WELCOME_KEY, true).apply() }
            parentFragmentManager.openBabuFragmentNoHistory(BabuHomeFragment())
        }
    }
}
