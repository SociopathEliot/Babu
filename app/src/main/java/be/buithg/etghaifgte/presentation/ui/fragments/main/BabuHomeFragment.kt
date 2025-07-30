package be.buithg.etghaifgte.presentation.ui.fragments.main

import android.content.pm.ActivityInfo
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import be.buithg.etghaifgte.R
import be.buithg.etghaifgte.databinding.FragmentBabuHomeBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class BabuHomeFragment : Fragment() {

    private lateinit var babuHomeBinding: FragmentBabuHomeBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        babuHomeBinding = FragmentBabuHomeBinding.inflate(inflater, container, false)
        return babuHomeBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        //todo home fragment logic

        val navHostFragment = childFragmentManager
            .findFragmentById(R.id.babu_fragment_container_view) as NavHostFragment
        val navController: NavController = navHostFragment.navController

        babuHomeBinding.babuBottomNav.navHome.setOnClickListener {
            navController.navigate(R.id.matchScheduleFragment)
        }
        babuHomeBinding.babuBottomNav.navPredictions.setOnClickListener {
            navController.navigate(R.id.predictionsFragment)
        }
        babuHomeBinding.babuBottomNav.navHistory.setOnClickListener {
            navController.navigate(R.id.predictionHistoryFragment)
        }
        babuHomeBinding.babuBottomNav.navStats.setOnClickListener {
            navController.navigate(R.id.statsFragment)
        }
        babuHomeBinding.babuBottomNav.navBlog.setOnClickListener {
            navController.navigate(R.id.blogFragment)
        }
        babuHomeBinding.babuBottomNav.navAchievements.setOnClickListener {
            navController.navigate(R.id.achievementsFragment)
        }

        navController.addOnDestinationChangedListener { _, destination, _ ->
            updateSelection(destination.id)
        }

    }

    private fun updateSelection(id: Int) {
        val map = mapOf(
            R.id.matchScheduleFragment to babuHomeBinding.babuBottomNav.navHome,
            R.id.predictionsFragment to babuHomeBinding.babuBottomNav.navPredictions,
            R.id.predictionHistoryFragment to babuHomeBinding.babuBottomNav.navHistory,
            R.id.statsFragment to babuHomeBinding.babuBottomNav.navStats,
            R.id.blogFragment to babuHomeBinding.babuBottomNav.navBlog,
            R.id.achievementsFragment to babuHomeBinding.babuBottomNav.navAchievements,
        )
        map.forEach { (dest, view) ->
            view.isSelected = dest == id
        }
    }
}