package be.buithg.etghaifgte.presentation.ui.fragments.main

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import android.widget.Toast
import be.buithg.etghaifgte.R
import androidx.fragment.app.activityViewModels
import androidx.core.content.edit
import androidx.navigation.fragment.findNavController
import dagger.hilt.android.AndroidEntryPoint
import be.buithg.etghaifgte.databinding.FragmentAchievementsBinding
import be.buithg.etghaifgte.data.local.entity.PredictionEntity
import be.buithg.etghaifgte.presentation.viewmodel.PredictionsViewModel
import be.buithg.etghaifgte.utils.Constants.ACHIEVEMENTS_COUNT_KEY
import be.buithg.etghaifgte.utils.Constants.ACHIEVEMENT_FIRSTWIN_CLAIMED_KEY
import be.buithg.etghaifgte.utils.Constants.ACHIEVEMENT_FIRST_WIN_KEY
import be.buithg.etghaifgte.utils.Constants.ACHIEVEMENT_STREAK_CLAIMED_KEY
import be.buithg.etghaifgte.utils.Constants.ACHIEVEMENT_STREAK_KEY
import be.buithg.etghaifgte.utils.Constants.ACHIEVEMENT_TOURNAMENT_CLAIMED_KEY
import be.buithg.etghaifgte.utils.Constants.ACHIEVEMENT_TOURNAMENT_KEY
import be.buithg.etghaifgte.utils.Constants.LEVEL_KEY
import be.buithg.etghaifgte.utils.Constants.getSharedPreferences

@AndroidEntryPoint
class AchievementsFragment : Fragment() {

    private lateinit var  binding: FragmentAchievementsBinding
    private val viewModel: PredictionsViewModel by activityViewModels()

    private val levels = listOf("Starter","Beginner", "Intermediate", "EXPERT")

    private fun updateLevelUI(level: Int) {
        val index = level.coerceIn(0, levels.lastIndex)
        binding.tvLevel.text = levels[index]
    }

    private fun increaseLevel() {
        val prefs = context?.getSharedPreferences() ?: return
        var level = prefs.getInt(LEVEL_KEY, 0)
        if (level < levels.lastIndex) {
            level++
            prefs.edit { putInt(LEVEL_KEY, level) }
            updateLevelUI(level)
        }
    }

    private fun showLevelUpMessage() {
        Toast.makeText(context, "You have reached a new level.", Toast.LENGTH_SHORT).show()
    }
    private fun canClaim(progress: Int, completedKey: String, claimedKey: String): Boolean {
        val prefs = context?.getSharedPreferences() ?: return false
        val completed = prefs.getBoolean(completedKey, false)
        val claimed   = prefs.getBoolean(claimedKey,   false)
        return completed && progress >= 100 && !claimed
    }

    private fun showClaimError(progress: Int, completedKey: String, claimedKey: String) {
        val prefs = context?.getSharedPreferences() ?: return
        val claimed = prefs.getBoolean(claimedKey, false)
        val completed = prefs.getBoolean(completedKey, false)
        val message = if (claimed) {
            "You already achieved that"
        } else {
            "You haven't completed this task yet"
        }
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
    }

    private fun isWin(item: PredictionEntity): Boolean {
        return when (item.wonMatches) {
            1 -> item.pick == item.teamA
            2 -> item.pick == item.teamB
            else -> false
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentAchievementsBinding.inflate(inflater,container,false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                findNavController().navigate(R.id.matchScheduleFragment)
            }
        })

        binding.btnHelp.setOnClickListener {
            findNavController().navigate(R.id.tutorialFragment)
        }

        val prefs = context?.getSharedPreferences()
        val level = prefs?.getInt(LEVEL_KEY, 0) ?: 0
        updateLevelUI(level)
        binding.btnClaimReward.setOnClickListener {
            val prefs = requireContext().getSharedPreferences()
            if (canClaim(
                    binding.progressIndicator.progress,
                    ACHIEVEMENT_STREAK_KEY,
                    ACHIEVEMENT_STREAK_CLAIMED_KEY)) {
                increaseLevel()
                showLevelUpMessage()
                prefs.edit {
                    putBoolean(ACHIEVEMENT_STREAK_CLAIMED_KEY, true)
                }
                binding.btnClaimReward.isEnabled = false
            } else {
                showClaimError(
                    binding.progressIndicator.progress,
                    ACHIEVEMENT_STREAK_KEY,
                    ACHIEVEMENT_STREAK_CLAIMED_KEY
                )
            }
        }
        binding.btnClaimReward2.setOnClickListener {
            val prefs = requireContext().getSharedPreferences()
            if (canClaim(
                    binding.progressIndicator2.progress,
                    ACHIEVEMENT_TOURNAMENT_KEY,
                    ACHIEVEMENT_TOURNAMENT_CLAIMED_KEY)) {
                increaseLevel()
                showLevelUpMessage()
                prefs.edit {
                    putBoolean(ACHIEVEMENT_TOURNAMENT_CLAIMED_KEY, true)
                }
                binding.btnClaimReward2.isEnabled = false
            } else {
                showClaimError(
                    binding.progressIndicator2.progress,
                    ACHIEVEMENT_TOURNAMENT_KEY,
                    ACHIEVEMENT_TOURNAMENT_CLAIMED_KEY
                )
            }
        }
        binding.btnClaimReward3.setOnClickListener {
            val prefs = requireContext().getSharedPreferences()
            if (canClaim(
                    binding.progressIndicator3.progress,
                    ACHIEVEMENT_FIRST_WIN_KEY,
                    ACHIEVEMENT_FIRSTWIN_CLAIMED_KEY)) {
                increaseLevel()
                showLevelUpMessage()
                prefs.edit {
                    putBoolean(ACHIEVEMENT_FIRSTWIN_CLAIMED_KEY, true)
                }
                binding.btnClaimReward3.isEnabled = false
            } else {
                showClaimError(
                    binding.progressIndicator3.progress,
                    ACHIEVEMENT_FIRST_WIN_KEY,
                    ACHIEVEMENT_FIRSTWIN_CLAIMED_KEY
                )
            }
        }

        viewModel.predictions.observe(viewLifecycleOwner) { list ->
            updateAchievements(list)
        }
        viewModel.loadPredictions()
    }

    private fun updateAchievements(list: List<PredictionEntity>) {
        val prefs = context?.getSharedPreferences() ?: return
        var achievements = prefs.getInt(ACHIEVEMENTS_COUNT_KEY, 0)

        val madePrediction = list.isNotEmpty()
        val tournamentDone = prefs.getBoolean(ACHIEVEMENT_TOURNAMENT_KEY, false)
        if (madePrediction && !tournamentDone) {
            achievements++
            prefs.edit {
                putBoolean(ACHIEVEMENT_TOURNAMENT_KEY, true)
                putInt(ACHIEVEMENTS_COUNT_KEY, achievements)
            }
        }
        val progressTournament = if (madePrediction) 100 else 0
        binding.progressIndicator2.progress = progressTournament
        binding.textPercent2.text = "$progressTournament % completed"

        val winExists = list.any { isWin(it) }
        val firstWinDone = prefs.getBoolean(ACHIEVEMENT_FIRST_WIN_KEY, false)
        if (winExists && !firstWinDone) {
            achievements++
            prefs.edit {
                putBoolean(ACHIEVEMENT_FIRST_WIN_KEY, true)
                putInt(ACHIEVEMENTS_COUNT_KEY, achievements)
            }
        }
        val progressWin = if (winExists) 100 else 0
        binding.progressIndicator3.progress = progressWin
        binding.textPercent3.text = "$progressWin % completed"

        val completed = list.filter { !it.upcomingFlag }
        var streak = 0
        var maxStreak = 0
        completed.forEach { item ->
            if (isWin(item)) {
                streak++
                if (streak > maxStreak) maxStreak = streak
            } else {
                streak = 0
            }
        }
        val progressStreak = (maxStreak.coerceAtMost(10) * 10)
        binding.progressIndicator.progress = progressStreak
        binding.textPercent.text = "$progressStreak % completed"
        val streakDone = prefs.getBoolean(ACHIEVEMENT_STREAK_KEY, false)
        if (maxStreak >= 10 && !streakDone) {
            achievements++
            prefs.edit {
                putBoolean(ACHIEVEMENT_STREAK_KEY, true)
                putInt(ACHIEVEMENTS_COUNT_KEY, achievements)
            }
        }

        binding.achievementsText.text = achievements.toString()
    }

}
