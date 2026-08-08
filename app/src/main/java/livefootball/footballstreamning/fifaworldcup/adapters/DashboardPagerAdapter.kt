package livefootball.footballstreamning.fifaworldcup.adapters

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import livefootball.footballstreamning.fifaworldcup.fragments.MatchHighlightsFragment
import livefootball.footballstreamning.fifaworldcup.fragments.LiveMatchesFragment
import livefootball.footballstreamning.fifaworldcup.fragments.GeneralScoresFragment
import livefootball.footballstreamning.fifaworldcup.fragments.AppSettingsFragment
import livefootball.footballstreamning.fifaworldcup.R

class DashboardPagerAdapter(fragmentActivity: FragmentActivity) : FragmentStateAdapter(fragmentActivity) {

    private var showHighlights = true
    private var showScore = false
    private var showHome = true

    fun updateHighlightsVisibility(visible: Boolean) {
        if (showHighlights != visible) {
            showHighlights = visible
            notifyDataSetChanged()
        }
    }

    fun updateScoreVisibility(visible: Boolean) {
        if (showScore != visible) {
            showScore = visible
            notifyDataSetChanged()
        }
    }

    fun updateHomeVisibility(visible: Boolean) {
        if (showHome != visible) {
            showHome = visible
            notifyDataSetChanged()
        }
    }

    override fun createFragment(position: Int): Fragment {
        val fragments = mutableListOf<Fragment>()
        if (showScore) fragments.add(GeneralScoresFragment())
        if (showHome) fragments.add(LiveMatchesFragment())
        if (showHighlights) fragments.add(MatchHighlightsFragment())
        fragments.add(AppSettingsFragment())

        return if (position < fragments.size) fragments[position] else AppSettingsFragment()
    }

    override fun getItemCount(): Int {
        var count = 1 // Settings is always there
        if (showScore) count++
        if (showHome) count++
        if (showHighlights) count++
        return count
    }

    override fun getItemId(position: Int): Long {
        return getIdForPosition(position).toLong()
    }

    override fun containsItem(itemId: Long): Boolean {
        val id = itemId.toInt()
        val ids = mutableListOf<Int>()
        if (showScore) ids.add(R.id.navigation_score)
        if (showHome) ids.add(R.id.navigation_home)
        if (showHighlights) ids.add(R.id.navigation_highlights)
        ids.add(R.id.navigation_settings)
        return ids.contains(id)
    }

    fun getPositionForId(itemId: Int): Int {
        val ids = mutableListOf<Int>()
        if (showScore) ids.add(R.id.navigation_score)
        if (showHome) ids.add(R.id.navigation_home)
        if (showHighlights) ids.add(R.id.navigation_highlights)
        ids.add(R.id.navigation_settings)

        val index = ids.indexOf(itemId)
        return if (index != -1) index else 0
    }

    fun getIdForPosition(position: Int): Int {
        val ids = mutableListOf<Int>()
        if (showScore) ids.add(R.id.navigation_score)
        if (showHome) ids.add(R.id.navigation_home)
        if (showHighlights) ids.add(R.id.navigation_highlights)
        ids.add(R.id.navigation_settings)

        return if (position < ids.size) ids[position] else R.id.navigation_home
    }
}
