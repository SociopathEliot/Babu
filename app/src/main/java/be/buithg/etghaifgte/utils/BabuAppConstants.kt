package be.buithg.etghaifgte.utils

import android.content.Context
import android.content.SharedPreferences
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import be.buithg.etghaifgte.R

object BabuAppConstants {
    const val BABU_DEFAULT_PP_LINK = "https://sites.google.com/view/examplesampleprivacypolicy"
    const val BABU_DEFAULT_TOS_LINK = "https://sites.google.com/view/examplesampletermsofuse"
    const val BABU_DEFAULT_DOMAIN_LINK = "https://pastebin.com/raw/q1CQaY4k"
    const val BABU_MAIN_OFFER_LINK_KEY = "main_offer_link"
    const val BABU_USER_STATUS_KEY = "user_status"
    const val BABU_WELCOME_KEY = "welcome"
    const val BABU_ACHIEVEMENTS_COUNT_KEY = "achievements_count"
    const val BABU_ACHIEVEMENT_STREAK_KEY = "achievement_streak"
    const val BABU_ACHIEVEMENT_TOURNAMENT_KEY = "achievement_tournament"
    const val BABU_ACHIEVEMENT_FIRST_WIN_KEY = "achievement_first_win"
    const val BABU_LEVEL_KEY = "user_level"
    private const val BABU_SHARED_PREFERENCES_KEY = "example_sample_shared_preferences"
    const val BABU_ACHIEVEMENT_STREAK_CLAIMED_KEY     = "achievement_streak_claimed"
    const val BABU_ACHIEVEMENT_TOURNAMENT_CLAIMED_KEY = "achievement_tournament_claimed"
    const val BABU_ACHIEVEMENT_FIRSTWIN_CLAIMED_KEY   = "achievement_firstwin_claimed"


    fun Context.getBabuPreferences(): SharedPreferences {
        return this.getSharedPreferences(BABU_SHARED_PREFERENCES_KEY, Context.MODE_PRIVATE)
    }

    fun FragmentManager.showBabuFragment(fragment: Fragment) {
        this.beginTransaction().apply {
            replace(R.id.navHostFragment, fragment)
            addToBackStack(null)
            commit()
        }
    }
    fun FragmentManager.displayBabuFragmentNew(fragment: Fragment) {
        this.beginTransaction().apply {
            replace(R.id.babu_fragment_container_view, fragment)
            addToBackStack(null)
            commit()
        }
    }

    fun FragmentManager.openBabuFragmentNoHistory(fragment: Fragment) {
        this.beginTransaction().apply {
            replace(R.id.navHostFragment, fragment)
            commit()
        }
    }
}